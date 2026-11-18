# rpd-selftest —— 萝卜地牢「真引擎、隐藏窗口」运行期场景测试

## 标准 Java / Gradle 入口

本目录作为 `:e2e-tests` 模块参与项目构建，测试代码使用 JDK 11 或以上版本编译和运行，游戏模块继续使用原有 Java 兼容级别。无需 PowerShell，也无需手动导出 classpath。以下命令从仓库根目录执行：

```bash
./gradlew :e2e-tests:listScenarios
./gradlew :e2e-tests:e2eTest
./gradlew :e2e-tests:e2eTest -Pscenario=Smoke
./gradlew :e2e-tests:e2eTest -Pscenario=Smoke,SelfTest_StuckTurn
./gradlew :e2e-tests:e2eTest -Pscenario=selftest
./gradlew :e2e-tests:e2eTest -Pscenario=all,selftest
./gradlew :e2e-tests:run --args=Smoke
```

Windows 使用 `gradlew.bat`。`all` 包含全部游戏回归场景（也包含 `Extra_*`），`extra` 只选择 `Extra_*`，`selftest` 选择框架自测。支持逗号组合、`*` 和 `?` 通配符；不支持旧 PowerShell 入口的方括号匹配。未匹配的选择会使命令失败。

每个场景在独立 JVM 中串行运行，使用隐藏窗口、禁用音频，存档、设置和临时目录放在独立沙盒中。进程内检测卡回合与超时；超过场景超时再加 30 秒仍不退出时，外部 Java 运行器强制终止进程。框架自测会核对源码中 `EXPECTED-VERDICT`、`EXPECTED-REASON` 和 `TIMEOUT-SECONDS` 标记，预期的 CRASH / STUCK / FAIL / ERROR / KILLED 视为自测通过。新增场景只需实现 `Scenario`，构建时自动生成场景清单，辅助类不会成为测试。

每次运行输出到 `tools/rpd-selftest/build/rpd-selftest/run-*/`，包含 `TEST-e2e.xml`（JUnit XML）、每个场景的日志、截图与沙盒。任何非预期结果使 Java 入口返回非零退出码，并使 Gradle 任务失败。CI 可收集 `tools/rpd-selftest/build/rpd-selftest/run-*/TEST-e2e.xml`。此任务独立于普通 `test` / `check`，需要显式调用。

Java 调用方使用模块的运行期 classpath（包括构建生成的 `scenarios.properties`）即可调用：

```java
List<SuiteRunner.Outcome> results = SuiteRunner.run(
        "Smoke,D4_WardenSaveReload", Paths.get("build/rpd-selftest"));
boolean passed = results.stream().allMatch(result -> result.passed);
```

`SuiteRunner.run` 返回结构化结果，不会终止调用方 JVM；命令行 `SuiteRunner.main` 会将失败转为退出码。依赖 LWJGL 的真实 OpenGL 环境，隐藏窗口不代表无图形依赖。无显示服务的 Linux CI 需要虚拟显示服务及可用的 OpenGL 实现，例如预先配置 Xvfb/Mesa 后运行任务。Windows/macOS 的运行结果需要在相应系统验证。

全部场景源码均纳入版本控制；仅忽略构建与运行产物。

## 旧 PowerShell 工具入口

以下保留原工具的跨 worktree 构建、overlay、并行和重复运行等用法；它是 Windows 专用入口，其参数与标准 Java 入口独立。

在本机把真实的游戏（目标 worktree 编译出来的原样 class + libGDX LWJGL3 后端）跑起来，窗口从头到尾隐藏、不出声，
用 Java 写的「场景」像真实玩家一样点格子、用物品、等回合，然后对游戏状态下断言。
每个场景一个结论（PASS / FAIL / CRASH / STUCK …），进程退出码反映结果。用来复现 bug、验证修复。

- 工具目录：`tools\rpd-selftest`（在游戏仓库之外，不改任何仓库里被跟踪的文件）
- 工具目录在仓库里的位置：`tools\rpd-selftest`。下文的命令都从仓库根目录执行。运行产生的 `runs\`、`logs\`、`out\`、`cache\` 已被 `.gitignore` 忽略。

## 1. 一分钟上手

```powershell
# 列出所有场景
powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 -Worktree <worktree> -Scenario list

# 对某个 worktree 跑全部「用户报告的 bug」场景（会先构建该 worktree，受 Gradle 信号量约束）
powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 -Worktree <worktree> -Scenario all

# 只跑一部分（名字、逗号分隔、通配符都行），每个场景重复 5 次
# 注意："E*" 会把 Extra_* 也一起选上；只要 E 组（灵爆 / 未激活原型机）请写 "E[0-9]*"
powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 -Worktree <worktree> -Scenario "E[0-9]*,F1*" -Repeat 5

# 全部游戏场景（all + extra）一次跑完（"[A-G]*" 里的 E 已经包含 Extra_*）
powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 -Worktree <路径> -Scenario "[A-G]*,Smoke"

