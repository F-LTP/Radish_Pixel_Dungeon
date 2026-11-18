# 门与楼梯系统重构方案（特殊门 / 特殊楼梯）

> 状态：方案主体已落地。本文保留设计决策和历史方案，不作为完整的当前 API 指南；新增或修改门/楼梯逻辑请以 `门系统与CombatRoom使用指南.md` 和现行代码为准。

- 原实现分支：`feat/special-gates`（基于 `luojimen` @ `3822019ec`）
- 旧实现审查记录已归档至 `../历史设计文档/gate-system-review-issues.md`
- 本文描述的设计目标和实施步骤是历史记录，不是当前待办。

## 1. 结论摘要

1. **特殊门和特殊楼梯必须分开建模。** 两者作用对象、判定时机、状态载体、失败反馈都不同，现在共用一个 `GateType` 是混乱的根源。
2. **取消 `GateType` 的六分类。** 只保留三个可组合的规则：`锁定（等事件）`、`单向（单侧手动开启）`、`一次性（用后封死）`；循环只属于特殊楼梯。
3. **事件驱动。** 生物尝试进入门格、英雄尝试激活楼梯、爆炸发生，都发布事件；规则作为订阅者做否决或解锁。核心代码不再按类型写 `switch`。
4. **`LevelTransition` 只保留楼梯拓扑。** `gate/gateOpened/gateDepth/gateIndex/gateEvent/gateLinks/cycleChain/cycleSlot/sealed` 全部移出，特殊楼梯状态改存 `Level.specialStairs`。
5. **炸弹门不是一种门，而是“锁定门 + 爆炸触发”。** 条件门同理，只是触发源不同；这样 `BOMB_ONLY` 和 `CONDITIONAL` 统一成一个“锁”。
6. **单向门只用于解谜特殊房间。** 关闭时绝对不可通行，仅允许在 `fromMask` 指定的邻格手动开启（始终维护 8 bit，实际可为 1~8 个方向）；不做有向寻路，自动寻路不会穿过它。

## 2. 为什么现在混乱

| 维度 | 特殊门 | 特殊楼梯 |
|---|---|---|
| 作用 | 阻止角色进入一个格子 | 阻止英雄激活一次层间/分支传送 |
| 载体 | `Level.doorGates` + `Terrain.DOOR/OPEN_DOOR` | `LevelTransition` 上的 gate 字段 |
| 作用对象 | 任意 `Char`（英雄、怪物、召唤物） | 只有 `Hero` |
| 判定时机 | 移动的每一步（`Char.move`） | 交互/激活（`Hero.handle`、`Level.activateTransition`） |
| 方向语义 | “从哪一侧进入” | “这个端点能不能用” |
| 失败结果 | 停在门外 | 留在原地，不触发传送 |
| 视觉 | 门贴图、红叉、光圈 | 楼梯贴图、环号文案 |
| 持久化 | 随关卡 | 随关卡，且曾额外依赖全局 `usedGates` |

`GateType` 把这些差异压进同一个枚举后，同一个值在两边含义不同：

- `ONE_WAY`：对门是“只允许从某一侧进入”，对楼梯却变成“永远不能激活”，等价于 `sealed`。
- `CYCLE_ABC`：对门没有可用的循环语义（当前实现会卡死在 B 态），对楼梯才成立。
- `SINGLE_USE`：门的“一次性”是格子状态，楼梯的“一次性”是连接/端点状态，持久化方式完全不同。
- `sealed` 是楼梯上的一个额外方向标记，却又和 `GateType.ONE_WAY` 重叠。

所以问题不是“类型还不够多”，而是**把两种领域对象强行合并**。下面的方案按领域拆开，再让规则可组合。

## 3. 新模型

### 3.1 特殊门 `SpecialDoor`

存放位置：`Level.specialDoors : HashMap<Integer, SpecialDoor>`，**按门格 key**。

```java
public class SpecialDoor implements Bundlable {
    int cell = -1;

    // 单向：只允许从“允许的若干邻格”手动开启。
    // fromMask 是完整 8 邻格的位掩码，位序与 PathFinder.NEIGHBOURS8 一致。
    // 始终按 8 位维护：门周围的墙可能出现在任意方向，不能假设只有固定的两堵墙。
    boolean oneWay;
    int fromMask;

    // 一次性：英雄第一次通过后封死
    boolean oneShot;
    boolean sealed;

    // 锁定：triggerId 为空表示不锁
    String triggerId = "";
    boolean unlocked;

    // 条件出现：true 时该格在墙与门之间翻转
    boolean toggle;
    boolean appeared;
}
```

