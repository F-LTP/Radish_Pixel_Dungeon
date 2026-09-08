# 分支楼层开发指南

> 状态：**已实现**。本文描述当前项目的分支与楼梯系统。添加新支线时以本文档和当前代码为准，不要恢复旧的 `destType`、`destBranchId`、整数 `branch` 或模糊楼梯匹配逻辑。

## 核心规则

- 一个楼层由 `branchId + depth` 唯一确定，不使用额外的 Floor ID。
- 每个分支的层数独立计算，从 `1` 开始。
- 一条双向楼梯连接的两端使用相同且稳定的 `linkId`。
- `UP` 表示上楼，地形使用 `Terrain.ENTRANCE`。
- `DOWN` 表示下楼，地形使用 `Terrain.EXIT`。
- 连接两端必须方向相反、目标互相指回。
- 同一楼层内，一个 `linkId` 只能出现一次。
- 目标楼层没有生成时，系统会先生成它，再按 `linkId` 查找对应楼梯。
- 找不到、找到多个或反向信息不一致时立即报错，不会回退到其他楼梯。

相关实现：

- `levels/branches/Branch.java`：单个分支配置。
- `levels/branches/Branches.java`：分支注册表。
- `levels/features/LevelTransition.java`：楼梯数据与创建方法。
- `levels/Level.java`：楼梯激活与严格查询。
- `scenes/InterlevelScene.java`：加载、生成、验证和切换目标层。
- `Dungeon.java`：根据分支和层数加载或生成楼层。

## 1. 注册新分支

假设添加一个两层的花园支线。

首先在 `Branches.java` 定义稳定 ID：

```java
public static final String GARDEN = "garden";
```

然后注册分支：

```java
public static void init() {
    registry.clear();
    register(createMainBranch());
    register(createMossBranch());
    register(createMiningBranch());
    register(createGardenBranch());
}
```

为每个局部层数指定 Level 类：

```java
private static Branch createGardenBranch() {
    @SuppressWarnings("unchecked")
    Class<? extends Level>[] levels = new Class[3];
    levels[1] = GardenLevel.class;
    levels[2] = GardenBossLevel.class;
    return new Branch(GARDEN, 2, "branch_garden", levels);
}
```

注意：

- 数组索引就是支线内部深度。
- `maxDepth` 必须与最大有效索引一致。
- 不要把来源主线层数当作支线层数。进入花园支线第一层时目标永远是 `garden:1`。
- 非法分支或非法层数会直接报错，不再生成 `DeadEndLevel` 掩盖配置错误。

根据项目消息文件的现有命名方式，为新分支补充本地化名称。

## 2. 规划连接 ID

每一对物理楼梯使用一个唯一 `linkId`。推荐格式：

```text
<支线>:<来源分支>-<来源层数>
```

例如主线第 7 层连接花园第一层：

```text
garden:main-7
```

如果同一对楼层间有多个独立入口，增加稳定后缀：

```text
garden:main-7:a
garden:main-7:b
```

不要用随机数、对象哈希或生成顺序构造 `linkId`。保存并重新加载后，它必须保持不变。

## 3. 创建主线端下楼梯

在主线入口房间的 `paint()` 中创建通往支线的端点：

```java
String linkId = "garden:main-7";
LevelTransition transition = LevelTransition.branchDown(
        level,
        cell,
        linkId,
        Branches.GARDEN,
        1
);
level.transitions.add(transition);
Painter.set(level, cell, Terrain.EXIT);
```

`branchDown()` 表示这个端点向下，并把类型设为 `BRANCH_EXIT`。目标由 `Branches.GARDEN + 1` 唯一确定。

如果入口只应在特定主线层生成，必须在主线生成逻辑中限制房间出现条件。

## 4. 创建支线端上楼梯

目标层生成时必须主动创建同一个 `linkId` 的另一端。系统不会自动决定房间或格子位置。

```java
String linkId = "garden:main-7";
LevelTransition transition = LevelTransition.branchUp(
        level,
        cell,
        linkId,
        Branches.MAIN,
        7
);
level.transitions.add(transition);
Painter.set(level, cell, Terrain.ENTRANCE);
```

两端的完整关系必须是：

```text
main:7   DOWN  garden:main-7  -> garden:1
garden:1 UP    garden:main-7  -> main:7
```

玩家第一次进入时，`InterlevelScene` 会：

