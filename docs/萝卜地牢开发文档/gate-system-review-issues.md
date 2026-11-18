# 门系统与循环楼梯审查问题清单

- 审查分支：`luojimen` @ `3822019ec`
- 对比基准：`origin/main` @ `d751dce8a`
- 审查方式：阅读 `git diff`、跟踪移动/开门/传送调用链、`./gradlew :core:compileJava --offline`
- 当前状态：编译通过；本文只记录问题，修复尚未开始
- 重构方案：`gate-system-redesign.md`（特殊门 / 特殊楼梯分离 + 事件驱动）

## 各类型门的实现结论

| 类型 | 普通门 | 楼梯/传送门 |
|---|---|---|
| 双向 | 正确 | 正确 |
| 单向 | 可用但判定过窄，`OPEN_DOOR` 可绕过 | 名不副实，`canUse` 永远 false，等价于 `sealed` |
| 一次性 | 状态变更触发者错误，视觉/站位有问题 | 按 `linkId` 记录，双向楼梯共用身份 |
| 炸药 | 只对 `Bomb` 生效，其他爆炸源不触发 | 同左，只会被 `Bomb` 打开 |
| 条件 | 可用，但只认事件类名 | 可用；`gateDepth` 没有参与判定 |
| 循环 | 会永久卡死在 B 态 | 只在测试关成立；transition 方案不可用 |

---

## P0：必然错误或机制性缺陷

### P0-1 普通门循环类型会永久卡死

- 位置：`levels/features/DoorGates.canPass()`、`onPass()`
- 现象：`index` 0=A(开)、1=B(关)、2=C(开)。从 0 通过一次后 `index` 变成 1，而 1 是关闭；`canPass` 失败不会推进状态，因此永远到不了 2。
- 影响：循环普通门只能通过一次，之后永久不可用。
- 建议：明确规则后重写——要么失败尝试也推进，要么改成按链条/次数推进；如果普通门不支持循环，删掉这条分支。

### P0-2 LevelTransition 上的循环方案不可用

- 位置：`levels/features/LevelTransition`（`gate=CYCLE_ABC`、`cycleChain/cycleSlot`）、`CycleStair`
- 现象：
  - `LevelTransition` 没有同层目的格字段，`gateLinks` 从未使用，同层循环无法表达。
  - 真正走 `activateTransition` 会进入 `InterlevelScene.travel()`，它要求目标层存在同 `linkId`、反方向的 transition；同层循环无法满足，会抛 `IllegalStateException`。
  - `CycleStair` 没有任何调用方，是死代码。
- 影响：这套字段目前只是半成品，不能作为循环楼梯的实际实现。
- 建议：二选一——(A) 删除 transition 上的循环字段，统一用 `CycleStairData + CycleStairs` 并补齐通用落点；(B) 给 `LevelTransition` 增加同层目的格和配套传送逻辑，删除 `CycleStairData`。

### P0-3 一次性普通门的状态会被非玩家行为消耗

- 位置：`levels/Level.pressCell()`、`Level.occupyCell()` 飞行分支、`DoorGates.onPass()`
- 现象：
  - `pressCell` 的 `Terrain.DOOR` 分支把 `Dungeon.hero` 硬编码传给 `onPass`，而 `pressCell` 对所有非飞行角色都会调用，怪物进入也会消耗。
  - 飞行分支传的是真实 `ch`，飞行怪物同样会消耗。
  - `pressCell(int cell)` 是 public 的，物品落到门上也会触发。
- 影响：一次性门、循环门可能被怪物或物品“用掉”。
- 建议：`onPass` 只在真实英雄主动进入时触发；或者明确允许所有角色触发，但必须传真实 `ch` 并统一两个调用点。

### P0-4 一次性楼梯的 linkId 被双向共用

- 位置：`LevelTransition.onUse()`、`regularEntrance()/regularExit()`
- 现象：`regularLinkId(branch, depth)` 同时用于第 N 层的下行楼梯和第 N+1 层的上行楼梯。`usedGates` 按 `linkId` 记录，所以“一次性”实际是“这条连接一次性”。
- 影响：两端都设一次性时，用掉一端会把另一端一起封死；pass 的语义与“每扇楼梯只能用一次”不一致。
- 建议：一次性状态改用 transition 唯一 id（至少包含分支、层、格子、方向），或者明确把语义定义为连接级并更新文案。