单向门不能用单个 `fromDx/fromDy` 表示。门格周围固定有 8 个邻格，而墙可能出现在任意方向：可能是走廊两端的两堵墙，也可能是转角、斜墙、三面墙，甚至没有墙。因此不能假设“两侧是墙、剩下 6 个”，必须始终维护完整 8 bit。常见情况可能只有 1~6 个可通行邻格，但模型不依赖这个数量。因此：

- 允许集合存为位掩码 `fromMask`，每个 bit 对应 `PathFinder.NEIGHBOURS8` 的一个偏移；`bitFor(dx, dy)` 必须覆盖全部 8 个方向，不能预先裁剪成 6 个。
- 判定用 `SpecialDoors.allowsOpen(fromCell, doorCell)`：把 `fromCell - doorCell` 映射成 bit，与 `fromMask` 按位与。
- 生成时不要手写“面朝西”，而是让门自己计算：
  - `SpecialDoors.sideMask(level, doorCell, seedCell)`：把门格临时当墙，从 `seedCell` 所在的区域做一次泛洪，收集该区域中与门格相邻的所有格子，得到允许邻格掩码。
  - 内容只需要指定“哪一侧是入口”（传一个 `seedCell` 或方向集合）；不规则门、转角门、多格开口、任意方向的墙都能正确得到 1~8 个方向。
- 单向门必须是两个区域之间的有效分隔。如果 `sideMask` 得到 0 个邻格、或得到全部邻格（门根本没有把区域分开），记录配置错误并按普通门处理，避免谜题软锁。
- 旧存档的 `fromDx/fromDy` 迁移成一个 bit；如果旧数据本身只记录了单格，这与新模型兼容。

规则是正交的，可以组合：

| 组合 | 含义 |
|---|---|
| 无字段 | 普通门，不需要 `SpecialDoor` 记录 |
| `oneWay` | 单向门，只能从 `fromMask` 指定的方向手动开启 |
| `oneShot` | 一次性门 |
| `triggerId = "explosion"` | 炸弹门（本质是锁定门） |
| `triggerId = "sewer.lever"` | 条件门 / 拉杆门 |
| `toggle` | 条件出现门：初始是墙，触发器每次命中在墙与门之间翻转 |
| `oneWay + oneShot` | 只能从允许侧开启，且通过一次后封死 |
| 门上的循环 | **不支持**；ABCABC 循环由特殊楼梯表达 |

规则由 `SpecialDoor` 状态和地形共同决定：关闭的单向门、未解锁的锁定门、封死的一次性门都不可通行；打开后的单向门按普通 `OPEN_DOOR` 处理。

### 3.2 特殊楼梯 `SpecialStair`

存放位置：`Level.specialStairs : HashMap<Integer, SpecialStair>`，**按 `transition.cell()` key**。

```java
public class SpecialStair implements Bundlable {
    int cell = -1;

    // 该端点不允许激活（原 sealed / 单向楼梯）
    boolean oneWay;
    String blockedKey = "";

    // 一次性：本端点用过后封死
    boolean oneShot;
    boolean used;

    // 锁定：与门共用同一套触发机制
    String triggerId = "";
    boolean unlocked;

    // 循环楼梯（仅特殊楼梯支持）
    String chain = "";
    int slot = 0;              // 0=A 1=B 2=C
    int destCell = -1;         // 同层落点；跨层时为目标层的落点，-1 表示用目标层入口
    String destBranch = "";    // 非空表示跨层
    int destDepth = 0;
}
```

关键约定：

- `LevelTransition` **不再保存任何门规则字段**。它继续只负责 `linkId/direction/destBranch/destDepth/type/centerCell`。
- 按 `cell` 而不是 `linkId` 建 key。原因：`RegularLevel` 会在生成后改写 `linkId`（`levels/RegularLevel.java:956-974`），而 `cell` 稳定；同时 `regularLinkId` 被连接两端共用，不适合作为“本端点”的身份。
- 存档时 `specialStairs` 随关卡保存；一次性状态不再依赖 `Dungeon.usedGates` 的全局 `linkId` 集合，语义变成“每扇楼梯各自一次性”。

### 3.3 锁定 `GateLock` 与触发注册表

锁定是门和楼梯共用的最小机制，但**不再是一个枚举值**：

```java
public interface GateTrigger {
    String id();                       // 存档用，稳定不变
    boolean matches(GameEvent event);
}

public final class GateTriggers {
    static void register(GateTrigger trigger);
    static GateTrigger byId(String id);   // 未知 id 返回 null，保持锁定并记录日志
}
```

内置触发：

| triggerId | 匹配 |
|---|---|
| `"explosion"` | `ExplosionEvent` |
| `"any_hero_move"` | 测试用，匹配 `HeroMoveEvent` |

内容侧可以注册自己的触发，不再用事件类名字符串硬匹配：