1. 保存当前层。
2. 加载 `garden:1`；如果不存在则调用其正常地图生成逻辑。
3. 在生成后的目标层严格查找 `garden:main-7`。
4. 验证目标端指回 `main:7`，并且方向为 `UP`。
5. 验证成功后将英雄放到目标端点。

因此，目标 `Level` 的首次生成路径必须包含该上楼梯。遗漏它会得到明确的 `Missing transition` 错误。

## 5. 支线内部普通楼梯

支线相邻层之间使用普通楼梯辅助方法，不需要手写 `linkId`：

```java
// 当前层的上楼梯
LevelTransition up = LevelTransition.regularEntrance(level, entranceCell);
level.transitions.add(up);
Painter.set(level, entranceCell, Terrain.ENTRANCE);

// 当前层的下楼梯
LevelTransition down = LevelTransition.regularExit(level, exitCell);
level.transitions.add(down);
Painter.set(level, exitCell, Terrain.EXIT);
```

系统会生成类似 `garden:1-2` 的稳定 ID：

```text
garden:1 DOWN garden:1-2 -> garden:2
garden:2 UP   garden:1-2 -> garden:1
```

支线第一层返回主线的楼梯不是普通入口，必须使用 `branchUp()` 和支线连接的 `linkId`。

最后一层不要创建超出 `Branch.maxDepth` 的普通出口。

## 6. 多个入口

一个支线楼层可以有多个入口，但每一对楼梯必须使用不同的 `linkId`。

例如两个主线入口都连接 `garden:1`：

```text
main:7   DOWN garden:main-7 -> garden:1
garden:1 UP   garden:main-7 -> main:7

main:9   DOWN garden:main-9 -> garden:1
garden:1 UP   garden:main-9 -> main:9
```

`garden:1` 的生成逻辑必须放置两个上楼端点。它们可以位于不同房间，但不能共用 `linkId`。

不要在玩家第一次进入后临时挑选一个未配对楼梯。动态配对会让生成顺序影响连接关系，并增加额外存档状态。

## 7. 动态来源层

如果入口可能出现在多个主线层，但每局只出现一次，应在入口确定时保存来源层数。Mining 分支就是参考实现：

```java
entranceDepth = Dungeon.depth;
```

主线端和支线端使用同一保存值：

```java
String linkId = "mining:main-" + entranceDepth;
```

主线端目标仍然是 `mining:1`，采矿端再指回实际的 `main:entranceDepth`。这个来源层数必须随任务或地牢状态保存，不能从当前支线深度推测。

## 8. 方向、类型和地形

三者必须一致：

| 创建方法 | 方向 | 类型 | 地形 |
|---|---|---|---|
| `regularEntrance()` | `UP` | `REGULAR_ENTRANCE` | `Terrain.ENTRANCE` |
| `regularExit()` | `DOWN` | `REGULAR_EXIT` | `Terrain.EXIT` |
| `branchUp()` | `UP` | `BRANCH_ENTRANCE` | `Terrain.ENTRANCE` |
| `branchDown()` | `DOWN` | `BRANCH_EXIT` | `Terrain.EXIT` |

类名中的 Entrance 通常表示“玩家进入本层的位置”，所以从该位置离开时是上楼。不要把 Entrance 理解为向下进入支线。

## 9. 不属于楼梯的移动

定点传送、测试传送器、复活和法术返回不应伪造成一对物理楼梯。它们使用 `InterlevelScene.Mode.RETURN` 或各自的专用流程，并提供明确的目标分支、层数和格子。

坠落仍使用 `FALL` 流程。不要为这些行为创建假的 `linkId` 或绕过双向校验。

## 10. 添加完成后的检查清单

- 新 `branchId` 已在 `Branches.init()` 注册。
- 分支层数从 `1` 开始，`maxDepth` 与 Level 数组一致。
- 所有目标 `branchId + depth` 都合法。
- 一对楼梯使用完全相同的稳定 `linkId`。
- 每端的目标都准确指向另一端所在楼层。
- 两端方向相反。
- `UP` 使用 `Terrain.ENTRANCE`，`DOWN` 使用 `Terrain.EXIT`。
- 目标层首次生成时一定会创建对应端点。
- 同一楼层没有重复 `linkId`。
- 多个入口各自使用不同 ID。
- 动态来源层已经持久化，不能从支线局部深度猜测。
- 没有调用模糊匹配作为跨层到达位置。
- 没有重新引入整数分支或旧存档迁移字段。

按项目约定，修改后由维护者自行执行 Gradle 编译；提交前至少运行静态搜索和 `git diff --check`。