# 工具自测（确认卡回合检测、两条线程的崩溃捕获、超时、强杀都还好用）
powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 -Worktree <路径> -Scenario selftest
```

本机实测：一个场景一个 JVM，2～10 秒；同时最多 2 个游戏进程；`all`（37 个场景）约 85～135 秒（机器忙时更慢；全部 PASS 时也更慢，因为没有场景提前崩掉），另加构建时间（增量构建 15～50 秒）。

## 2. `run.ps1` 参数

| 参数 | 默认 | 说明 |
|---|---|---|
| `-Worktree <路径>` | 必填 | 被测的 worktree（要有 `gradlew.bat`）。 |
| `-Scenario <…>` | 必填 | `all` = 用户报告的 bug 对应的全部游戏场景（除 `Extra_*`、`SelfTest_*` 之外的所有场景）；`extra` = `Extra_*`（搭工具时顺带发现的问题）；`selftest` = `SelfTest_*`（测工具自己）；`list` = 只列名字；其它 = 场景名，可逗号分隔，可用 PowerShell 通配符（`"A*"`、`"E[0-9]*"`、`"[A-G]*,Smoke"`）。**`"E*"` 会连 `Extra_*` 一起选上**，只要 E 组请写 `"E[0-9]*"`。`all` / `extra` / `selftest` / `list` 这四个词只能单独用，不能写进逗号列表（`"E[1-9]*,extra"` 会报 `no scenario matches 'extra'`、退出码 2）；要混着选就用通配符，例如 `"E[1-9]*,Extra_*"`。 |
| `-SkipBuild` | 关 | 不跑 Gradle，直接用上次为这个 worktree 导出的 classpath。**注意**：classpath 里的游戏代码是 `core\build\libs\core-*.jar` 等 jar 包，只有本工具的构建步骤（或等价的 `:desktop:classes`）会更新它们。worktree 的源码或资源变了之后，第一次必须不带 `-SkipBuild`，否则测的是旧代码。 |
| `-Repeat <1..50>` | 1 | 每个场景跑 N 次（战斗掷骰没有按动作固定种子，见第 9 节）。结果行的名字带 `_r1`、`_r2`… |
| `-TimeoutSeconds` | 240 | 单个场景进程的总超时（进程内看门狗判 STUCK；再过 30 秒还在就由 `run.ps1` 强杀，判 KILLED）。场景源码里的 `TIMEOUT-SECONDS: n` 标记优先。 |
| `-TotalTimeoutMinutes` | 90 | 整次调用的总超时；到点杀掉正在跑的，剩下的记 NOTRUN。 |
| `-TimeScale` | 8 | 地牢里的动画加速倍率（只影响动画时长，不影响回合逻辑）。场景里可用 `h.timeScale( 1f )` 改回真实速度。 |
| `-MaxParallel <1..2>` | 2 | 这次调用同时跑几个游戏进程。全机上限由具名信号量 `rpd_selftest_game`（容量 2）保证。 |
| `-HeapMb` | 1024 | 每个游戏进程的堆上限（`-Xmx`）。 |
| `-Fps` | 120 | 隐藏窗口的帧率上限。 |
| `-SkipDraw` | 关 | 不执行绘制，只跑逻辑。默认照常绘制，才能抓到绘制路径上的崩溃。带它运行时 `h.screenshot` 不截图（只记一条 note）。 |
| `-Width` / `-Height` | 480 / 800 | 隐藏窗口的像素尺寸（竖屏手机比例），也就是 `h.screenshot` 存下来的 PNG 的尺寸。要看横屏 / 桌面布局就传 `-Width 1280 -Height 720` 之类。 |
| `-Overlay <目录>` | 无 | 试改：见第 8 节。 |

`run.ps1` 做的事：

1. 构建目标 worktree（除非 `-SkipBuild`）：`tools\build-target.ps1` 调 `gradlew.bat -I gradle\selftest.init.gradle :desktop:rpdSelftestClasspath`，
   用具名信号量 `rpd_gradle_build` 限制全机最多 2 个 Gradle 构建，带 `--no-daemon`、`--console=plain`、
   `JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US`。需要走代理下载依赖时，先设环境变量 `RPD_SELFTEST_PROXY=主机:端口`（例如 `127.0.0.1:7890`）；安卓 SDK 的位置沿用你平时构建用的 `local.properties` 或 `ANDROID_HOME`。
   任务是用 init script 注入的，不改仓库文件；只会在 worktree 里生成 / 更新被 git 忽略的 `build\` 目录。
   classpath 和版本信息导出到 `out\<worktree 目录名>\classpath.txt`。
2. 用 `javac` 把 `src\`（工具本体）和 `scenarios\`（场景）对着这份 classpath 编译到本次运行目录里。某个场景编译不过不会连累别的场景（那个场景记 NOCOMPILE）。
3. 每个场景起一个 JVM 跑；汇总；保证自己起的进程都被杀干净。

## 3. 输出怎么读

控制台输出（也写进运行目录的 `report.txt`）是给另一个 agent 直接读的：

```
==================== rpd-selftest results ====================
worktree: C:\...\<worktree>   (version 0.8.4-base2.4.2, 37 scenario run(s), 91 s total)
[PASS  ] A0_KnockbackControl                   3.0s  all 4 checks passed
[FAIL  ] A1_BlastWaveMobIntoChasm              3.3s  怪被推离了原位 (12,10)（实际位置 (12,10)#222）
[CRASH ] E1_PsiBlastTwice                      4.2s  uncaught exception in render thread, during player input [use ScrollOfPsionicBlast action READ]: java.lang.NullPointerException: ...
...
---- E1_PsiBlastTwice: CRASH ----          ← 每个非 PASS 的场景一段：全部断言、note、异常栈 / 线程栈
  ...
  summary : <运行目录>\E1_PsiBlastTwice\summary.txt
  full log: <运行目录>\E1_PsiBlastTwice\game.log