```java
GateTriggers.register(new GateTrigger() {
    public String id() { return "sewer.lever"; }
    public boolean matches(GameEvent e) { return e instanceof LeverPulledEvent; }
});
```

这样：

- 炸弹门 = `SpecialDoor.locked("explosion")`
- 条件门 = `SpecialDoor.locked("sewer.lever")`
- 一次性 = `SpecialDoor.oneShot()`
- 单向 = `SpecialDoor.oneWay(WEST)`

不再需要 `GateType.BOMB_ONLY` / `CONDITIONAL` 的分支代码。

## 4. 事件设计

所有事件放在 `events/`，继承 `GameEvent`，字段用 getter 暴露，遵循 `event-system-usage.md`。

| 事件 | 可取消 | 发布点 | 用途 |
|---|---|---|---|
| `DoorEnterAttemptEvent` | 是 | `Char.move`、`Hero.getCloser`、`Hero.handle` | 单向、锁定、已封死的否决 |
| `DoorEnteredEvent` | 否 | `Level.occupyCell` → `pressCell` | 一次性消耗、进入后的效果 |
| `DoorLeftEvent` | 否 | `Char.move` 离开门格时 | 一次性封死（英雄已离开，不会站在墙里） |
| `ExplosionEvent` | 否 | `Bomb.explode` | 解锁 `"explosion"` 类门/楼梯 |
| `TransitionUseAttemptEvent` | 是 | `Hero.handle`、`Hero.actTransition`、`Level.activateTransition` | 单向、锁定、已用过的楼梯 |
| `TransitionUsedEvent` | 否 | `Level.activateTransition` 成功后 | 一次性封死、循环链推进 |

`DoorEnterAttemptEvent` 至少携带：

```java
Char ch;
SpecialDoor door;
int fromCell;
int toCell;
String blockedMessageKey;   // 否决者填写，供 GLog / WndInfoCell 使用
```

`TransitionUseAttemptEvent` 携带 `Hero hero`、`LevelTransition transition`、`SpecialStair stair`（可为 null）和 `blockedMessageKey`。

**取消契约**：这些事件的订阅者只做“判定 + 填文案”，不得产生位移、扣回合、改地形等副作用。真正的位置变更/封死由事件发布方在事件返回后执行。

### 4.1 `EventManager` 的改动

现在 `EventManager.emit` 硬编码调用 `GateEvents.onEvent`（`events/EventManager.java:106-111`）。改为：

1. 删除硬编码的 `GateEvents`。
2. 支持基类订阅：先按 `event.getClass()` 分发，若未被取消，再分发 `GameEvent.class` 的订阅者。

```java
emitTo(event.getClass(), event);
if (!event.isCancelled()) {
    emitTo(GameEvent.class, event);
}
```

这样锁定规则可以正常写成：

```java
@SubscribeEvent(event = GameEvent.class, priority = -100)
public static void onAnyEvent(GameEvent event) { ... }
```

不需要再维护一个全局特判入口。注意：`GameEvent.class` 的订阅者优先级最低，只有具体事件处理完后仍未取消才执行。

## 5. 特殊门方案

### 5.1 判定流程

```text
Hero.handle(cell):
  如果 cell 是单向门:
      如果相邻且方向允许: curAction = EnterDoor(cell); return true
      如果方向不允许: 显示 blockedMessage，return false
      否则: 走普通 Move（停在门旁，等待再次手动操作）
  否则如果 cell 是其他特殊门，并且 SpecialDoors.blockedFor(hero, cell):
      显示 blockedMessage，return false（不产生动作、不扣回合）
  其余逻辑照旧

Hero.actEnterDoor(cell):
  再次校验相邻与方向
  Door.enter(cell)          // DOOR -> OPEN_DOOR，passable 变为 true
  移动进入该格             // 同一回合完成，避免门悬空打开
  spend 正常移动时间

Char.move(step, travelling):
  如果 travelling 且存在特殊门，并且 blockedFor(this, step):
      sprite 回拉，return（静默，提示由上层负责）

Level.occupyCell(ch) -> pressCell(ch.pos, ...):
  如果该格是特殊门:
      SpecialDoors.onEntered(ch, cell)
      emit DoorEnteredEvent
```

`Char.move` 的硬拦截覆盖击退、传送、怪物移动等绕过 `Hero.handle` 的路径；`Hero.handle` 的预检查负责“不扣回合 + 给提示”。单向门在 `Door.enter` 之后已是 `OPEN_DOOR`，所以 `Char.move` 的拦截不会阻止手动进入。

### 5.2 各规则

**双向门**：不创建 `SpecialDoor`，零开销。

**单向门**：

单向门按“只能从一侧手动开启的门”实现，而不是“每一步做方向过滤的门”。它只用于解谜特殊房间，不承担高频通行，因此可以接受多一次手动操作。