### P0-5 被门挡住仍然消耗回合，寻路会反复重试

- 位置：`actors/Char.move()`、`actors/hero/Hero.getCloser()`
- 现象：`Char.move` 在 `gateBlocks` 时直接 return；但 `getCloser` 在 `move(step)` 之后无条件 `spend(...)`。闭着的门在寻路里仍是可通行格，路径会持续穿过它。
- 影响：通过寻路/键盘走向单向门、未开启的炸药门、未满足条件的门时，英雄会每回合撞门并空耗回合，且没有提示。
- 建议：把门检查前移到扣时间之前（`getCloser` 或 `BeforeHeroMoveEvent`），并让当前角色不可通过的门在寻路时视为不可通行。

### P0-6 Hero.act 的兜底分支会把无关原因误报成“门被挡”

- 位置：`actors/hero/Hero.act()` 的 `else if (getTransition(cell) != null)` 分支
- 现象：只要进入 `LvlTransition` 的条件不满足就会打印 `blockedMessage()`，包括敌人靠近、关卡 locked、格子上有植物、26 层出口限制等。
- 影响：点击一扇完全正常的楼梯，只要旁边有敌人，就会提示“这是一扇单向的门”。
- 建议：只有 `canUse(this) == false` 时才提示；其余原因走普通移动或保持原行为。`blockedMessage()` 对 `TWO_WAY` 也应返回中性文案，不能默认成单向。

### P0-7 一次性门封死后英雄站在 SOLID 格，红叉视觉被墙层遮住

- 位置：`DoorGates.onPass()`、`GameScene` 的绘制层级
- 现象：
  - `onPass` 把地形设成 `Terrain.WALL`，而英雄此时正站在该格，格子变为 `SOLID | LOS_BLOCKING`、不可通行。
  - `SealedDoorVisual` 被加入 `Dungeon.level.customTiles`，但 `customTiles` 是 `terrain` 组的子节点，墙层 `walls`/`customWalls` 后绘制，会盖在上面。
- 影响：英雄暂时站在墙里；红叉门贴图很可能不可见，影响怪物寻路和 `openSpace`。
- 建议：使用不可通行但非 `SOLID` 的专用地形，或在英雄离开后再封死；覆盖墙的视觉改放 `customWalls`。

---

## P1：逻辑错误或可被绕过

### P1-1 单向门只检查 Terrain.DOOR，OPEN_DOOR 可绕过

- 位置：`actors/Char.move()`
- 现象：只在 `map[step] == Terrain.DOOR` 时调用 `canPass`。门一旦是 `OPEN_DOOR` 就不再判定。
- 可绕过场景：门上放了物品（`Door.leave` 不会关门）、门格上还有另一个角色、存档时门开着、击退/传送直接改 `pos`。
- 建议：至少同时检查 `OPEN_DOOR`；把判定挂到 `Door.enter` 或统一的进入逻辑上，并带上真实角色。

### P1-2 单向门方向判定是一格而不是一侧

- 位置：`DoorGates.canPass()`、`GateTester.apply()`
- 现象：用 `(ch.pos - 门格) == (fromDx, fromDy)` 判断，只允许精确的那一 Approach 格；`fromDx/fromDy` 默认 `0,0` 时门永久不可通过。
- 建议：用半平面/方向向量判断“哪一侧”，并为未初始化方向的旧数据提供安全默认值（例如默认放行或记录日志）。

### P1-3 单向楼梯名不副实

- 位置：`LevelTransition.canUse()` 的 `ONE_WAY` 分支
- 现象：无条件 `return false`，从任何一侧都不能激活。
- 影响：对楼梯来说 `ONE_WAY` 等价于 `sealed`，但提示文案写“只能从另一侧通过”，会误导玩家。
- 建议：楼梯单向只用 `sealed` 表达；删掉 `GateType.ONE_WAY` 的楼梯分支，或为楼梯实现真正的方向判定并统一文案。