TOTAL: 37   PASS=9  FAIL=16  CRASH=12
REAL_SAVE_CHECK: ok - %APPDATA%\.shatteredpixel was not touched during the run
RUN_DIR: tools\rpd-selftest\runs\20261112_193432_harness-build_61628
EXIT_CODE: 1  (0 = all as expected, 1 = FAIL/CRASH/STUCK present, 2 = tool problem present)
```

每个场景一行：`[结论] 名字 秒数 一句话原因`。FAIL 的原因是**第一条**不成立的断言；全部断言在下面的明细段和 `summary.txt` 里。
PASS 的场景如果写了 `note`，也会有一小段（实测数值）。

### 结论

| 结论 | 含义 | 是对游戏的结论吗 |
|---|---|---|
| `PASS` | 所有断言成立，没有未捕获异常，没卡住。 | 是 |
| `FAIL` | 至少一条「期望的正确行为」断言不成立（= 复现了 bug）；或者换层时游戏弹了错误窗口。 | 是 |
| `CRASH` | 未捕获异常：渲染线程、「SHPD Actor Thread」或别的游戏线程（真实游戏里就是闪退）。带完整栈。玩家输入的回调里抛出的异常记成 `render thread, during player input [哪一步]`。 | 是 |
| `STUCK` | 卡回合（15 秒且 300 帧内游戏时间和「当前 actor」都不变）、英雄 60 秒没回到可行动状态、渲染线程不出帧、或场景总超时。输出当前 actor 和所有线程的栈。 | 是 |
| `ERROR` | 场景前置条件不成立（`h.require`）、场景自己写错了、或工具出错。**不是**对游戏行为的结论，需要先看场景。 | 否 |
| `UNSAFE` | 安全护栏被触发：窗口变可见 / 得到焦点 / 被最小化、存档路径不在沙盒里、音频不是空实现。进程立刻结束。 | 否 |
| `KILLED` | 进程挂死到自己下不了结论，超时 30 秒后被 `run.ps1` 强杀。 | 视情况 |
| `DIED` | JVM 没写结论就退出了（比如 native 崩溃；会附 `hs_err` 路径和 stderr）。 | 视情况 |
| `NOCOMPILE` | 这个场景对着目标 worktree 编译不过（场景用到的游戏类 / 方法被改了）。 | 否 |
| `NOTRUN` | 整次调用的总超时到了，没轮到它。 | 否 |

### 退出码

- `0`：全部符合预期（普通场景 PASS；自测场景得到它设计好的那个结论）。
- `1`：有 FAIL / CRASH / STUCK（对游戏的负面结论）。
- `2`：工具问题——构建失败、工具本体编译不过、`-Overlay` 编译不过、ERROR / UNSAFE / DIED / KILLED / NOCOMPILE / NOTRUN，或自测场景没有得到设计好的结论。

### 运行目录

每次调用一个目录 `runs\<时间>_<worktree 名>[-OVERLAY]_<PID>\`，不会自动清理：

- `report.txt` 控制台输出的副本；`classpath.txt`；`javac-*.log`（编译输出）
- 每个场景（或每次重复）一个子目录：
  - `summary.txt` 结论、全部断言、note、异常栈 / 线程栈
  - `game.log` 完整日志：每一步输入、每次世界操作、游戏自己的日志（GLog）、每条断言
  - `shots\NN-名字.png` 场景里 `h.screenshot(…)` 存的截图（每张在 `summary.txt` 里有一条 `screenshot: <绝对路径>` 的 note）
  - `result.txt` 机器可读的结论；`jvm-stdout.txt` / `jvm-stderr.txt`；`java.args`
  - `sandbox\` 这次运行的存档、设置、`user.home`

## 4. 安全保证（不打扰正在用电脑的人）

- **窗口永不可见**：`Lwjgl3ApplicationConfiguration.setInitialVisible(false)`，无边框、摆在屏幕外（-20000,-20000）、不全屏（游戏桌面版默认全屏，已预置关闭）。
  另有三层护栏：GLFW `FOCUS_ON_SHOW=false`；游戏进程每一帧检查窗口的 visible / focused / iconified，一旦为真立刻藏回去并以 `UNSAFE` 结束；
  `run.ps1` 从进程外轮询 `MainWindowHandle`，看到可见窗口立刻杀进程。JVM 带 `-Djava.awt.headless=true`（桌面版有 Swing 文本输入框），起进程用 `CreateNoWindow`（没有控制台窗口）。
- **不出声**：`config.disableAudio(true)`（libGDX 的 `MockAudio`，不打开音频设备）；启动时校验 `Gdx.audio` 确实是空实现，不是就 `UNSAFE`。
- **不碰真实存档和设置**：存档、设置、`user.home`、临时目录全部指向运行目录里的沙盒；启动时校验存档路径确实落在沙盒内、沙盒不在 `%APPDATA%` 下，否则 `UNSAFE` 不跑。
  `run.ps1` 在运行前后给 `%APPDATA%\.shatteredpixel` 做指纹比对，输出 `REAL_SAVE_CHECK:`。
- **不弹系统对话框**：不用游戏自带的 `DesktopLauncher`（它对未捕获异常会弹 tinyfd 对话框），自己的 `Launcher` 把未捕获异常写日志并以非零码退出；不初始化手柄。
- **不留僵尸进程**：`run.ps1` 的 `finally` 杀掉自己起的每一个进程；游戏进程自己还盯着 `run.ps1` 的 PID，父进程没了就自行退出；每个进程有进程内看门狗 + 外部强杀两层超时。
  **只杀自己起的进程**，机器上别的 java 进程（其它工作包的 Gradle、别的项目）不碰。
- **资源**：堆上限 1G（`-Xmx1024m`，`-XX:+UseSerialGC`）；同时最多 2 个游戏进程（具名信号量 `rpd_selftest_game`）；空闲内存低于 700 MB 时先等（最多 3 分钟）再起下一个。

没有实测过的护栏：「窗口变可见 / 抢焦点」那几条 `UNSAFE` 路径——要测它就得真的把窗口显示出来，这是不允许的，所以只做了代码走查。

## 5. 场景清单

`P/` = `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`。「基线」= `57c1e4827`（未修复的代码）上的实测结果。

### `-Scenario all`（37 个）

| 场景 | 断言的「正确行为」 | 基线 |
|---|---|---|
| `Smoke` | 工具自检：开局、走路、等待、攻击都能做 | PASS |
| `A0_KnockbackControl` | 对照：平地上冲击波把怪 / 英雄击退 | PASS |
| `A0b_GeyserControl` | 对照：平地上间歇泉把相邻的怪 / 英雄冲开 | PASS |
| `A1_BlastWaveMobIntoChasm` | 冲击波把怪推进相邻的深渊 → 怪坠落死亡 | FAIL |
| `A2_AquaBlastMobIntoChasm` | 水爆晶核（`AquaBlast` 法术，目前只有测试模式能拿到）把怪冲进深渊 → 怪坠落死亡 | FAIL |
| `A2b_AquaBrewMobIntoChasm` | 水爆魔药（`AquaBrew`，玩家在炼金釜里能做的那个）扔到怪脚下，把怪冲进深渊 → 怪坠落死亡 | FAIL |
| `A3_GeyserTrapMobIntoChasm` | 间歇泉陷阱把怪冲进深渊 → 怪坠落死亡 | FAIL |
| `A4_BlastWaveHeroIntoChasm` | 英雄被自己的冲击波推进深渊 → 掉到下一层 | FAIL |
| `A5_GeyserHeroIntoChasm` | 英雄被间歇泉冲进深渊 → 掉到下一层 | FAIL |
| `B0_TeleportScrollControl` | 对照：读传送卷轴后 `hero.pos` 与精灵位置一致、精灵可见 | PASS |
| `B1_TeleportTrap` | 踩传送陷阱（两次）：被传走，精灵画在 `hero.pos` 上、可见；再等一回合仍然一致。1 倍速 | FAIL |
| `B2_GatewayTrap` | 踩虫洞陷阱（两次）：同上。1 倍速 | FAIL |
| `C1_ProjectingGoblin` | 索敌精英哥布林丢完 2 块石头后，隔着 2 格深渊仍然攻击英雄 | FAIL |
| `D1_WardenDoorSnipe` | 英雄在入口房 / 门格上时典狱长不在场；从门格打不到 Boss；不允许「Boss 掉血却没开战」 | FAIL |
| `D2_WardenFightStart` | 踏进战斗区后开战：封层、典狱长 + 8 台 DM-100 + 3 台 DM-175 在场、各占一格 | PASS（回归保护） |
| `D3_WardenSheepFlock` | 战斗区站满羊时开战：Boss 与 11 台原型机仍各占一格 | FAIL |
| `D4_WardenSaveReload` | 开战前 / 开战后存档读档：Boss、原型机、封层状态不变，不会重复开战 | PASS（回归保护） |
| `D5_WardenFallIn` | 英雄从第 9 层的深渊掉进典狱长层（不经过门）：落点合理，战斗能正常开始 | PASS（回归保护） |
| `E0_BlastInactivePrototype` | 最小复现：视野里有 1 台未激活的 DM-100 时读灵爆 → 不崩溃 | CRASH |
| `E0b_ZapInactivePrototype` | 用魔弹法杖把未激活的 DM-100 打死 → 不崩溃 | CRASH |
| `E0c_BurnInactivePrototype` | 未激活的 DM-100 被火烧死（死在 Actor 线程上）→ 不崩溃 | CRASH |
| `E0d_TapInactivePrototype` | 只是点一下相邻的未激活 DM-100（互换位置）→ 不崩溃 | CRASH |
| `E0e_TapInactivePrototypeInFight` | 同上，但在真正开打的典狱长战里：点一下身边的未激活原型机 → 不崩溃 | CRASH |
| `E1_PsiBlastTwice` | 按用户报告摆场（2 敌对 DM-175、1 敌对 DM-100、1 友方 DM-175、典狱长在视野内），连读两次灵爆，英雄存活 | CRASH |
| `E2_PsiBlastTwice_HeroDies` | 同上，第二次的自伤杀死英雄 → 正常死亡 | CRASH |
| `E3_PsiBlastTwice_InactiveLeft` | 场上还躺着 7 台未激活的 DM-100 时读灵爆 | CRASH |
| `E4_PsiBlastTwice_WardenDies` | 第二次灵爆把典狱长炸死 → 正常结束 Boss 战 | CRASH 或 PASS（取决于怪物遍历顺序：22 次里 13 次 CRASH、9 次 PASS，用 `-Repeat`） |
| `E5_PsiBlastTwice_WardenOutOfView` | 典狱长在视野外时连读两次灵爆 | PASS |
| `E6_PsiBlastTwice_NoShield` | DM-175 没有护盾时连读两次灵爆 | CRASH |
| `E7_PsiBlastTwice_WaitBlindness` | 读一次，等失明自然结束后再读一次 | CRASH |
| `E8_PsiBlastTwice_HeroDies_NoWarden` | 典狱长在视野外，第二次的自伤杀死英雄 | PASS |
| `E9_PsiBlast_DoorThenInside` | 第 1 张在门口读，进门开战后读第 2 张 | CRASH |
| `F1_Dm175ShieldAfterBreak` | DM-175 的护盾被打掉后 10 回合内不再获得护盾 | FAIL |
| `F2_Dm175ShieldAfterDecay` | 护盾自然衰减到 0 之后保持为 0 | FAIL |
| `F3_Dm175ShieldAfterReload` | 护盾被打掉后存档读档，读档后也不再获得护盾 | FAIL |
| `G1_ExamineInactivePrototype` | 放大镜看未激活的 DM-100：窗口里的图 = 地图上的图（DM-100 那一帧） | FAIL |
| `G2_ExamineActivePrototype` | 放大镜看已激活的 DM-100 / DM-175：窗口里显示激活后的图 | FAIL |

关于 E1～E9（都走 `PsiBlastLab`）：

- 基线上的崩溃条件是「读灵爆时**视野里**有未激活的原型机，并且它被炸死」。E1 / E2 / E4 / E6 / E7 里第 2 次读之前场上的未激活机体，是第 1 次灵爆把典狱长炸掉 100 血后**游戏自己补生成**的 3 台 DM-100，
  落点随机；英雄的视野只有 8 格，偶尔 3 台全落在视野外（基线上 E6 因此「碰巧 PASS」过 1 次）。所以 `PsiBlastLab.read()` 在读之前会检查：
  场上有未激活的原型机、但一台都不在视野里时，把其中一台挪到英雄看得见的格子上，并在结论摘要里记一条 `note`（`moved one into view: …`）。
  E3 / E9 用的是开战时就在场的未激活机体（不清场），E5 / E8 第 2 次读时场上没有未激活的机体，这一步对它们不起作用。
- 每次读之前会去掉英雄的失明（失明时读不了卷轴），第 2 次读之前还会把英雄的血补满（或按场景设成 1）。E7 是最接近真实操作的时序：它老老实实等 10 回合让失明自己结束，
  等的这段时间里英雄照常挨打，典狱长也会继续激活原型机。
- E5 / E8 在基线上 PASS 不是「没有 bug」，而是崩溃条件不具备：典狱长在视野外 → 第 1 次灵爆炸不到它 → 不会补生成未激活的原型机 → 第 2 次没有东西可崩。
- E4 在基线上时崩时不崩：第 2 次灵爆按 `Dungeon.level.mobs`（`HashSet`）的遍历顺序逐个结算，先轮到典狱长 → 它一死所有原型机被清场 → 不崩；先轮到未激活的原型机 → 崩。

### `-Scenario extra`（8 个，不在用户报告里：搭工具时顺带发现的问题，以及给 warden 工作包靠代码走查提出的两个崩溃补的运行期复现）

| 场景 | 断言的「正确行为」 | 基线 |
|---|---|---|
| `Extra_ActivatedPrototypeAsleep` | 被典狱长激活的 DM-100 在 2 回合内出手 | FAIL（25 次里 18 次；与掷骰有关，用 `-Repeat`） |
| `Extra_AquaBlastAfterReload` | 背包里的水爆晶核（`AquaBlast`）读档后还是水爆晶核 | FAIL（读档后变成水爆魔药 `AquaBrew`） |
| `Extra_GoblinStonesAfterReload` | 石头丢完的哥布林，读档后不会又丢一块 | FAIL |
| `Extra_WardenBossBarAfterReload` | 战斗中读档后 Boss 血条仍然指向典狱长 | FAIL |
| `Extra_WardenSaveWhileActivating` | 典狱长正在激活原型机时存档 → 这个档读得回来 | CRASH（读档必崩） |
| `Extra_WardenAllyLightningKill` | 玩家阵营的 DM-100 隔着 2～3 格用闪电打死典狱长 → 不崩溃，Boss 战正常结束（解封、出口出现、原型机清场） | CRASH（Actor 线程，`DMPrototypeSprite.zap` 空指针） |
| `Extra_WardenSnakeBiteActivation` | 开着「蛇咬」挑战打典狱长：典狱长激活第一台原型机 → 不崩溃 | CRASH（Actor 线程，`ClassCastException`） |
| `Extra_WardenSnakeBiteShockStone` | 开着「蛇咬」挑战，玩家用电击符石激活一台原型机 → 不崩溃 | CRASH（渲染线程，`ClassCastException`） |

挑战的开法（`newGame*` 会把挑战清零）：开局后、换层前写一句 `h.world( "turn on Snake Bite", () -> Dungeon.challenges |= Challenges.SNAKE_BITE );`。

`B1` / `B2` / `G1` / `G2` 会顺手存截图（踩完陷阱之后的画面、放大镜窗口），修没修好可以直接看图：见各自结论段里的 `screenshot:` 行。

### `-Scenario selftest`（13 个，测工具自己）

其中 8 个**设计上**就该得到非 PASS 的结论（得到了算 OK，没得到 `run.ps1` 以退出码 2 报「SELF-TEST BROKEN」）：

| 场景 | 设计好的结论 |
|---|---|
| `SelfTest_StuckTurn` | STUCK（一只永不结束回合的怪） |
| `SelfTest_TotalTimeout` | STUCK（场景总超时） |
| `SelfTest_HardHang` | KILLED（进程挂死，由 `run.ps1` 强杀） |
| `SelfTest_ActorThreadCrash` / `SelfTest_RenderThreadCrash` / `SelfTest_InputCrash` | CRASH（三条抛异常的路径） |
| `SelfTest_FailedCheck` | FAIL |
| `SelfTest_Precondition` | ERROR |
| `SelfTest_HeroKilledByMob` / `SelfTest_SaveReload` / `SelfTest_ChasmJump` / `SelfTest_Potions` / `SelfTest_Screenshot` | PASS（英雄死亡不被误判成卡回合；存读档；确认窗 + 换层；喝 / 扔药水；截图是一张尺寸正确、有内容、随状态变化的 PNG） |

改过 `src\` 之后请跑一遍 `-Scenario selftest`。

## 6. 怎么写一个新场景

在 `scenarios\rpdtest\scenarios\` 里新建一个 `.java` 文件，包名 `rpdtest.scenarios`，类 `implements Scenario`，文件名 = 类名 = 场景名。不需要注册，`run.ps1` 会自己发现。

最小模板（就是现有的 `A1_BlastWaveMobIntoChasm.java`）：

```java
package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;