- 关闭时 `passable = false`，对寻路、虚空锁链、传送、击退都表现为墙。
- 英雄在允许的邻格且相邻时，手动点击或按方向键指向门格：`Hero.handle` 校验 `fromMask` 后创建 `HeroAction.EnterDoor`。该动作在同一回合内先 `Door.enter(cell)` 再移动进入，避免只开门却不进入时门一直开着。
- 不在相邻格时点击门：按普通移动处理，英雄会走到门旁；因为门不可通行，不会自动进入，需要再手动操作一次。
- 打开期间它就是普通 `OPEN_DOOR`，任何角色都能通过。
- 英雄离开后 `Door.leave` 关闭门，并重新 `applyLevelFlags` 把 `passable` 置 false；此时英雄已在反侧，无法从反侧重新开启，单向性成立。
- 爆炸、击退、飞行、传送都不能开启它；只有英雄的这次手动开启会调用 `Door.enter`。单向门同时应加入 `SpecialDoors.isIndestructible(cell)`，避免被爆炸烧掉。
- 方向判定用完整的 8 bit `fromMask`，不是单个方向，也不假设墙在固定位置：`fromMask` 可以同时包含多个 bit（例如走廊一侧的 3 个邻格，或转角处的更多邻格），只要英雄所在邻格对应的 bit 被设置就允许开启。掩码由 `SpecialDoors.sideMask(level, doorCell, seedCell)` 在生成时按实际区域计算，遍历全部 8 个邻格。

**一次性门**：

- 进入时 `DoorEnteredEvent`：只有 `ch == Dungeon.hero` 才记为“已通过”，怪物和掉落物不消耗。
- 英雄离开该格时 `DoorLeftEvent`：如果 `oneShot`，先把格上其他角色和物品弹出到相邻空格，再 `sealed = true`，地形改为 `Terrain.LOCKED_DOOR`。
- 弹射优先选英雄前进方向的反侧，避免把怪物推到英雄的落点；相邻格都不可用时退到两格内的空格。找不到落点则本次不封门，只记录日志，避免把角色封进墙里。
- 为什么在离开时封死：英雄还站在格子上时改成不可通行地形会让他站在墙里；`LOCKED_DOOR` 同时解决了寻路和视觉层级问题。
- 封死后由 `blockedFor` 永久拦截；`SealedDoorVisual` 放在 `customTiles` 即可，因为 `LOCKED_DOOR` 不是墙层。

**锁定门（炸弹/条件/拉杆）**：

- `triggerId` 非空且 `!unlocked` 时，`DoorEnterAttemptEvent` 被规则取消并给出对应文案。
- 解锁订阅者收到 `GameEvent` 后，遍历当前关卡中仍锁定的门和楼梯，匹配 `triggerId` 的置 `unlocked = true`，更新地形/视觉，播放提示。
- 为了避免每次伤害事件都扫全部门，`Level` 维护 `ArrayList<SpecialDoor> lockedDoors` / `ArrayList<SpecialStair> lockedStairs`，解锁后移除；通常长度为 0。
- **炸弹门不是“不能被炸毁的门”**：它是锁定门。`Bomb.explode` 在销毁可燃地形前发布 `ExplosionEvent`，订阅者先解锁并把门改成 `OPEN_DOOR`。因为 `OPEN_DOOR` 仍是可燃地形，`Bomb` 的销毁循环要保留一个 `SpecialDoors.isIndestructible(cell)` 查询，避免刚开的门被同一次爆炸烧掉。
- 所有爆炸源统一走 `Bomb.explode`（炸弹、ConjuredBomb、ExplosiveTrap、StoneOfBlast、Explosive 诅咒），不再只认 `Bomb` 自己。
- 当前匹配只看事件类型，不看爆炸格：本层任意位置的爆炸都会解锁全部 `explosion` 门。若内容需要“必须炸在门旁”，应在触发器里比较 `ExplosionEvent.getCell()` 与门格的距离。

**条件出现门（墙 ↔ 门）**：

- `toggle` 与 `triggerId` 一起使用；初始 `appeared = false`，该格是普通 `WALL`，`solid/passable` 与墙完全一致。
- 每次匹配的 `GameEvent` 翻转一次：`false → true` 写 `Terrain.DOOR`（再按 `oneWay` 等规则应用 `passable`）；`true → false` 先清空格上角色和物品，再写 `Terrain.WALL`。
- 用 `SpecialDoors.addToggle(level, cell, triggerId)` 注册；它会在设置规则后重新应用一次地形，避免在原有门格上忘记恢复初始墙态。
- 存读档保存 `toggle/appeared`，`applyLevelFlags` 负责按状态重建墙或门。
- 触发器必须能被重复命中（`signal:<id>`、`explosion` 等）。`any_hero_move` 会导致英雄每走一步就翻转，不适合做出现门。
- `toggle` 与 `locked` 共用 `triggerId`，不能同时表达“先解锁再出现”；需要分阶段时用两个门或一个外部信号源。

