# rpd-selftest: hidden-window runtime scenario tests for Radish Pixel Dungeon.
# (This file is kept ASCII-only on purpose: Windows PowerShell 5.1 reads BOM-less scripts as ANSI.)
#
# Usage:
#   powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 `
#       -Worktree <path of the worktree to test> -Scenario <Name | Name1,Name2 | Pattern* | all | extra | selftest | list>
#
#   all      = every game scenario for the reported bugs (everything except Extra_* and SelfTest_*)
#   extra    = the Extra_* scenarios: incidental findings that are not in the user's bug report
#   selftest = the SelfTest_* scenarios: they test the tool itself (stuck-turn detection, crash capture on
#              both threads, timeouts, hero death, save/reload ...); several of them are DESIGNED to end
#              with STUCK / CRASH / FAIL / ERROR / KILLED and are counted as OK when they do
#   list     = print the scenario names and exit
#
# Optional: -Overlay <dir with game .java files>  tries a candidate fix on top of the worktree's classes
#           without touching the worktree (see the parameter comment below and README.md).
#
# What it does:
#   1. builds the target worktree's :desktop runtime classpath (Gradle, under the machine-wide
#      semaphore rpd_gradle_build, see tools\build-target.ps1) unless -SkipBuild;
#   2. compiles the harness (src\) and the scenarios (scenarios\) against that classpath;
#   3. runs every selected scenario in its own JVM: real engine, hidden window, no audio,
#      saves/settings redirected into a per-run sandbox; at most 2 game processes machine-wide
#      (named semaphore rpd_selftest_game);
#   4. prints one verdict line per scenario, plus the key log / stack for everything that is not PASS.
#
# Exit code: 0 = every scenario PASS (self-tests: ended with their expected verdict);
#            1 = at least one FAIL / CRASH / STUCK (a verdict about the game);
#            2 = tool problem (build failed, harness does not compile, ERROR / UNSAFE / DIED / KILLED,
#                or a self-test that did not end with its expected verdict).
param(
    [Parameter(Mandatory = $true)][string]$Worktree,
    [Parameter(Mandatory = $true)][string]$Scenario,
    # reuse the classpath exported by the last build of this worktree (no Gradle run)
    [switch]$SkipBuild,
    # per scenario process: in-process watchdog; the runner kills the process 30 s later if it is still there
    [int]$TimeoutSeconds = 240,
    # whole invocation
    [int]$TotalTimeoutMinutes = 90,
    # animation / turn speed-up inside the dungeon (1 = real time)
    [double]$TimeScale = 8,
    [int]$Fps = 120,
    [ValidateRange(1, 2)][int]$MaxParallel = 2,
    # run every selected scenario N times (combat rolls are not deterministic)
    [ValidateRange(1, 50)][int]$Repeat = 1,
    [int]$HeapMb = 1024,
    [switch]$SkipDraw,
    # size of the hidden window in pixels (= size of the PNGs written by h.screenshot); default: portrait phone
    [ValidateRange(240, 4096)][int]$Width = 480,
    [ValidateRange(240, 4096)][int]$Height = 800,
    # OPTIONAL, for trying out a candidate fix without touching any worktree: a directory with game .java
    # sources (normal package directory layout, e.g. <dir>\com\shatteredpixel\...\Goblin.java). They are compiled
    # against the worktree's classpath and put IN FRONT of the worktree's classes for this run only.
    # Only safe for changes that keep the classes binary compatible with the rest of the game
    # (method bodies, added members); the results are NOT the results of the worktree alone.
    [string]$Overlay = ""
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir = Join-Path $root "src"
$scnRoot = Join-Path $root "scenarios"
$scnDir = Join-Path $scnRoot "rpdtest\scenarios"
$started = Get-Date
$report = New-Object System.Collections.ArrayList

function Fwd([string]$p) { return ($p -replace '\\', '/') }
function Q([string]$p) { return '"' + (Fwd $p) + '"' }

# Run a console tool with no window at all; returns @{ Exit; Out; Err }.
function Invoke-Tool([string]$exe, [string]$arguments, [string]$workDir, [int]$timeoutSec) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $exe
    $psi.Arguments = $arguments
    $psi.WorkingDirectory = $workDir
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $p = [System.Diagnostics.Process]::Start($psi)
    $o = $p.StandardOutput.ReadToEndAsync()
    $e = $p.StandardError.ReadToEndAsync()
    if (-not $p.WaitForExit($timeoutSec * 1000)) {
        try { $p.Kill() } catch { }
        return @{ Exit = -999; Out = ""; Err = "timed out after $timeoutSec s" }
    }
    $p.WaitForExit()
    return @{ Exit = $p.ExitCode; Out = $o.Result; Err = $e.Result }
}