import rpdtest.H;
import rpdtest.Scenario;

public class A1_BlastWaveMobIntoChasm implements Scenario {

	@Override
	public String describe() {
		return "冲击波法杖把怪推进相邻的深渊：怪应坠落死亡";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );                                        // 开新局：战士、固定种子、21x21 测试场，英雄 300 血站在 (10,10)
		Arena.chasmEastOf( h, 13 );                              // 世界操作：x>=13 整片改成深渊
		Rat rat = Arena.dummy( h, Arena.c( 12, 10 ) );           // 世界操作：在 (12,10) 放一只 500 血、不还手的老鼠
		WandOfBlastWave wand = h.give( new WandOfBlastWave() );  // 世界操作：给一根法杖（自动鉴定）

		h.zap( wand, Arena.c( 12, 10 ) );                        // 玩家输入：用法杖 → 选目标格；返回时游戏又在等输入
		h.frames( 40 );                                          // 让动画播完
		h.rest( 1 );                                             // 玩家输入：等 1 回合

		h.note( "after zap: " + Arena.describe( h, rat ) );      // 写进结论摘要的实测事实
		h.check( h.get( () -> rat.pos ) != Arena.c( 12, 10 ), "怪被推离了原位 (12,10)" );      // 期望行为；不成立 = FAIL
		h.check( !h.get( rat::isAlive ), "怪掉进深渊后死亡" );
		h.check( !h.get( () -> Dungeon.level.mobs.contains( rat ) ), "怪已经从本层的怪物列表里移除" );
	}
}
```

规则：

1. **断言写「期望的正确行为」**，文字写成一句完整的话并带上实测值（FAIL 时这句话就是结果行）。bug 存在时场景 FAIL / CRASH，修好后 PASS。
2. **前置条件用 `h.require`**（场景没搭对 → ERROR，不要让它变成 FAIL）；**事实用 `h.note`**。一个场景至少要执行一条 `check`，否则判 ERROR。
3. **玩家动作只用输入类方法**（`tap` / `walkTo` / `rest` / `use` / `zap` / `read` / `chooseOption` …），它们走和真实点击相同的入口，回合由游戏自己的 Actor 线程推进。
   不要在 `world` 块里直接调 `hero.attack()`、`trap.activate()` 之类的内部方法来「模拟」玩家动作——会绕过回合系统，产生假阳性 / 假阴性。
4. **读游戏状态一律包在 `h.get( () -> … )` 里**（在渲染线程执行）；**直接改状态一律用世界操作**（`spawn` / `setTerrain` / `give` / `world(…)`…），它们只在「英雄可行动、Actor 线程在等待」时执行，否则 ERROR。
   `get` / `world` 的 lambda 里**不能**再调 `h.*` 的实例方法（会报 ERROR：`H methods must be called from the scenario thread`）；静态的 `H.spriteOnCell` / `H.spriteCell` / `H.charSpritesIn` 则只能在 lambda 里用。
5. 一个场景 = 一个 JVM = 一局游戏：`newGame` / `newGameInArena` 只能调一次。
6. 辅助类（不是场景）也放在同一目录，但**源码里不能出现 `implements Scenario` 这几个字**（`run.ps1` 靠它识别场景）。现有的辅助类：`Arena`、`WardenLab`、`PsiBlastLab`、`ExamineLab`、`InactiveDeath`。
7. 命名：`SelfTest_*` 不进 `all`（用 `selftest` 跑）；`Extra_*` 不进 `all`（用 `extra` 跑）；其余都进 `all`。
   新场景一加进目录，别人用通配符（`"A*"`、`"[A-G]*"`）跑时就会自动带上它——加之前先在基线上跑通。
8. 源码里（一般写在类注释里）可以放标记：
   - `TIMEOUT-SECONDS: 60` —— 这个场景自己的进程超时；
   - `EXPECTED-VERDICT: STUCK` 和 `EXPECTED-REASON: 一段文字` —— 只给工具自测用：声明这个场景设计上就该得到这个结论（且原因里包含这段文字）。
9. 文件存成 UTF-8（`javac -encoding UTF-8`）。

## 7. 场景 API 一览（`rpdtest.H`）

返回 `Status` 的方法在「游戏又在等玩家输入」（`READY`）或「英雄已死」（`DEAD`）时返回；等不到就以 STUCK 结束进程。

### 开局 / 换层

| 方法 | 说明 |
|---|---|
| `newGame( HeroClass, seed )` | 开新局，第 1 层是按种子正常生成的下水道。自动把开场故事标成已读、关掉挑战、关掉首次进入的弹窗。 |
| `newGameInArena( HeroClass, seed )` | 开新局，第 1 层直接是测试场 `TestArenaLevel`（21x21 空房间）。职业用 `HeroClasses.WARRIOR` 等。 |
| `gotoLevel( depth, levelClass )` | 直接进第 depth 层并强制用指定的关卡类（`Dungeon.forcedLevelClass` + `InterlevelScene` RETURN，做法同测试模式的楼层跳转器）；`null` = 按正常规则生成。英雄落在入口。到了之后会核对关卡类，不是要的那个 → ERROR。 |
| `saveAndReload()` | 存档并从存档重新载入（`Dungeon.saveAll()` + `InterlevelScene` CONTINUE）。**之后所有游戏对象都是新实例**（见第 9 节）。 |

### 等待

| 方法 | 说明 |
|---|---|
| `waitReady()` / `waitReady( ms )` | 等到游戏在等输入或英雄死亡。15 秒且 300 帧没有任何回合进展 → STUCK（卡回合）；总共超过 60 秒（或给定的 ms）→ STUCK。 |
| `frames( n )` | 等 n 帧（让动画、延迟回调跑完）。 |
| `waitUntil( () -> 条件, maxFrames )` | 每 5 帧在渲染线程看一次条件，成立返回 true，到 maxFrames 还不成立返回 false。等「不占回合」的事（死亡动画淡出等）用它，不要用写死的 `frames( n )`。 |
| `timeScale( f )` | 改地牢里的加速倍率（1 = 真实速度）。断言精灵位置 / 动画状态的场景开局后先调 `h.timeScale( 1f )`。 |
| 字段 `readyTimeoutMs` `stuckMs` `stuckFrames` `renderCallTimeoutMs` | 上面那些阈值（60000 / 15000 / 300 / 45000），场景里可以改。 |

### 玩家输入（与真实点击等价）

| 方法 | 说明 |
|---|---|
| `tap( cell )` | 点一下某格（`GameScene.handleCell`）：空地 = 走过去（可能连走多回合）；敌人 = 攻击；中立 / 友方 = 互动；有选格提示时 = 选目标。点了没任何效果 → ERROR。 |
| `tryTap( cell )` | 同上，但没效果时返回 false 而不是 ERROR。 |
| `walkTo( cell )` | 反复点击直到走到（中途被打断会继续点）。走不到 → ERROR。 |
| `attack( cell )` | = `tap`，攻击那一格上的敌人一次。 |
| `rest( n )` | 原地等 n 回合（与工具栏的等待按钮相同）。英雄死亡时提前返回 `DEAD`。 |
| `use( item, action )` | 用物品的某个动作（`Item.execute`，等价于背包里点物品再点动作按钮）。不等待：之后接 `selectCell` / `chooseOption` / `waitReady`。 |
| `selectCell( cell )` | 给当前的选格提示选定目标。没有选格提示 → ERROR。 |
| `useAndWait( item, action )` / `useOn( item, action, cell )` | 上面两步的组合。 |
| `zap( wand, cell )` `throwAt( item, cell )` `cast( spell, cell )` `read( scroll )` `drink( potion )` | 一步到位的常用写法。已鉴定的有害药水喝之前有确认窗：写成 `h.use( potion, Potion.AC_DRINK ); h.chooseOption( 0 ); h.waitReady();`。 |
| `examine( cell )` | 放大镜查看某格（`GameScene.examineCell`）；之后用 `findWindow` 取窗口。 |
| `chooseOption( index )` | 在最上层的 `WndOptions` 里点第 index 个按钮（0 起）。不等待。 |
| `closeWindows()` | 关掉所有窗口（逐个按返回键）。 |
| `windowNames()` / `findWindow( Class )` | 当前打开的窗口类名 / 指定类型的窗口（返回的对象只能再交给 `get(…)` 读）。 |
| `input( what, Runnable )` | 兜底：在渲染线程调用任意一个「和真实点击等价」的入口。这里面抛出的异常记 CRASH。 |

### 世界操作（只在英雄可行动时执行；执行后自动刷新视野）

| 方法 | 说明 |
|---|---|
| `spawn( mob, cell )` | 在某格生成一只怪。传入 new 好的实例，生成前后都可以设 HP / `state` / `alignment` / 精英 buff（生成后的修改放在 `world(…)` 里）。例：`Goblin g = h.spawn( new Goblin(), c ); h.affect( g, ChampionEnemy.Projecting.class );` |
| `setTerrain( cell, Terrain.X )` / `fillTerrain( x0, y0, x1, y1, Terrain.X )` | 改地形并刷新地图。 |
| `placeTrap( trap, cell )` | 在某格放一个可见的陷阱。 |
| `give( item )` | 给英雄一件物品（先设好等级 / 数量 / 附魔再传进来；自动鉴定）。**返回背包里实际的那个实例**，之后用返回值。 |
| `placeHero( cell )` | 直接把英雄放到某格（不触发陷阱 / 地形效果）。 |
| `setHeroHP( hp, ht )` | 设英雄的生命和生命上限（上限差额记在 `Hero.HTBoost` 里，升级时不会被冲掉）。 |
| `affect( target, BuffClass )` / `affect( target, FlavourBuffClass, 回合数 )` / `detach( target, BuffClass )` | 加 / 去 buff。 |
| `world( what, Runnable )` | 兜底：任意直接改状态的代码。这里面抛出的异常记 ERROR（不是玩家动作，分不清是场景的错还是游戏的错）。 |
| `get( Supplier )` | 在渲染线程读状态并返回。 |

### 断言与记录

| 方法 | 说明 |
|---|---|
| `check( cond, "期望…" )` | 期望行为。不成立记一条失败，场景继续跑，最后结论 FAIL。返回 cond。 |
| `checkFatal( cond, "期望…" )` / `fail( "…" )` | 不成立（或无条件）立刻以 FAIL 结束场景。 |
| `require( cond, "前置条件…" )` | 不成立 → ERROR。 |
| `note( text )` / `log( text )` | 写进结论摘要 / 只写进 `game.log`。 |
| `screenshot( name )` | 把下一帧的画面存成 PNG（`<场景运行目录>\shots\NN-name.png`），返回绝对路径，并记一条 `screenshot: <路径>` 的 note。画面是画进离屏缓冲再读回来的，**窗口仍然不可见**。尺寸 = `-Width` × `-Height`。有窗口开着时也能截。想截动画播完后的样子，先 `frames( n )` / `waitUntil(…)`。带 `-SkipDraw` 时不截图、返回 null。工具不做图像比对，图是给人（或能看图的 agent，用 Read 工具直接读 PNG）看的。 |

### 查询与小工具

`heroAlive()`、`heroPos()`、`depth()`、`now()`（游戏时间，只适合在同一层里做差）、`xy( x, y )`、`cellStr( cell )`、`gameLogContains( text )`（游戏消息里出现过某段文字）、
静态的 `H.spriteOnCell( sprite, cell )`、`H.spriteCell( sprite )`、`H.charSpritesIn( group )`（只能在 `get(…)` 里用）。

`rpdtest.Probe`（开局后自动装好，订阅游戏的 `HitCheckEvent` / `ShieldBreakEvent`）：`Probe.hits()`、`Probe.hitCount()`、`Probe.count( attacker, defender, fromIndex )`、`Probe.shieldBreaks( target )`；
每条 `Hit` 有 `attacker`、`defender`、`magic`、`time`、`attackerPos`、`defenderPos`。用来数「谁在什么时候打了谁」。

`rpdtest.Reflect`：读写游戏里的 private 成员（`Reflect.get( obj, "字段" )` 等）。字段被改名时以 ERROR 结束并提示 `game code changed?`。

辅助类：`Arena`（测试场：`start`、`c(x,y)`、`chasmEastOf`、`dummy`、`describe`）；
`WardenLab`（典狱长关卡：坐标常量 `ENTRANCE (6,16)` / `OUTSIDE (6,13)` / `DOOR (6,12)` / `INSIDE (6,11)` / `BOSS_HOME (6,10)`，`enter`、`walkIn`、`startFight`、`warden()`、`prototypes()`、`inactivePrototypes()`、`move`、`remove`、`describe`、`field()`、`stackedChars()`）。

## 8. `-Overlay`：不动任何 worktree 试一个改法

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools\rpd-selftest\run.ps1 -Worktree <路径> -Scenario "E[0-9]*" -SkipBuild -Overlay <放着游戏 .java 的目录>
```