**循环门**：普通门不支持；ABCABC 循环由特殊楼梯/传送门表达。这与“循环楼梯即可”的需求确认一致，`luojimen` 的普通门循环分支已删除。

### 5.3 寻路与不可通行状态

单向门、锁定门、已封死的一次性门在关闭/未解锁时都属于“绝对不可通行”，与方向无关：只有英雄在 `fromMask` 允许的邻格手动开启单向门的那一次会临时打开它。因此不需要有向寻路，`Dungeon.level.passable` 就是唯一事实来源。

- `SpecialDoors.applyLevelFlags(level)` 在以下时机把应当阻断的门格 `passable` 置 false：
  - 关卡房间绘制完成、`specialDoors` 全部注册之后；
  - `Level.buildFlagMaps()` 之后；
  - 读档恢复 `specialDoors` 之后；
  - `Door.leave` 关门之后；
  - 门被封死、重新锁定时。
- 新增 `SpecialDoor` 时也可以立即设置该格，避免生成阶段到统一应用之间被寻路使用。
- 门被打开（`Door.enter` → `OPEN_DOOR`）时，`Level.set` 会把该格恢复为可通行；离开关门后再由 `applyLevelFlags` 重新置 false。单向门的“关闭/打开”因此直接由地形表示，`Char.move` 只在 `DOOR` 且规则要求关闭时拦截。
- 一次性门：未使用时正常可通行；英雄离开后封死，`passable` 置 false。状态随关卡存档，读档后重新应用。
- 锁定门：未解锁时不可通行；`unlocked = true` 后重新应用，恢复普通门行为。
- 虚空锁链、传送、怪物寻路等所有直接读 `level.passable` 的系统都会自动看到“关闭的门是墙”，不需要额外特判，也不需要审计直接位移效果。
- 纯传送/魔法位移如果绕过 `passable` 直接改坐标，必须自行避免落到封死/锁定格；这是它们原本就应遵守的落点校验。

边界情况：

- 英雄正站在已打开的门格上时，该格是 `OPEN_DOOR`，可通行；离开时关门并重新阻断。
- 门打开期间其他角色可以正常通过；关门条件是格子上没有角色（沿用 `Door.leave`）。
- 单向门只用于解谜房间，不保证自动寻路能穿过；这是明确的交互约定。
- 存档：恢复 `specialDoors` 后先 `applyLevelFlags`，再进行寻路/AI。

### 5.4 文案

- `SpecialDoor.blockedMessageKey()` 根据当前阻断的规则返回固定 key，不再用 `gate.name().toLowerCase()` 动态拼。
- `WndInfoCell` 只读取门/楼梯对象的摘要，不依赖枚举齐全。

## 6. 特殊楼梯方案

### 6.1 判定流程

```text
Hero.handle(cell):
  t = level.getTransition(cell)
  如果 t 存在:
      emit TransitionUseAttemptEvent(hero, t, specialStair)
      如果被取消:
          显示 blockedMessage，return false（不产生动作、不扣回合）
      如果环境条件不满足（敌人靠近、locked、植物、26 层限制）:
          走普通移动，不再打印门文案
      否则:
          curAction = LvlTransition

Hero.actTransition / Level.activateTransition:
  再次发 TransitionUseAttemptEvent 作为最终保护
  成功后发 TransitionUsedEvent
```

这修掉现在 `Hero.act` 的兜底分支把所有失败原因都显示成“门被挡”的问题：**规则否决才提示门文案，环境原因不提示**。

### 6.2 各规则

**普通楼梯**：不创建 `SpecialStair`，行为与上游 SPD 一致。

**单向楼梯（原 `sealed`）**：

- 从 `LevelTransition.sealed` 迁移到 `SpecialStair.oneWay`。
- 苔藓关卡“所有上行楼梯只能下”的逻辑从 `Dungeon.switchLevel` 的全局清理中移走，改为在苔藓关卡生成时给对应 transition 标记 `SpecialStair.oneWay`。
- 删除 `Dungeon.setLevel` 里“每次进关重置 sealed”的旧存档修复 hack（`Dungeon.java:557-570`），改为一次性迁移。

**一次性楼梯**：

- `TransitionUsedEvent` 里置 `used = true`，本端点以后 `canActivate = false`。
- 状态随关卡保存，删除 `Dungeon.usedGates`。
- 语义明确为“每扇楼梯各自一次性”，不再出现“同一连接两端共用 linkId 被一起封死”的问题。

**锁定楼梯（炸弹/条件）**：与门共用 `triggerId` 和 `GateTriggers`；`TransitionUseAttemptEvent` 被取消时提示对应文案。

