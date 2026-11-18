# 门系统与 CombatRoom 使用指南

本文面向开发者与 AI Agent，说明特殊门/楼梯的模型、关卡接入方式，以及 `CombatRoom` 的机制示范。新增同类内容前，应先复用这里的 API 和事件，不要另建一套门状态或直接修改 `LevelTransition` 的拓扑字段。

## 模型边界

- 普通门由地形 `DOOR` / `OPEN_DOOR` 表达，不需要创建 `SpecialDoor`。
- 特殊门按门格存入 `Level.specialDoors`，支持单向、一次性、触发器锁定和条件出现。ABCABC 不属于普通门。
- 特殊楼梯按 transition 格存入 `Level.specialStairs`。循环链由特殊楼梯表示；普通 `LevelTransition` 仍负责常规层间连接。
- 规则状态由 `SpecialDoor` / `SpecialStair` 保存，关卡切换和存档应使用这些对象，不要另加全局一次性状态。

## 门的创建

在房间绘制完成、门格和地形已确定后注册规则：

```java
SpecialDoors.add(level, doorCell)
        .oneWayFromSide(level, seedCell);

SpecialDoors.add(level, oneShotCell)
        .oneShot();

SpecialDoors.add(level, bombDoorCell)
        .locked("explosion");

SpecialDoors.addToggle(level, conditionalDoorCell, "signal:lever.pulled");
```

双向普通门不用注册。单向门用 `oneWayFromSide` 从区域种子计算允许侧；不规则场景可用 `oneWayFromCells` 指定允许的相邻格。单向门应位于确实分隔区域的位置，避免掩码为空或覆盖全部方向。

触发器使用稳定的 ID。炸弹门使用 `explosion`；其他条件通过 `GateTriggers.register(...)` 注册 `GateTrigger`，或由机关调用 `GateTriggers.signal("signal-id")`。`signal:` 前缀是信号触发的门 ID 约定。条件出现门通过 `addToggle` 创建，每次匹配信号在墙和门之间翻转，不等价于锁定后永久解锁。

一次性门只由英雄完成使用结算。进入/离开及阻断逻辑由 `SpecialDoors` 处理，不要在房间代码里手动改 `passable` 来替代特殊门状态。若改动地形或特殊门注册时机，应确认 `SpecialDoors.applyLevelFlags(level)` 在关卡标志构建后运行。

## 楼梯与循环

普通楼梯继续使用 `LevelTransition`。需要单向、一次性或锁定规则时，通过 `SpecialStairs.add(level, transition)` 注册：

```java
SpecialStairs.add(level, transition).oneWay();
SpecialStairs.add(level, transition).oneShot();
SpecialStairs.add(level, transition).locked("signal:boss.defeated");
```

循环楼梯通过 `SpecialStairs.addCycle(level, cell, chainId, slot)` 声明 A/B/C 端点，并设置 `destCell(...)`（同层）或 `dest(branch, depth, destCell)`（跨层）。同一条链使用相同 ID，slot 依次为 0、1、2。落点必须可通行且无人占用；无效落点不得推进链条或消耗一次性状态。

## CombatRoom 示例

`levels/rooms/standard/CombatRoom.java` 展示一种完整的房间机制组合：

1. 继承 `EmptyRoom`，仍作为标准房间参与关卡生成；禁止合并，并关闭标准怪物权重，避免额外生成普通房间怪物。
2. `paint` 收集生成器连接门格，注册为特殊门，并尝试生成 2–4 个本房守卫；可用位置不足时实际数量可能更少。门格和守卫 ID 随房间存档。
3. 首次有英雄进入后锁住门。通过 `HeroMoveEvent` 和 `HeroActEvent` 检测进入/读档后仍在房内的情况。
4. 通过 `CharFinalDamageEvent` 和守卫存活检查判断清场；没有存活敌人后解锁门。
5. `onLevelLoad` 恢复门状态；仅在 `started && !cleared` 且读档时已无守卫的情形下修正战斗状态。

扩展战斗房时，应沿用房间坐标/存档状态和事件订阅；不要在每回合全局扫描所有房间，也不要只依赖死亡动画或当前场景回调来判定清场。若怪物可以召唤/分裂，应按房间范围补充判断，或把生成者 ID 纳入机制状态。

## 测试维度

测试支线不在正式关卡中生成入口，也不会随常规分支注册。使用测试模式中的“测试维度传送器”进入；传送器执行时临时注册测试分支，再进入测试层。在测试维度再次使用该传送器可返回原分支和层数。不要把测试楼梯、测试传送逻辑或测试分支注册加回 `SewerLevel`、`Level.pressCell` 或正式启动流程。

## 改动检查

新增门/楼梯行为后，至少检查：规则触发前后能否通行、阻断时是否扣回合、存档读档状态是否保留、传送失败是否改变循环/一次性状态，以及测试入口是否只存在于测试模式。门格与分支数据变更后运行 `./gradlew :core:compileJava --offline`。