目录里按正常的包路径放游戏源文件（`<目录>\com\shatteredpixel\shatteredpixeldungeon\...\Xxx.java`）。它们会对着 worktree 的 classpath 编译，
并在这一次运行里排在 worktree 的 class **前面**。只适合保持二进制兼容的改动（改方法体、加成员）；结果行上方会醒目地标出 `OVERLAY`，这**不是** worktree 自己的结果。

`validation\` 里是我用来验证场景本身的几份 overlay（基线源码的私有副本，改动处都标了 `[rpd-selftest trial fix …]`）。
**它们不是提交给任何工作包的修复方案**，没有考虑设计意图、存档兼容和代码风格，只用来证明「断言是可以被满足的、而且确实对着那个 bug」：

| 目录 | 内容 | 用它跑的结果 |
|---|---|---|
| `validation\trial-fixes\src` | 8 个文件，对每个 bug 做了最小试改 | 基线上所有 FAIL / CRASH 的场景全部变成 PASS（`all` + `extra` 共 45 个，45/45） |
| `validation\trial-fix-sprite-only\src` | 只改 `DMPrototypeSprite`（未激活原型机的动画帧） | E 组全部 PASS，G / F 组仍然 FAIL → 崩溃只由这一处引起 |
| `validation\trial-fix-shield-unsaved\src` | 故意不完整：护盾「只给一次」的标记没进存档 | F1 PASS、F3 FAIL → F3 能抓出「读档后又给盾」 |
| `validation\trial-fix-fightflag-unsaved\src` | 故意不完整：「已开战」标记没进存档 | D4 FAIL → D4 能抓出「读档后 Boss / 原型机状态错乱」 |

## 9. 已知限制与坑

**关于工具能下什么结论**

- 它测的是**桌面后端上的游戏逻辑和渲染路径**。安卓特有的东西（触摸、生命周期、真实音频、不同 GPU 驱动）测不到。
- 玩家输入是从「点地图格子（`GameScene.handleCell`）/ 物品动作（`Item.execute`）/ 等待（`Hero.rest`）/ 选项窗按钮 / 放大镜（`GameScene.examineCell`）」这一层进去的，
  不模拟鼠标坐标去点工具栏、背包窗、快捷栏这些控件。所以「按钮本身接错了线」「背包窗里点不动」这类纯 UI 的 bug 测不到。
- 崩溃判定基于「未捕获异常」。游戏自己 catch 住的异常不算 CRASH，但会计数：`summary.txt` 里的 `REPORTED_EXCEPTIONS`（走了 `Game.reportException`）和 `STDERR_EXCEPTION_LINES`（只打印到 stderr）；PASS 但有这类异常时 `run.ps1` 会在明细里提示。
- **画面对不对，自动判定只能通过状态间接断言**（精灵的坐标、可见性、用的哪张贴图的哪几帧、是否挂在场景里）。`h.screenshot` 能把画面存成 PNG 给人看，但工具不做图像比对；
  启动器预置的是小界面（`SPDSettings.interfaceSize( 0 )`）、中文、默认 480x800 竖屏，要看别的布局用 `-Width` / `-Height`（界面档位目前没有开关，需要的话在场景开局前用 `h.world` 改 `SPDSettings`，改完要重建场景才生效，没有实测过）。
- 没有按动作固定随机数：开局种子固定只决定关卡生成；战斗掷骰用的是全局随机数，受帧时序影响；`Dungeon.level.mobs` 是 `HashSet`，遍历顺序每次运行可能不同。
  结论依赖掷骰或遍历顺序的场景请用 `-Repeat` 多跑几次（`E4` 和 `Extra_ActivatedPrototypeAsleep` 就是这样）。写场景时尽量把随机性挤掉（500 血的靶子、2000 血的英雄、等足够多的回合）。
- 游戏自己随机决定的东西（补生成的原型机落在哪一格、怪往哪走）也会让触发条件偶尔不成立。场景里凡是「靠游戏自己把局面摆出来」的地方，都要在读 / 打之前 `note` 一下实际局面
  （`PsiBlastLab` 每次读之前都记「视野里几只怪、其中几台未激活」），这样意外的 PASS 一眼就能看出是「条件没凑齐」还是「真的没问题」。

**时序**

- 默认 8 倍速：回合逻辑不受影响，但动画补间一两帧就播完。「动画还没播完时又发生了别的事」这类 bug（典型：走路补间把刚被传送走的精灵拖回原地）在高倍速下可能偶尔不出现。
  断言精灵位置 / 动画状态的场景要在开局后调 `h.timeScale( 1f )`（`B0` / `B1` / `B2` 就是这样）。
- 等动画相关的条件用 `h.waitUntil(…)`，不要用写死的 `h.frames( n )`（怪物尸体淡出按游戏时间要 3 秒）。

**写场景时最容易踩的**

- `world(…)` / `spawn` / `give` 等只能在英雄可行动时调（否则 ERROR）。刚 `tap` 完如果英雄死了，就不能再做世界操作。
- `tap` / `read` / `zap` 等「输入没有任何效果」会 ERROR（防止场景悄悄什么都没测）。不确定有没有效果时用 `tryTap`。有窗口挡着时输入类方法也会 ERROR，先 `chooseOption` / `closeWindows`。
- `saveAndReload()` 之后，之前拿到的怪、物品、buff、关卡对象全是旧实例：怪要重新从 `Dungeon.level.mobs` 里找，物品用 `Dungeon.hero.belongings.getItem( X.class )` 重新取。
- 失明（`Blindness`）时读不了卷轴——灵爆读完自带失明，连读两张要先 `h.detach( Dungeon.hero, Blindness.class )` 或等它结束（`PsiBlastLab` 里两种都有）。
- 匿名的 `Mob` 子类不能存档（Bundle 不能实例化），带这种怪的场景不要 `saveAndReload()`。
- 同一进程里读档时，静态的 `BossHealthBar` 还留着读档前的 Boss 实例一小会儿；判断血条请用 `BossHealthBar.isAssigned( 当前的 boss 实例 )`。

**和目标 worktree 的耦合**

- 工具本体编译时依赖这个改版特有的类：`events.*`（`HitCheckEvent`、`ShieldBreakEvent`、`EventManager`）、`levels.branches.Branches`、`actors.hero.HeroClasses` 等。
  被测 worktree 如果删改了它们，`run.ps1` 报 `HARNESS DOES NOT COMPILE`（退出码 2），需要改 `src\`。
- `custom.testmode.TestArenaLevel` 和 `Dungeon.forcedLevelClass` 只在 leaf 开发线上有（`nc/*` 分支没有），工具本体用反射访问它们（做法来自 redo-engineer 工作包的私有副本）：
  在没有它们的 worktree 上工具本体照样能编译，但 `newGameInArena` / `Arena.start` 用不了（ERROR，提示改用 `newGame()`），`gotoLevel( depth, 类 )` 不能强制关卡类（到了之后发现不是要的类 → ERROR）。
  对 `new_contents` 线的一个 worktree 导出的 classpath 做过「只编译、不运行」的检查：工具本体 14 个文件编译通过；58 个场景里 25 个能编译，另外 33 个用到了那条线上没有的类
  （典狱长相关的全部场景，以及 `Smoke` 等 3 个直接引用 `TestArenaLevel` 的）→ `NOCOMPILE`。也就是说**现有场景基本只适用于 leaf 线**，`nc/*` 上要另写场景（`newGame()` + 正常生成的关卡）。
- 场景直接引用游戏类：被改名 / 删除 → 那个场景 `NOCOMPILE`；用反射读的私有字段（`Goblin.StoneRemain`、`Goblin.canZap`、`Warden.activating`、`Warden.activatingTurnsLeft`）被改名 → 那个场景 `ERROR`。两种情况都要跟着改场景，不是游戏的问题。
- `-SkipBuild` 用的是上一次构建导出的 jar；源码 / 资源变了就不能带（见第 2 节）。

**PowerShell 5.1**

- `run.ps1` 和 `tools\build-target.ps1` 只用 ASCII 字符（PS 5.1 把不带 BOM 的脚本按 ANSI 读），改它们时别写中文。
- 读带中文的输出前先 `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8`；存控制台副本用 `Out-File -Encoding utf8`（`Tee-Object` 写出来是 UTF-16）。

**其它**

- 运行目录 `runs\`、Gradle 日志 `logs\` 不会自动清理（一次 `all` 约 1.5 MB，含编译出的 class 和每个场景的沙盒存档）。要清理请逐个目录送回收站，不要硬删。
- `cache\` 里是 native 库的解压目录和「native 已就绪」标记；第一次运行会单进程预热。
- 我自己只对 `harness-build` 跑过（搭工具期间不允许碰别的 worktree）。不过 `runs\` 里已经有别的 agent 对典狱长修复分支的 worktree 跑出来的 7 次结果（`runs\*_warden_*`，
  其中 3 次带 Gradle 构建：排队 0 秒、构建 14～51 秒、`BUILD SUCCESSFUL`，场景都正常编译和运行；最近一次是 20:26，用的是工具的最终状态），说明「换一个 worktree」这条路是通的。
- 工具自己出过一次竞态（发生在累计约 800 次场景运行时，已修，之后 480 次没有再出现）：工具在渲染线程遍历场景的成员列表找窗口时，Actor 线程正好往场景里加东西 → `ConcurrentModificationException`，那次记成了 `ERROR`。
  `Group` 的增删和遍历都是 `synchronized(this)` 的，现在工具遍历时也拿同一把锁（`H.membersOf`）。以后再看到工具自己的栈（`rpdtest.H…`）里出异常，先当成工具问题查。
- 别的 agent 可能正在用这个工具：改 `src\`、`run.ps1` 或已有场景之前先想清楚，改完立刻在基线和 `validation\trial-fixes` 上各跑一遍；不要给已有场景改名。
- 没有实测过：`UNSAFE` 护栏的触发路径（见第 4 节）。

## 10. 工作原理与目录

```
run.ps1                        统一入口（ASCII）
tools\build-target.ps1         构建目标 worktree 并导出 classpath（Gradle 信号量纪律）
gradle\selftest.init.gradle    Gradle init script：给 :desktop 追加 rpdSelftestClasspath 任务
src\rpdtest\                   工具本体（14 个类）
  Launcher                     进程入口，代替 DesktopLauncher：隐藏窗口、禁音频、沙盒、看门狗、未捕获异常 → CRASH
  HarnessGame                  ShatteredPixelDungeon 的子类：游戏逻辑一行不改，只加护栏（每帧查窗口）、加速、异常钩子、截图（把一帧画进离屏缓冲）
  HiddenPlatformSupport / BootScene / HarnessLogger   平台桩 / 空的启动场景 / 把游戏日志并进测试日志
  ScenarioRunner               场景线程：等启动 → 跑场景 → 再跑 90 帧（抓延迟爆出来的异常）→ 定结论
  H                            场景 API
  Probe / Reflect / Result / Log / Scenario / HarnessError / CheckFailed
scenarios\rpdtest\scenarios\   场景（58 个：all 37 + extra 8 + selftest 13）与辅助类（5 个）
validation\                    验证场景用的试改 overlay（不是修复方案）
out\<worktree 名>\             每个 worktree 最近一次导出的 classpath.txt
runs\                          每次调用一个运行目录
logs\                          Gradle 构建日志
cache\                         native 库解压目录等
```

线程模型：游戏有渲染线程和「SHPD Actor Thread」（推进回合）。场景代码跑在第三条线程（`rpd-scenario`）上，
所有对游戏的读写都通过 `Gdx.app.postRunnable` 投递到渲染线程执行——玩家输入就是在渲染线程里调用 `GameScene.handleCell` / `Item.execute` / `Hero.rest`，和真实点击的调用栈相同，之后回合由游戏自己的 Actor 线程推进。
「英雄可行动」的判定：当前是 `GameScene` 且没在切场景、英雄活着、`hero.ready`、`hero.curAction == null`、选格器可用、`Actor.current == hero`、Actor 线程处于 WAITING。
任何线程的未捕获异常都由 `Thread.setDefaultUncaughtExceptionHandler` 接住（渲染线程的由 `Launcher.main` 的 try/catch 接住），写完整栈，定 CRASH，`Runtime.halt` 退出。