**循环楼梯**：

- 明确它是“踩上去就传送”的特殊格，和普通楼梯的“交互后激活”是两种操作。不要硬塞进 `activateTransition`。
- 同层：目标 `destCell`，直接走一个共享的 `teleportWithinLevel` 助手；传送前校验目标格可通行且无人占用。
- 跨层：`destBranch/destDepth + destCell`，通过 `InterlevelScene.Mode.RETURN` 切场景；`returnPos = destCell`，`-1` 时让 `returnTo` 使用目标层入口。删除硬编码的 `CycleTestLevel.ENTRANCE_CELL`。
- 链状态：`Dungeon.gateChains : Map<String,Integer>`（由现有 `cycleChains` 改名/收敛）。只有传送目标校验通过后才推进；先推进再检查失败会留下错误状态。
- 防递归：保留 `teleporting` 标志，避免 A→B→C→A 连环触发。
- 视觉：`CycleStairVisual` 由 `SpecialStair` 生命周期管理，增删成对，切换规则时移除旧光圈。

### 6.3 与普通楼梯配对系统的关系

旧文档 `stair_pair_id_system.md` 描述的是楼梯配对与循环设计，属于已被现行楼梯模型取代的历史设计，不应作为当前实现依据。它与本方案的特殊楼梯 A/B/C 链不是同一机制；当前 `CycleState` 没有调用方。

## 7. 使用示例

```java
// 普通门：什么都不用写

// 单向门：允许从 seedCell 所在的整片区域手动开启
// sideMask 会遍历全部 8 个邻格，收集该区域里与门格相邻的格子
SpecialDoors.add(level, cell).oneWayFromSide(seedCell);

// 也可以直接指定多个允许邻格
SpecialDoors.add(level, cell).oneWayFromCells(
        cell - 1, cell - level.width() - 1, cell + level.width() - 1);

// 一次性门：英雄通过后封死
SpecialDoors.add(level, cell).oneShot();

// 炸弹门
SpecialDoors.add(level, cell).locked("explosion");

// 拉杆门
SpecialDoors.add(level, cell).locked("sewer.lever");

// 一次性楼梯
SpecialStairs.add(level, transition).oneShot();

// 单向楼梯
SpecialStairs.add(level, transition).oneWay();

// 循环楼梯：A 同层到 B，B 同层到 C，C 跨层到 test:2
SpecialStairs.addCycle(level, cellA, "cycletest", 0).destCell(cellB);
SpecialStairs.addCycle(level, cellB, "cycletest", 1).destCell(cellC);
SpecialStairs.addCycle(level, cellC, "cycletest", 2).dest("test", 2, destCell);
```

## 8. 文件改动清单

### 新增

| 文件 | 内容 |
|---|---|
| `levels/features/SpecialDoor.java` | 门数据 + bundle |
| `levels/features/SpecialDoors.java` | 注册、状态查询、进入/离开、寻路过滤 |
| `levels/features/SpecialStair.java` | 楼梯数据 + bundle |
| `levels/features/SpecialStairs.java` | 注册、激活判定、一次性、循环传送 |
| `levels/features/GateTrigger.java` | 触发接口 |
| `levels/features/GateTriggers.java` | 触发注册表 |
| `events/DoorEnterAttemptEvent.java` | 进入门格前的可取消事件 |
| `events/DoorEnteredEvent.java` | 已进入门格 |
| `events/DoorLeftEvent.java` | 离开门格 |
| `events/ExplosionEvent.java` | 爆炸中心 + 半径 |
| `events/TransitionUseAttemptEvent.java` | 激活楼梯前 |
| `events/TransitionUsedEvent.java` | 激活成功 |

### 修改