### P1-4 炸药门只响应 Bomb

- 位置：`items/bombs/Bomb.explode()`、`explodeMobs()`、`DoorGates.onExplosion()`
- 现象：只有 `Bomb` 的两个方法调用 `onExplosion`，且两段代码重复。`ExplosiveTrap`、`StoneOfBlast`、`WandOfBlastWave`、爆炸诅咒等其他爆炸源不会开门。
- 影响：如果设计语义是“任何爆炸都能炸开”，实现不完整；如果只允许炸弹，应写进文案并收敛重复代码。
- 建议：接到统一的爆炸入口/事件上。

### P1-5 条件门只认事件类名，gateDepth 未实现

- 位置：`GateEvents.onEvent()`、`DoorGates.onEvent()`、`LevelTransition.gateDepth`
- 现象：条件只是“发生了某个类的事件”，没有参数、目标、层数、次数；`gateDepth` 只在信息面板显示，从不参与判定。
- 影响：`GateType.CONDITIONAL` 的注释与实现不符，实际条件非常粗。
- 建议：改成可带参数的谓词/条件对象；如果保留类名匹配，至少修正文档并删掉误导性的 `gateDepth`。

### P1-6 循环楼梯的跨层/同层实现不可通用

- 位置：`levels/features/CycleStairs.onStep()`
- 现象：
  - 跨层时 `returnPos` 硬编码为 `CycleTestLevel.ENTRANCE_CELL`，换任何关卡落点都是错的。
  - 同层传送直接改 `hero.pos`，没有检查目标格是否可通行、是否被占用；链条状态在传送成功前就推进了。
- 建议：在 `CycleStairData` 中记录落点格，或复用现有 transition 的落点解析；传送前校验 `passable` 与占位。

### P1-7 门状态分散在四处

- 位置：`LevelTransition`、`Level.doorGates`、`Level.cycleStairs`、`Dungeon.usedGates/cycleChains`
- 现象：同一套“门”概念有 4 份状态和 3 套 `GateType` switch，判定、推进、文案各写一遍。
- 影响：任何规则变更都要同步改多处，容易出现两边行为不一致。
- 建议：抽一个 `GateRule`（或每种 `GateType` 一个实现），门和楼梯共用“能否通过/通过后状态/提示文案”；状态只保留一份来源。

### P1-8 Dungeon.setLevel 每次进关都会重置 sealed

- 位置：`Dungeon.setLevel()`
- 现象：非苔藓关卡会把除 `moss:main-3` 之外所有 transition 的 `sealed` 清掉。
- 影响：未来任何合法的单向/封死 transition 都会在读关时被重置，属于隐藏的全局副作用。
- 建议：把旧存档修复做成一次性迁移或版本判断，不要每次进关无条件清理。

### P1-9 吞异常掩盖真实错误

- 位置：`Char.move()`、`Hero.act()`、`Level.pressCell()`、`GateEvents`、`DoorGates.onEvent`、`EventManager`
- 现象：统一的 `try/catch (Exception)` 只写日志；`Char.move` 出错时 `gateBlocks = true`，角色会被永久卡住。
- 影响：逻辑 bug 被转换成“偶尔不能动/门不开”，很难定位。
- 建议：只在真正的边界（事件分发、存档迁移）兜底；移动路径上的异常应暴露出来，或至少记录完整堆栈并明确失败行为。

---

## P2：设计、测试隔离与维护性

### P2-1 测试内容进入正式流程

- `SewerLevel.create()` 在正式主线第 1 层无条件生成测试支线入口。
- `Level.pressCell()` 硬编码 `CycleTestLevel` 和 `Branches.TEST`。
- `Branches.init()` 在生产注册 `TEST` 分支。
- `HeroDefinition` 把 `GateTester`、`CycleTestPortal` 放进测试背包。
- 建议：测试入口由调试开关控制，正式流程不生成测试楼梯 / 不注册测试分支。

### P2-2 循环光圈只增不减