function Get-FreeMb { return [int]((Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory / 1024) }

# Fingerprint of the REAL save location of the game; the harness must never touch it.
function Get-RealSaveState([string]$appName) {
    $base = Join-Path $env:APPDATA ".shatteredpixel"
    if (-not (Test-Path -LiteralPath $base)) { return "no .shatteredpixel directory" }
    $lines = New-Object System.Collections.ArrayList
    foreach ($d in @(Get-ChildItem -LiteralPath $base -Force | Sort-Object Name)) {
        [void]$lines.Add(("{0}|{1}" -f $d.Name, $d.LastWriteTimeUtc.Ticks))
    }
    $game = Join-Path $base $appName
    if (Test-Path -LiteralPath $game) {
        foreach ($f in @(Get-ChildItem -LiteralPath $game -Recurse -Force | Sort-Object FullName)) {
            $len = 0
            if (-not $f.PSIsContainer) { $len = $f.Length }
            [void]$lines.Add(("{0}|{1}|{2}" -f $f.FullName, $len, $f.LastWriteTimeUtc.Ticks))
        }
    } else {
        [void]$lines.Add("(no '$appName' directory)")
    }
    return ($lines -join "`n")
}

function Read-KeyValues([string]$path) {
    $h = @{}
    foreach ($l in @(Get-Content -LiteralPath $path -Encoding UTF8)) {
        $i = $l.IndexOf('=')
        if ($i -gt 0) { $h[$l.Substring(0, $i)] = $l.Substring($i + 1) }
    }
    return $h
}

function Out-Line([string]$s) {
    [void]$script:report.Add($s)
    Write-Output $s
}

# ---------------------------------------------------------------- validate

if (-not (Test-Path -LiteralPath $Worktree)) { Write-Output "ERROR: worktree not found: $Worktree"; exit 2 }
$wt = (Resolve-Path -LiteralPath $Worktree).Path.TrimEnd('\')
if (-not (Test-Path -LiteralPath (Join-Path $wt "gradlew.bat"))) { Write-Output "ERROR: no gradlew.bat in $wt"; exit 2 }

$overlayDir = $null
$overlaySources = @()
if ($Overlay -ne "") {
    if (-not (Test-Path -LiteralPath $Overlay)) { Write-Output "ERROR: overlay directory not found: $Overlay"; exit 2 }
    $overlayDir = (Resolve-Path -LiteralPath $Overlay).Path.TrimEnd('\')
    $overlaySources = @(Get-ChildItem -LiteralPath $overlayDir -Recurse -Filter *.java | Sort-Object FullName | ForEach-Object { $_.FullName })
    if ($overlaySources.Count -eq 0) { Write-Output "ERROR: no .java file under the overlay directory $overlayDir"; exit 2 }
}

$jdk = $null
$cands = @()
if ($env:JAVA_HOME) { $cands += $env:JAVA_HOME.TrimEnd('\') }
$jc = Get-Command javac -ErrorAction SilentlyContinue
if ($jc) { $cands += (Split-Path -Parent (Split-Path -Parent $jc.Source)) }
foreach ($c in $cands) { if (Test-Path -LiteralPath (Join-Path $c "bin\javac.exe")) { $jdk = $c; break } }
if (-not $jdk) { Write-Output "ERROR: no JDK found (JAVA_HOME / javac on PATH)"; exit 2 }
$javaExe = Join-Path $jdk "bin\java.exe"
$javacExe = Join-Path $jdk "bin\javac.exe"

# scenarios = .java files in scenarios\rpdtest\scenarios that implement Scenario (other files are helpers).
# Optional markers inside a scenario source file (anywhere, normally in the class comment):
#   EXPECTED-VERDICT: <VERDICT>   the scenario is a self-test of the tool and is SUPPOSED to end with this verdict
#   EXPECTED-REASON: <text>       ... and its one-line reason must contain this text (rest of the line)
#   TIMEOUT-SECONDS: <n>          per-scenario process timeout (overrides -TimeoutSeconds)
# Scenarios named SelfTest_* test the tool itself; they are not part of "all" (use "selftest").
$allScenarios = @()
$expected = @{}
$expectedReason = @{}
$ownTimeout = @{}
foreach ($f in @(Get-ChildItem -LiteralPath $scnDir -Filter *.java | Sort-Object Name)) {
    $text = [System.IO.File]::ReadAllText($f.FullName)
    if ($text -match 'implements\s+Scenario\b') {
        $allScenarios += $f.BaseName
        if ($text -match 'EXPECTED-VERDICT:\s*([A-Z]+)') { $expected[$f.BaseName] = $Matches[1] }
        if ($text -match 'EXPECTED-REASON:[ \t]*([^\r\n]+)') { $expectedReason[$f.BaseName] = $Matches[1].Trim() }
        if ($text -match 'TIMEOUT-SECONDS:\s*(\d+)') { $ownTimeout[$f.BaseName] = [int]$Matches[1] }
    }
}
$gameScenarios = @($allScenarios | Where-Object { $_ -notlike 'SelfTest_*' -and $_ -notlike 'Extra_*' })
$extraScenarios = @($allScenarios | Where-Object { $_ -like 'Extra_*' })
$toolScenarios = @($allScenarios | Where-Object { $_ -like 'SelfTest_*' })
if ($Scenario -eq "list") {
    Write-Output "game scenarios for the reported bugs (-Scenario all):"
    $gameScenarios | ForEach-Object { Write-Output "  $_" }
    Write-Output "game scenarios for incidental findings, not part of 'all' (-Scenario extra):"
    $extraScenarios | ForEach-Object { Write-Output "  $_" }
    Write-Output "tool self-tests (-Scenario selftest):"
    $toolScenarios | ForEach-Object {
        $e = "PASS"; if ($expected.ContainsKey($_)) { $e = $expected[$_] }
        Write-Output ("  {0}   (expected verdict: {1})" -f $_, $e)
    }
    exit 0
}
$selected = @()
if ($Scenario -eq "all") {
    $selected = $gameScenarios
} elseif ($Scenario -eq "extra") {
    $selected = $extraScenarios
} elseif ($Scenario -eq "selftest") {
    $selected = $toolScenarios
} else {
    foreach ($pat in ($Scenario -split ',')) {
        $pat = $pat.Trim()
        if ($pat -eq "") { continue }
        $m = @($allScenarios | Where-Object { $_ -like $pat })
        if ($m.Count -eq 0) {
            Write-Output "ERROR: no scenario matches '$pat'. Available: $($allScenarios -join ', ')"
            exit 2
        }
        foreach ($x in $m) { if ($selected -notcontains $x) { $selected += $x } }
    }
}
if ($selected.Count -eq 0) { Write-Output "ERROR: no scenario selected"; exit 2 }

$leaf = Split-Path -Leaf $wt
$outName = $leaf
$outDir = Join-Path $root "out\$outName"
$wtMark = Join-Path $outDir "worktree.txt"
if ((Test-Path -LiteralPath $wtMark) -and ((Get-Content -LiteralPath $wtMark -Encoding UTF8 | Select-Object -First 1) -ne $wt)) {
    # same folder name, different worktree: keep them apart
    $md5 = [System.Security.Cryptography.MD5]::Create()
    $hash = ([System.BitConverter]::ToString($md5.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($wt.ToLowerInvariant()))) -replace '-', '').Substring(0, 8)
    $outName = "$leaf-$hash"
    $outDir = Join-Path $root "out\$outName"
    $wtMark = Join-Path $outDir "worktree.txt"
}
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
[System.IO.File]::WriteAllText($wtMark, $wt, $utf8NoBom)

$runTag = $outName
if ($overlayDir) { $runTag = "$outName-OVERLAY" }
$runRoot = Join-Path $root ("runs\{0}_{1}_{2}" -f (Get-Date -Format "yyyyMMdd_HHmmss"), $runTag, $PID)
New-Item -ItemType Directory -Force -Path $runRoot | Out-Null
$cacheTmp = Join-Path $root "cache\tmp"
New-Item -ItemType Directory -Force -Path $cacheTmp | Out-Null

Out-Line ("rpd-selftest: worktree={0}" -f $wt)
if ($overlayDir) {
    Out-Line ("rpd-selftest: OVERLAY={0} ({1} source file(s)) - these results are NOT the results of the worktree alone" -f $overlayDir, $overlaySources.Count)
}
Out-Line ("rpd-selftest: scenarios={0}{1}" -f ($selected -join ','), $(if ($Repeat -gt 1) { " x$Repeat" } else { "" }))
Out-Line ("rpd-selftest: run dir={0}" -f $runRoot)

# ---------------------------------------------------------------- 1. build the target worktree

$cpFile = Join-Path $outDir "classpath.txt"
if ($SkipBuild) {
    if (-not (Test-Path -LiteralPath $cpFile)) {
        Out-Line "ERROR: -SkipBuild given but there is no exported classpath for this worktree yet ($cpFile). Run once without -SkipBuild."
        exit 2
    }
    Out-Line ("build: skipped (-SkipBuild), using classpath exported at {0}" -f (Get-Item -LiteralPath $cpFile).LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss"))
} else {
    Out-Line "build: gradle :desktop classes of the target worktree (waits for the machine-wide gradle semaphore if needed) ..."
    $bt = Join-Path $root "tools\build-target.ps1"
    $r = Invoke-Tool "powershell.exe" ("-NoProfile -ExecutionPolicy Bypass -File `"$bt`" -Worktree `"$wt`" -OutDir `"$outDir`"") $root (60 * 60)
    foreach ($l in ($r.Out -split "`r?`n")) { if ($l.Trim() -ne "") { Out-Line ("build: " + $l) } }
    if ($r.Exit -ne 0) {
        if ($r.Err.Trim() -ne "") { Out-Line ("build: stderr: " + $r.Err.Trim()) }
        Out-Line "RESULT: BUILD FAILED - the target worktree does not build; no scenario was run."
        exit 2
    }
}

$cpInfo = Read-KeyValues $cpFile
$cpEntries = @()
foreach ($l in @(Get-Content -LiteralPath $cpFile -Encoding UTF8)) {
    if ($l.StartsWith("cp=")) { $cpEntries += $l.Substring(3) }
}
$missing = @($cpEntries | Where-Object { -not (Test-Path -LiteralPath $_) })
if ($cpEntries.Count -eq 0 -or $missing.Count -gt 0) {
    Out-Line ("ERROR: exported classpath is unusable ({0} entries, {1} missing, e.g. {2}). Run again without -SkipBuild." -f $cpEntries.Count, $missing.Count, ($missing | Select-Object -First 1))
    exit 2
}
$appName = $cpInfo["appName"]
$versionName = $cpInfo["appVersionName"]
$versionCode = $cpInfo["appVersionCode"]
Copy-Item -LiteralPath $cpFile -Destination (Join-Path $runRoot "classpath.txt")

# ---------------------------------------------------------------- 2. compile harness + scenarios

$classes = Join-Path $runRoot "classes"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$gameCp = ($cpEntries | ForEach-Object { Fwd $_ }) -join ';'

function Compile-Java([string]$tag, [string[]]$sources, [string]$extraSourcePath, [string]$dest) {
    if (-not $dest) { $dest = $classes }
    $lines = New-Object System.Collections.ArrayList
    [void]$lines.Add("-encoding"); [void]$lines.Add("UTF-8")
    [void]$lines.Add("-proc:none"); [void]$lines.Add("-nowarn"); [void]$lines.Add("-Xlint:none")
    [void]$lines.Add("-d"); [void]$lines.Add((Q $dest))
    [void]$lines.Add("-cp"); [void]$lines.Add('"' + (Fwd $dest) + ';' + $gameCp + '"')
    if ($extraSourcePath) { [void]$lines.Add("-sourcepath"); [void]$lines.Add((Q $extraSourcePath)) }
    foreach ($s in $sources) { [void]$lines.Add((Q $s)) }
    $argFile = Join-Path $runRoot ("javac-{0}.args" -f $tag)
    [System.IO.File]::WriteAllLines($argFile, [string[]]$lines.ToArray(), $utf8NoBom)
    # -J options are not allowed inside @argfiles; English diagnostics survive any code page
    $r = Invoke-Tool $javacExe ('-J-Duser.language=en -J-Duser.country=US "@' + $argFile + '"') $runRoot 300
    $txt = (($r.Out + "`n" + $r.Err).Trim())
    [System.IO.File]::WriteAllText((Join-Path $runRoot ("javac-{0}.log" -f $tag)), $txt, $utf8NoBom)
    return @{ Exit = $r.Exit; Text = $txt }
}

if ($overlayDir) {
    # candidate-fix overlay: compiled against the worktree's classes, then put in front of them
    $overlayClasses = Join-Path $runRoot "overlay-classes"
    New-Item -ItemType Directory -Force -Path $overlayClasses | Out-Null
    $r = Compile-Java "overlay" $overlaySources $null $overlayClasses
    if ($r.Exit -ne 0) {
        Out-Line "RESULT: OVERLAY DOES NOT COMPILE against this worktree; no scenario was run. javac output:"
        foreach ($l in ($r.Text -split "`r?`n")) { Out-Line ("  " + $l) }
        exit 2
    }
    $gameCp = (Fwd $overlayClasses) + ';' + $gameCp
    $n = @(Get-ChildItem -LiteralPath $overlayClasses -Recurse -Filter *.class).Count
    Out-Line ("overlay: compiled {0} source file(s) into {1} class file(s), placed in front of the worktree's classes:" -f $overlaySources.Count, $n)
    foreach ($s in $overlaySources) { Out-Line ("overlay:   " + $s.Substring($overlayDir.Length + 1)) }
}

$coreSources = @(Get-ChildItem -LiteralPath $srcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName })
$r = Compile-Java "core" $coreSources $null $null
if ($r.Exit -ne 0) {
    Out-Line "RESULT: HARNESS DOES NOT COMPILE against this worktree (the game API the harness relies on changed?). javac output:"
    foreach ($l in ($r.Text -split "`r?`n")) { Out-Line ("  " + $l) }
    exit 2
}

$compileFailed = @{}
$scnSources = @(Get-ChildItem -LiteralPath $scnDir -Filter *.java | ForEach-Object { $_.FullName })
$r = Compile-Java "scenarios" $scnSources $null $null
if ($r.Exit -ne 0) {
    # one scenario that does not compile must not block the others: retry one by one
    foreach ($name in $selected) {
        $one = Compile-Java ("scenario-" + $name) @((Join-Path $scnDir ($name + ".java"))) $scnRoot $null
        if ($one.Exit -ne 0) { $compileFailed[$name] = $one.Text }
    }
}
Out-Line ("compile: harness ok, scenarios ok={0} failed={1}" -f ($selected.Count - $compileFailed.Count), $compileFailed.Count)

# ---------------------------------------------------------------- 3. run

$realSaveBefore = Get-RealSaveState $appName
$nativesMarker = Join-Path $root "cache\natives-ready.txt"
$warmup = -not (Test-Path -LiteralPath $nativesMarker)

$queue = New-Object System.Collections.Queue
foreach ($name in $selected) {
    for ($i = 1; $i -le $Repeat; $i++) {
        $label = $name
        if ($Repeat -gt 1) { $label = "{0}_r{1}" -f $name, $i }
        $queue.Enqueue(@{ Name = $name; Label = $label })
    }
}

$results = New-Object System.Collections.ArrayList
$running = New-Object System.Collections.ArrayList

function New-ResultObject($label, $verdict, $reason, $seconds, $dir) {
    $name = $label -replace '_r\d+$', ''
    $exp = "PASS"
    if ($expected.ContainsKey($name)) { $exp = $expected[$name] }
    $expWhy = ""
    if ($expectedReason.ContainsKey($name)) { $expWhy = $expectedReason[$name] }
    # AsExpected: PASS for a normal scenario; for a tool self-test, exactly the verdict (and reason) it is designed to produce
    $ok = ($verdict -eq $exp) -and ($expWhy -eq "" -or ([string]$reason).Contains($expWhy))
    return New-Object PSObject -Property @{
        Label = $label; Verdict = $verdict; Reason = $reason; Seconds = $seconds; Dir = $dir
        Expected = $exp; ExpectedReason = $expWhy; AsExpected = $ok
    }
}

function Get-ScenarioTimeout([string]$name) {
    if ($ownTimeout.ContainsKey($name)) { return [int]$ownTimeout[$name] }
    return $TimeoutSeconds
}

# One result line. Self-tests that end with the verdict they are supposed to produce are marked as such.
function Format-ResultLine($res) {
    $tag = ""
    if ($res.Expected -ne "PASS" -or $res.ExpectedReason -ne "") {
        if ($res.AsExpected) {
            if ($res.Expected -ne "PASS") { $tag = "(self-test OK: this is the verdict it is designed to produce)  " }
        } else {
            $tag = "(SELF-TEST BROKEN: expected $($res.Expected)"
            if ($res.ExpectedReason -ne "") { $tag += " with reason containing '$($res.ExpectedReason)'" }
            $tag += ")  "
        }
    }
    return ("[{0,-6}] {1,-34} {2,6:N1}s  {3}{4}" -f $res.Verdict, $res.Label, $res.Seconds, $tag, $res.Reason)
}

function Start-Scenario($item) {
    $dir = Join-Path $runRoot $item.Label
    $sandboxHome = Join-Path $dir "sandbox\home"
    New-Item -ItemType Directory -Force -Path $sandboxHome | Out-Null
    $lines = New-Object System.Collections.ArrayList
    [void]$lines.Add("-Xmx${HeapMb}m")
    [void]$lines.Add("-Xms64m")
    [void]$lines.Add("-XX:+UseSerialGC")
    [void]$lines.Add('"-XX:ErrorFile=' + (Fwd (Join-Path $dir "hs_err_pid%p.log")) + '"')
    [void]$lines.Add("-Dfile.encoding=UTF-8")
    [void]$lines.Add("-Djava.awt.headless=true")
    [void]$lines.Add('"-Duser.home=' + (Fwd $sandboxHome) + '"')
    [void]$lines.Add('"-Djava.io.tmpdir=' + (Fwd $cacheTmp) + '"')
    [void]$lines.Add("-Drpd.scenario=" + $item.Name)
    [void]$lines.Add('"-Drpd.rundir=' + (Fwd $dir) + '"')
    [void]$lines.Add('"-Drpd.versionName=' + $versionName + '"')
    [void]$lines.Add("-Drpd.versionCode=" + $versionCode)
    [void]$lines.Add("-Drpd.timescale=" + $TimeScale.ToString([System.Globalization.CultureInfo]::InvariantCulture))
    [void]$lines.Add("-Drpd.fps=" + $Fps)
    [void]$lines.Add("-Drpd.width=" + $Width)
    [void]$lines.Add("-Drpd.height=" + $Height)
    [void]$lines.Add("-Drpd.timeout=" + (Get-ScenarioTimeout $item.Name))
    [void]$lines.Add("-Drpd.parentPid=" + $PID)
    if ($SkipDraw) { [void]$lines.Add("-Drpd.skipdraw=true") }
    [void]$lines.Add("-cp")
    [void]$lines.Add('"' + (Fwd $classes) + ';' + $gameCp + '"')
    [void]$lines.Add("rpdtest.Launcher")
    $argFile = Join-Path $dir "java.args"
    [System.IO.File]::WriteAllLines($argFile, [string[]]$lines.ToArray(), $utf8NoBom)

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $javaExe
    $psi.Arguments = '"@' + $argFile + '"'
    $psi.WorkingDirectory = $dir
    $psi.UseShellExecute = $false
    # no console window, ever
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    foreach ($v in @("JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS", "JDK_JAVA_OPTIONS")) {
        if ($psi.EnvironmentVariables.ContainsKey($v)) { $psi.EnvironmentVariables.Remove($v) }
    }
    $p = [System.Diagnostics.Process]::Start($psi)
    return @{
        Item = $item; Dir = $dir; Proc = $p; Start = (Get-Date)
        OutTask = $p.StandardOutput.ReadToEndAsync(); ErrTask = $p.StandardError.ReadToEndAsync()
    }
}

function Complete-Scenario($run, [bool]$killed) {
    $dir = $run.Dir
    $secs = [math]::Round(((Get-Date) - $run.Start).TotalSeconds, 1)
    $exit = -1
    try { $run.Proc.WaitForExit(5000) | Out-Null; $exit = $run.Proc.ExitCode } catch { }
    $so = ""; $se = ""
    try { if ($run.OutTask.Wait(3000)) { $so = $run.OutTask.Result } } catch { }
    try { if ($run.ErrTask.Wait(3000)) { $se = $run.ErrTask.Result } } catch { }
    [System.IO.File]::WriteAllText((Join-Path $dir "jvm-stdout.txt"), $so, $utf8NoBom)
    [System.IO.File]::WriteAllText((Join-Path $dir "jvm-stderr.txt"), $se, $utf8NoBom)
    $resFile = Join-Path $dir "result.txt"
    if (Test-Path -LiteralPath $resFile) {
        $kv = Read-KeyValues $resFile
        return (New-ResultObject $run.Item.Label $kv["verdict"] $kv["reason"] $secs $dir)
    }
    if ($killed) {
        return (New-ResultObject $run.Item.Label "KILLED" "process was still running $((Get-ScenarioTimeout $run.Item.Name) + 30) s after start and was killed by the runner (no verdict written)" $secs $dir)
    }
    $why = "JVM exited with code $exit without writing a verdict"
    $hs = @(Get-ChildItem -LiteralPath $dir -Filter "hs_err_pid*.log" -ErrorAction SilentlyContinue)
    if ($hs.Count -gt 0) { $why += " (native JVM crash, see $($hs[0].FullName))" }
    if ($se.Trim() -ne "") { $why += ": " + (($se.Trim() -split "`r?`n") | Select-Object -First 3) -join ' | ' }
    return (New-ResultObject $run.Item.Label "DIED" $why $secs $dir)
}

foreach ($name in $selected) {
    if ($compileFailed.ContainsKey($name)) {
        $first = (($compileFailed[$name] -split "`r?`n") | Where-Object { $_ -match 'error:' } | Select-Object -First 1)
        for ($i = 1; $i -le $Repeat; $i++) {
            $label = $name
            if ($Repeat -gt 1) { $label = "{0}_r{1}" -f $name, $i }
            [void]$results.Add((New-ResultObject $label "NOCOMPILE" ("scenario does not compile against this worktree: " + $first) 0 $runRoot))
        }
    }
}

$sem = New-Object System.Threading.Semaphore(2, 2, "rpd_selftest_game")
$deadline = $started.AddMinutes($TotalTimeoutMinutes)
$lowMemSince = $null
try {
    while ($queue.Count -gt 0 -or $running.Count -gt 0) {

        # start as many as allowed
        while ($queue.Count -gt 0 -and $running.Count -lt $MaxParallel) {
            $next = $queue.Peek()
            if ($compileFailed.ContainsKey($next.Name)) { [void]$queue.Dequeue(); continue }
            # first ever run extracts the native libraries into cache\tmp: do that with a single process
            if ($warmup -and $running.Count -gt 0) { break }
            # be gentle with the machine: wait (up to 3 min) for some free memory before adding a game process
            $free = Get-FreeMb
            if ($free -lt 700) {
                if ($null -eq $lowMemSince) { $lowMemSince = Get-Date; Out-Line ("run: only {0} MB free memory, waiting before starting the next game process ..." -f $free) }
                if (((Get-Date) - $lowMemSince).TotalSeconds -lt 180) { break }
                Out-Line ("run: still only {0} MB free after 3 min, starting anyway" -f $free)
            }
            $lowMemSince = $null
            if (-not $sem.WaitOne(0)) { break }
            try {
                $run = Start-Scenario $next
            } catch {
                [void]$sem.Release()
                throw
            }
            [void]$queue.Dequeue()
            [void]$running.Add($run)
        }

        Start-Sleep -Milliseconds 200

        foreach ($run in @($running.ToArray())) {
            $done = $false; $killed = $false; $windowSeen = $false
            if ($run.Proc.HasExited) {
                $done = $true
            } elseif (((Get-Date) - $run.Start).TotalSeconds -gt ((Get-ScenarioTimeout $run.Item.Name) + 30)) {
                try { $run.Proc.Kill() } catch { }
                $done = $true; $killed = $true
            } else {
                # independent guard from outside the JVM: MainWindowHandle is non-zero only for a VISIBLE top-level window
                try {
                    $run.Proc.Refresh()
                    if ($run.Proc.MainWindowHandle -ne [IntPtr]::Zero) {
                        try { $run.Proc.Kill() } catch { }
                        $done = $true; $killed = $true; $windowSeen = $true
                    }
                } catch { }
            }
            if ($done) {
                $res = Complete-Scenario $run $killed
                if ($windowSeen) {
                    $res = New-ResultObject $run.Item.Label "UNSAFE" "the runner saw a VISIBLE top-level window owned by the game process and killed it immediately" $res.Seconds $run.Dir
                }
                [void]$sem.Release()
                $running.Remove($run)
                [void]$results.Add($res)
                Out-Line ("run: " + (Format-ResultLine $res))
                if ($warmup) {
                    $gl = Join-Path $run.Dir "game.log"
                    if ((Test-Path -LiteralPath $gl) -and (Select-String -LiteralPath $gl -Pattern "boot ok" -Quiet)) {
                        [System.IO.File]::WriteAllText($nativesMarker, ("natives extracted and loaded ok at " + (Get-Date).ToString("s")), $utf8NoBom)
                        $warmup = $false
                    }
                }
            }
        }

        if ((Get-Date) -gt $deadline) {
            Out-Line ("run: TOTAL TIMEOUT ({0} min) - killing running scenarios, skipping the rest" -f $TotalTimeoutMinutes)
            foreach ($run in @($running.ToArray())) {
                try { if (-not $run.Proc.HasExited) { $run.Proc.Kill() } } catch { }
                $res = Complete-Scenario $run $true
                [void]$sem.Release()
                $running.Remove($run)
                [void]$results.Add($res)
            }
            while ($queue.Count -gt 0) {
                $it = $queue.Dequeue()
                [void]$results.Add((New-ResultObject $it.Label "NOTRUN" "total timeout reached before this scenario could start" 0 $runRoot))
            }
        }
    }
} finally {
    # never leave a game process (and its hidden window) behind
    foreach ($run in @($running.ToArray())) {
        try { if (-not $run.Proc.HasExited) { $run.Proc.Kill(); $run.Proc.WaitForExit(5000) | Out-Null } } catch { }
        try { [void]$sem.Release() } catch { }
    }
    $sem.Dispose()
}

# ---------------------------------------------------------------- 4. report

$realSaveAfter = Get-RealSaveState $appName
$order = @{}
$idx = 0
foreach ($name in $selected) {
    for ($i = 1; $i -le $Repeat; $i++) {
        $label = $name
        if ($Repeat -gt 1) { $label = "{0}_r{1}" -f $name, $i }
        $order[$label] = $idx; $idx++
    }
}
$sorted = @($results.ToArray() | Sort-Object { $order[$_.Label] })

Out-Line ""
Out-Line "==================== rpd-selftest results ===================="
Out-Line ("worktree: {0}   (version {1}, {2} scenario run(s), {3:N0} s total)" -f $wt, $versionName, $sorted.Count, ((Get-Date) - $started).TotalSeconds)
if ($overlayDir) {
    Out-Line ("OVERLAY : {0}   ({1} game source file(s) replaced for this run - NOT the worktree's own result)" -f $overlayDir, $overlaySources.Count)
}
foreach ($res in $sorted) {
    Out-Line (Format-ResultLine $res)
}

$detailLimit = 140
foreach ($res in $sorted) {
    # a self-test that produced the non-PASS verdict it is supposed to produce needs no detail block
    if ($res.Expected -ne "PASS" -and $res.AsExpected) { continue }
    if ($res.Verdict -eq "PASS") {
        $sumFile = Join-Path $res.Dir "summary.txt"
        if (Test-Path -LiteralPath $sumFile) {
            $kv = Read-KeyValues (Join-Path $res.Dir "result.txt")
            $extra = @(Get-Content -LiteralPath $sumFile -Encoding UTF8 | Where-Object { $_ -match '^\s+note:' })
            $warn = ""
            if ([int]$kv["reported_exceptions"] -gt 0 -or [int]$kv["stderr_exception_lines"] -gt 0) {
                $warn = "PASS, but the game caught/swallowed exceptions during the run: reported=$($kv['reported_exceptions']) stderr=$($kv['stderr_exception_lines']) (see $sumFile)"
            }
            if ($extra.Count -gt 0 -or $warn -ne "") {
                Out-Line ""
                Out-Line ("---- {0}: PASS ----" -f $res.Label)
                if ($warn -ne "") { Out-Line ("  " + $warn) }
                foreach ($l in $extra) { Out-Line $l }
            }
        }
        continue
    }
    Out-Line ""
    Out-Line ("---- {0}: {1} ----" -f $res.Label, $res.Verdict)
    if ($res.Verdict -eq "NOCOMPILE") {
        $name = $res.Label -replace '_r\d+$', ''
        foreach ($l in (($compileFailed[$name] -split "`r?`n") | Select-Object -First 40)) { Out-Line ("  " + $l) }
        continue
    }
    if ($res.Verdict -eq "NOTRUN") { Out-Line ("  " + $res.Reason); continue }
    $sumFile = Join-Path $res.Dir "summary.txt"
    $logFile = Join-Path $res.Dir "game.log"
    if (Test-Path -LiteralPath $sumFile) {
        $lines = @(Get-Content -LiteralPath $sumFile -Encoding UTF8)
        $shown = 0
        foreach ($l in $lines) {
            if ($shown -ge $detailLimit) { break }
            Out-Line ("  " + $l); $shown++
        }
        if ($lines.Count -gt $shown) { Out-Line ("  ... ({0} more lines in summary.txt)" -f ($lines.Count - $shown)) }
        Out-Line ("  summary : " + $sumFile)
    } else {
        Out-Line ("  " + $res.Reason)
        $se = Join-Path $res.Dir "jvm-stderr.txt"
        if ((Test-Path -LiteralPath $se) -and ((Get-Item -LiteralPath $se).Length -gt 0)) {
            Out-Line "  -- JVM stderr (last 30 lines) --"
            foreach ($l in @(Get-Content -LiteralPath $se -Encoding UTF8 | Select-Object -Last 30)) { Out-Line ("  " + $l) }
        }
        if (Test-Path -LiteralPath $logFile) {
            Out-Line "  -- game.log (last 40 lines) --"
            foreach ($l in @(Get-Content -LiteralPath $logFile -Encoding UTF8 | Select-Object -Last 40)) { Out-Line ("  " + $l) }
        }
    }
    if (Test-Path -LiteralPath $logFile) { Out-Line ("  full log: " + $logFile) }
}

$count = @{}
foreach ($res in $sorted) { $count[$res.Verdict] = 1 + [int]$count[$res.Verdict] }
$parts = @()
foreach ($k in @("PASS", "FAIL", "CRASH", "STUCK", "ERROR", "UNSAFE", "DIED", "KILLED", "NOCOMPILE", "NOTRUN")) {
    if ($count.ContainsKey($k)) { $parts += ("{0}={1}" -f $k, $count[$k]) }
}
Out-Line ""
Out-Line ("TOTAL: {0}   {1}" -f $sorted.Count, ($parts -join '  '))

if ($realSaveBefore -eq $realSaveAfter) {
    Out-Line "REAL_SAVE_CHECK: ok - %APPDATA%\.shatteredpixel was not touched during the run"
} else {
    Out-Line "REAL_SAVE_CHECK: WARNING - %APPDATA%\.shatteredpixel changed during the run (may be the user playing another game; the harness itself only writes inside its run dir). Before/after fingerprints are in the run dir."
    [System.IO.File]::WriteAllText((Join-Path $runRoot "real-save-before.txt"), $realSaveBefore, $utf8NoBom)
    [System.IO.File]::WriteAllText((Join-Path $runRoot "real-save-after.txt"), $realSaveAfter, $utf8NoBom)
}
Out-Line ("RUN_DIR: " + $runRoot)

$exitCode = 0
$selfTestOk = 0
foreach ($res in $sorted) {
    if ($res.AsExpected) { if ($res.Expected -ne "PASS") { $selfTestOk++ }; continue }
    if ($res.Expected -ne "PASS" -or $res.ExpectedReason -ne "") { $exitCode = 2; continue }   # a tool self-test did not behave as designed
    if (@("FAIL", "CRASH", "STUCK") -contains $res.Verdict) { if ($exitCode -lt 1) { $exitCode = 1 } }
    else { $exitCode = 2 }
}
if ($selfTestOk -gt 0) {
    Out-Line ("SELF_TESTS: {0} tool self-test(s) ended with the non-PASS verdict they are designed to produce (counted as OK)" -f $selfTestOk)
}
Out-Line ("EXIT_CODE: {0}  (0 = all as expected, 1 = FAIL/CRASH/STUCK present, 2 = tool problem present)" -f $exitCode)
[System.IO.File]::WriteAllLines((Join-Path $runRoot "report.txt"), [string[]]$report.ToArray(), $utf8NoBom)
exit $exitCode