| 文件 | 改动 |
|---|---|
| `events/EventManager.java` | 删除 `GateEvents` 硬编码；支持 `GameEvent.class` 基类分发 |
| `levels/Level.java` | `doorGates/cycleStairs` → `specialDoors/specialStairs`；存档；`pressCell` 的 DOOR 分支改用真实 `ch`；移除测试传送门硬编码 |
| `actors/Char.java` | `move` 用 `SpecialDoors` 硬拦截；删除 `DoorGates` 直接依赖 |
| `actors/hero/Hero.java` | `handle` 处理单向门的手动开启、事件预判门/楼梯；`act` 增加 `EnterDoor` 分支；`getCloser` 被拦时不 `spend`；`actTransition` 最终保护；删除误报文案分支 |
| `actors/hero/HeroAction.java` | 新增 `EnterDoor`，用于“校验方向 → 开门 → 立即进入”的原子动作 |
| `levels/features/Door.java` | 删除 `DoorGate/GateType` 引用；`leave` 时通知 `SpecialDoors` 并重新应用 `passable` |
| `items/bombs/Bomb.java` | 统一发 `ExplosionEvent`；删除门类型判断和重复的 NEIGHBOURS9 代码；保留不可摧毁查询 |
| `levels/LevelTransition.java` | 删除 `gate/gateOpened/gateDepth/gateIndex/gateEvent/gateLinks/cycleChain/cycleSlot/sealed` 及 `canUse/onUse/blockedMessage` |
| `Dungeon.java` | 删除 `usedGates`；`cycleChains` → `gateChains`；删除 `setLevel` 的 sealed/光圈补丁 |
| `windows/WndInfoCell.java` | 读取 `SpecialDoor/SpecialStair`，不再拼枚举 key |
| `levels/CycleTestLevel.java` | 改用 `SpecialStair` 建循环楼梯 |
| `custom/testmode/GateTester.java` | 改用新 API，删除 `GateType` 轮换 |
| `levels/rooms/special/MossExitRoomInMain.java` 等苔藓房间 | 在生成时标记单向，不再依赖 `Dungeon.setLevel` |
| `levels/SewerLevel.java`、`levels/branches/Branches.java` | 测试入口/测试分支改为调试开关（与门重构分开提交也可以） |

### 删除

- `levels/features/GateType.java`
- `levels/features/DoorGate.java`
- `levels/features/DoorGates.java`
- `levels/features/GateEvents.java`
- `levels/features/CycleStair.java`
- `levels/features/CycleStairData.java`
- `levels/features/CycleStairs.java`

`SealedDoorVisual`、`CycleStairVisual` 保留，归属到新的门/楼梯类。

## 9. 存档迁移

分支尚未发布，建议只做“读旧字段、不写旧字段”的一次性兼容：

| 旧数据 | 新数据 |
|---|---|
| `Level.door_gates` 的 `TWO_WAY` | 丢弃 |
| `ONE_WAY` | `SpecialDoor.oneWay = true`；`fromDx/fromDy` 映射成 `fromMask` 的单个 bit |
| `SINGLE_USE` | `SpecialDoor.oneShot = true` |
| `BOMB_ONLY` | `SpecialDoor.locked("explosion")` |
| `CONDITIONAL` | `SpecialDoor.locked(旧事件类名对应的 triggerId)`；无法映射时保持锁定并记日志 |
| `CYCLE_ABC` | 丢弃（门不再支持循环） |
| `Level.cycle_stairs` | `SpecialStair` 的 chain/slot/destCell 或 destBranch/destDepth |
| `LevelTransition.gate_*` | 迁移到按 cell 的 `SpecialStair`；旧字段读取后忽略 |
| `LevelTransition.sealed` | `SpecialStair.oneWay = true` |
| `Dungeon.used_gates` | 按 linkId 尽量映射到对应 `SpecialStair.used`；映射不到就忽略 |
| `Dungeon.cycle_chains` | 改名读取到 `gateChains` |

迁移完成后，新的存档只写 `special_doors/special_stairs/gate_chains`。`Game.versionCode` 或独立的 `gate_schema` 字段用于只迁移一次。

## 10. 实施顺序

1. **基础设施**：新增 `SpecialDoor/SpecialStair/GateTrigger`、六个事件、`EventManager` 基类分发；先不删旧类。
2. **接入门**：`Char.move`、`Hero.handle/getCloser`、`Level.occupyCell/pressCell/Door.leave`、`modifyPassable`；迁移单向/一次性/锁定。
3. **接入楼梯**：`Hero.handle/actTransition`、`Level.activateTransition`；迁移单向/一次性/锁定；删除 `LevelTransition` 的门字段。
4. **重做循环楼梯**：`SpecialStairs` 承接同层/跨层，删除 `CycleStairData/CycleStairs/CycleStair`，修硬编码落点和光圈生命周期。
5. **清理**：删除 `GateType/DoorGate/DoorGates/GateEvents`、`Dungeon.usedGates`、`Dungeon.setLevel` 补丁、死字段和全限定名。
6. **测试隔离**：`CycleTestLevel`、`GateTester`、`SewerLevel` 测试入口、生产 `TEST` 分支。

每一步都用 `./gradlew :core:compileJava --offline` 验证；机制层（步骤 1-3）通过后再动循环楼梯。

## 11. 验证清单

编译：

```bash
./gradlew :core:compileJava --offline
```

手动场景：