- 位置：`GateTester.apply()`、`Dungeon.setLevel()`
- 现象：把 transition 从 `CYCLE_ABC` 切成其他类型时不会移除 `CycleStairVisual`；`Dungeon.setLevel` 只负责补，不负责删。
- 影响：测试时切换类型后残留光圈。
- 建议：状态切换时同步增删视觉，或每帧按当前 gate 类型重建。

### P2-3 死代码与死字段

- `CycleStair` 整类无调用方。
- `LevelTransition.gateLinks`、`gateIndex` 从未使用；`gateDepth` 只用于显示。
- `CycleTestLevel.returnDepth` 被写入和存档，但回程实际读静态 `pendingReturnDepth`。
- `DoorGates.lastMessageAt` 从未使用。
- `CycleStairVisual(int slot)` 只有测试关使用，`CycleStair.addRing` 的无参构造永远得到 slot=0。
- 建议：修复时一并删除或接入实际逻辑。

### P2-4 WndInfoCell 动态拼文案 key

- 位置：`windows/WndInfoCell`
- 现象：用 `gate.name().toLowerCase() + "_short"/"_desc"` 动态拼 key，依赖所有枚举都有对应文案。
- 影响：新增枚举或改名时容易静默显示 `no_text_found`。
- 建议：把文案 key 放进 `GateType` 或集中映射，编译期可检查。

### P2-5 全限定名过多

- 新增代码大量使用 `com.shatteredpixel...` 全名，同包内也写全名，明显低于项目现有风格。
- 建议：统一改为 import；这属于修复时的顺手清理，可与逻辑修改分开提交。

---

## 注释清理记录（已完成）

已按 AGENTS.md 清理 `luojimen` 新增注释，只改注释、不改逻辑：

- 删除字段/方法名复述型 Javadoc 和 `//`：`LevelTransition`、`DoorGates`、`GateType`、`CycleStairs`、`CycleTestLevel`、`Level`、`Dungeon`、`Bomb` 等。
- 修正错误注释：
  - `GateType.CONDITIONAL` 不再声称“层数要求”。
  - `DoorGates` 循环门注释改为“A/C 开启，B 关闭；通过后推进”，不再声称失败尝试会推进。
  - `CycleTestLevel.returnDepth` 的误导性注释已删除。
  - `CycleStair` 明确标注“当前关卡生成未调用”。
  - `CycleStairData` 的目的地说明改为“同层格或跨层分支/层数”。
- 保留的注释均为“为什么/不变量/Hack”：传送递归保护、寻路打断、`sealed` 的落点用途、旧存档字段兼容、事件回调兜底、测试占位 transition 等。
- 编译验证：`./gradlew :core:compileJava --offline`，`BUILD SUCCESSFUL`。

---

## 建议的修复顺序

1. 先修机制层（P0-5、P0-6、P0-3）：判定点、扣时间、触发者、误报文案。
2. 再修单门类型（P0-1、P0-4、P0-7、P1-1、P1-2、P1-3）。
3. 统一状态模型（P1-7），在此之前不要继续扩展新门类型。
4. 循环门二选一（P0-2、P1-6）。
5. 测试内容隔离与死代码清理（P2-1、P2-3）。

## 需要你确认的设计问题

- 循环门最终采用哪套模型：`CycleStairData + CycleStairs`，还是补齐 `LevelTransition`？
- 一次性门的语义是“每扇 transition 一次”，还是“整条连接一次”？
- 炸药门是否只允许 `Bomb`，还是任何爆炸源都算？
- 条件门是否需要支持参数/层数/次数，还是保留“事件类名”即可？

## 验证计划

- 编译：`./gradlew :core:compileJava --offline`
- 手动场景：
  - 双向门/楼梯正常通行。
  - 单向门从两侧分别进入；门上放物品、门开着、怪物经过时的行为。
  - 一次性门用后封死；怪物经过、物品落地不消耗；英雄不站在墙里。
  - 炸药门被炸弹和其他爆炸源分别命中。
  - 条件门在事件前后分别尝试进入。
  - 循环门 A→B→C→A 完整一轮；同层和跨层；存档读档后状态保持。
