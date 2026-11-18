# rpd-selftest: build the :desktop runtime classpath of a target worktree and export it to <OutDir>\classpath.txt.
# (This file is kept ASCII-only on purpose: Windows PowerShell 5.1 reads BOM-less scripts as ANSI.)
#
# Discipline:
#   - named semaphore rpd_gradle_build (capacity 2): at most 2 Gradle builds machine-wide;
#   - --no-daemon, --console=plain;
#   - JAVA_TOOL_OPTIONS (English diagnostics);
#   - optional proxy: set the environment variable RPD_SELFTEST_PROXY=host:port (e.g. 127.0.0.1:7890)
#     if Gradle has to download dependencies through a proxy;
#   - the Android SDK location is whatever your normal builds use (local.properties or ANDROID_HOME).
# Does not modify any git-tracked file of the target repo: the task is injected with a Gradle init script (-I).
#
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File build-target.ps1 -Worktree <path> -OutDir <dir>
# Exit code: 0 success; 1 build failed; 2 bad arguments / environment.
param(
    [Parameter(Mandatory = $true)][string]$Worktree,
    [Parameter(Mandatory = $true)][string]$OutDir,
    [int]$QueueTimeoutMinutes = 30,
    [int]$TailLines = 40
)

$ErrorActionPreference = "Continue"

$root = Split-Path -Parent $PSScriptRoot
$initScript = Join-Path $root "gradle\selftest.init.gradle"

if (-not (Test-Path -LiteralPath (Join-Path $Worktree "gradlew.bat"))) {
    Write-Output "BUILD_RESULT: ERROR (no gradlew.bat in $Worktree)"
    exit 2
}
if (-not (Test-Path -LiteralPath $initScript)) {
    Write-Output "BUILD_RESULT: ERROR (missing init script $initScript)"
    exit 2
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
# The Gradle task overwrites this file as the last step of a successful build;
# "written after this build started" is how we know it is this build's product.
$cpFile = Join-Path $OutDir "classpath.txt"

if ($env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT = $env:ANDROID_HOME }
# optional proxy for dependency downloads: RPD_SELFTEST_PROXY=host:port
$proxyArgs = @()
if ($env:RPD_SELFTEST_PROXY -match '^\s*([^:\s]+):(\d+)\s*$') {
    $proxyArgs = @("-Dhttp.proxyHost=$($Matches[1])", "-Dhttp.proxyPort=$($Matches[2])", "-Dhttps.proxyHost=$($Matches[1])", "-Dhttps.proxyPort=$($Matches[2])")
}
# javac diagnostics follow the JVM language; force English so they survive the pipe
$env:JAVA_TOOL_OPTIONS = "-Duser.language=en -Duser.country=US"

$logDir = Join-Path $root "logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$name = Split-Path $Worktree -Leaf
$log = Join-Path $logDir ("gradle_{0}_{1}.log" -f $name, (Get-Date -Format "yyyyMMdd_HHmmss_fff"))

# at most 2 builds machine-wide: the same named semaphore as build.ps1
$sem = New-Object System.Threading.Semaphore(2, 2, "rpd_gradle_build")
$waitStart = Get-Date
$got = $sem.WaitOne([TimeSpan]::FromMinutes($QueueTimeoutMinutes))
$waited = [int]((Get-Date) - $waitStart).TotalSeconds
if (-not $got) {
    $sem.Dispose()
    Write-Output ("BUILD_RESULT: ERROR (waited {0}s for semaphore rpd_gradle_build, gave up)" -f $waited)
    exit 2
}
$code = -1
$buildStart = Get-Date
try {
    Push-Location $Worktree
    # with redirected output Java writes bytes in the system ANSI code page (936); decode with the same one
    [Console]::OutputEncoding = [System.Text.Encoding]::GetEncoding(936)
    & .\gradlew.bat "-I" $initScript ":desktop:rpdSelftestClasspath" "-PrpdSelftestOut=$cpFile" --console=plain --no-daemon @proxyArgs 2>&1 |
        ForEach-Object { "$_" } |
        Out-File -FilePath $log -Encoding utf8
    $code = $LASTEXITCODE
    Pop-Location
} finally {
    [void]$sem.Release()
    $sem.Dispose()
}
$elapsed = [int]((Get-Date) - $buildStart).TotalSeconds

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$lines = @(Get-Content -LiteralPath $log -Encoding UTF8)
$fresh = (Test-Path -LiteralPath $cpFile) -and ((Get-Item -LiteralPath $cpFile).LastWriteTime -ge $buildStart.AddSeconds(-2))
$ok = (($lines | Where-Object { $_ -match 'BUILD SUCCESSFUL' }).Count -gt 0) -and $fresh

Write-Output ("GRADLE_LOG: {0}" -f $log)
Write-Output ("GRADLE_QUEUE_WAIT_SECONDS: {0}  GRADLE_BUILD_SECONDS: {1}  GRADLE_EXIT: {2}" -f $waited, $elapsed, $code)
if ($ok) {
    Write-Output ("CLASSPATH_FILE: {0}" -f $cpFile)
    Write-Output "BUILD_RESULT: BUILD SUCCESSFUL"
    exit 0
} else {
    $errors = @($lines | Where-Object { $_ -match '(^|\s)error:|: error |FAILED|Exception|What went wrong' })
    Write-Output "BUILD_RESULT: BUILD FAILED"
    Write-Output "---- error lines ----"
    $errors | Select-Object -First 60 | ForEach-Object { Write-Output $_ }
    Write-Output ("---- last {0} lines ----" -f $TailLines)
    $lines | Select-Object -Last $TailLines | ForEach-Object { Write-Output $_ }
    exit 1
}