- 普通门/楼梯：与上游行为一致。
- 单向门：关闭时寻路/虚空锁链/怪物都把它当墙；在 `fromMask` 允许的任意邻格手动点击或按方向键可以打开并进入；同一扇门有多个允许邻格时（1~8 个）每一个都能独立开启；从掩码外的邻格操作给出提示且不打开；离开后关门并重新不可通行；爆炸、击退、飞行、传送都不能打开它。
- 方向掩码：墙在任意方向（走廊两端、转角、斜墙、三面墙）时都能正确收集允许邻格；不出现“只认某个固定方向”导致门不可用；0 个或全部邻格的异常配置会记日志并按普通门处理。
- 一次性门：英雄通过后，离开时封死；怪物先经过不消耗；物品落地不消耗；英雄不会站在墙里；红叉可见。
- 炸弹门：炸弹、爆炸陷阱、爆破石、爆炸诅咒都能打开；开门后不被同一次爆炸烧毁。
- 条件门：注册的触发事件发生前不能进，发生后能进；未注册的 triggerId 只记日志、不崩溃。
- 一次性楼梯：从本端用过后本端不可用，另一端不受影响；存读档后状态保持。
- 单向楼梯：苔藓上行楼梯不可用；主线楼梯不受影响；存读档后仍然不可用。
- 循环楼梯：A→B→C→A 完整一轮；同层落点被占用/不可通行时有明确反馈且链状态不推进；跨层后落点正确；存读档后链状态保持。
- 寻路：关闭的门不会进入路径；不会出现每回合撞门空耗。
- 自动通行：点击单向门后方的远处目标不会自动开路；这是单向门的预期行为。
- 阻断状态：锁定门、封死的一次性门在 `level.passable` 中不可通行，虚空锁链等直接读 `passable` 的系统给出 `cant_reach`；存读档后保持。

## 12. 需要确认的设计选择

下面是本方案的默认决定，若与预期不同请在审核时指出：

1. **一次性语义**：默认“每扇门/每扇楼梯各自一次性”，不是“整条连接一次性”。这样状态可以随关卡保存，不再需要全局 `usedGates`。
2. **单向门使用范围**：只用于解谜特殊房间。关闭时绝对不可通行，只允许在 `fromMask` 指定的若干邻格手动开启（完整 8 bit 掩码，1~8 个方向，不假设墙在固定方向）；怪物不能自行开门，不要求自动寻路能穿过。如果以后要用于高频通行区域，需要改回有向可达性方案。
3. **循环楼梯的交互**：默认“踩上去就传送”，不改成“点击激活”；普通楼梯仍走 `LevelTransition`。
4. **循环普通门**：已确认删除。普通门只支持单向、一次性、锁定、条件出现；需要循环时用特殊楼梯表达。
5. **怪物是否消耗一次性门**：默认只由英雄消耗；怪物可以正常通过，但不能把门用掉。
6. **测试内容**：默认在后续步骤把 `CycleTestLevel`/测试入口/`TEST` 分支移出正式流程；如果希望继续在生产环境保留测试入口，需要额外的调试开关。

## 13. 实现记录

- 新增 `SpecialDoor/SpecialDoors`、`SpecialStair/SpecialStairs`、`GateTrigger/GateTriggers`，删除 `GateType/DoorGate/DoorGates/GateEvents/CycleStair/CycleStairData/CycleStairs`。
- 条件门统一用 `locked(triggerId)` 表达；内置 `explosion`、`any_hero_move`，内容侧可用 `GateTriggers.register(...)` 注册。
- 新增 `GateSignalEvent`：门进入、楼梯使用可以配置 `signalOnEnter/signalOnUse` 发布具名信号，其他条件门用 `locked("signal:<id>")` 订阅。机关、踏板只需调用 `GateTriggers.signal("sewer.lever")`。
- `EventManager` 改为“先具体事件类，未取消再分发 `GameEvent` 基类”。
- 单向门关闭时写 `level.passable=false`；英雄在 `fromMask` 允许的邻格点击时走 `HeroAction.EnterDoor`，同一回合开门并进入。
- 兼容性说明：本方案记录的旧存档字段迁移未实现；历史字段 `door_gates/cycle_stairs/used_gates` 会被忽略。修改存档结构时应重新核对当前版本的兼容策略。
- 新增条件出现门：`SpecialDoor.toggle/appeared` + `SpecialDoors.addToggle(...)`；每次匹配触发器在 `WALL` 与 `DOOR` 之间翻转，状态随存档保存。
- 一次性门封死前先清空门格：其他角色和物品会被弹到相邻空格（优先英雄前进方向的反侧），相邻格不可用时退到两格内；找不到落点则本次不封门。
- 普通门循环确认不支持；ABCABC 循环由 `SpecialStair` 表达。
- 新增示例房间 `CombatRoom`：继承 `EmptyRoom`，和普通房间一样参与生成，门数由构建器决定；目标生成 2~4 个当前层怪物（可用位置不足时数量会更少），英雄进入后锁住该房的全部门，房内敌人全灭后恢复。`doorCells/guardIds/started/cleared` 随房间存档。
