# 支线系统整改计划 v4（简化版）

## 一、核心需求

- 一层可以有多个下楼楼梯
- 每个楼梯有**固定目标楼层**和**目标楼梯 ID**
- 目标楼层生成时，**根据已记录的信息**创建对应楼梯
- 双向精确导航

---

## 二、核心设计

### 2.1 LevelTransition 增强

```java
public class LevelTransition extends Rect implements Bundlable {
    
    // 现有字段
    public Type type;
    public int centerCell;
    public int destDepth;
    public int destBranch;      // 改为 String 更灵活，保留 int 兼容
    public Type destType;
    
    // ====== 新增字段 ======
    public String id;           // 当前楼梯的唯一标识
    public String destId;       // 目标楼梯的唯一标识
}
```

### 2.2 楼梯 ID 命名规范

```
{来源分支}_{来源层数}_to_{目标分支}_{目标层数}

示例：
- main_1_to_main_2         主线1层 → 主线2层（普通下楼）
- main_2_to_moss_1         主线2层 → 苔藓1层（进入支线）
- moss_1_to_main_2         苔藓1层 → 主线2层（返回主线）
- moss_3_to_garden_1       苔藓3层 → 隐藏花园1层（嵌套支线）
- garden_1_to_moss_3       隐藏花园1层 → 苔藓3层（返回）
- main_15_to_main_16       主线15层 → 主线16层
```

**命名规则：**
- 来源和目标都明确标注分支和层数
- 一眼就能看出"从哪去哪"
- 双向楼梯互为镜像（如 `moss_1_to_main_2` 和 `main_2_to_moss_1`）

### 2.3 导航逻辑

```
主线2层 → 楼梯A (id="main_2_to_moss_1", destId="moss_1_to_main_2")
                ↓
苔藓1层 ← 楼梯B (id="moss_1_to_main_2", destId="main_2_to_moss_1")
```

---

## 三、楼层生成流程

### 3.1 主线楼层生成（创建入口楼梯）

```java
// 主线2层的 MossEntranceRoom.paint()
public void paint(Level level) {
    // ... 房间绘制 ...
    
    int cell = level.pointToCell(center());
    
    LevelTransition t = new LevelTransition(level, cell, Type.BRANCH_EXIT);
    t.id = "main_2_to_moss_1";
    t.destDepth = 1;
    t.destBranch = "moss";
    t.destId = "moss_1_to_main_2";  // 约定：目标楼层的楼梯 ID
    
    level.transitions.add(t);
    Painter.set(level, cell, Terrain.EXIT);
}
```

### 3.2 支线楼层生成（创建对应出口楼梯）

```java
// SmallGrassMiniLevel 或 Dungeon.newLevel()
// 检查是否有指向当前楼层的楼梯约定

public void createExits() {
    // 遍历所有已保存的过渡信息，找到指向当前楼层的
    List<TransitionInfo> sources = findSourcesTo(depth, branch);
    
    for (TransitionInfo info : sources) {
        // 创建对应的入口楼梯
        int cell = findSuitableCell();
        
        LevelTransition t = new LevelTransition(this, cell, Type.BRANCH_ENTRANCE);
        t.id = info.destId;           // 使用约定的 ID
        t.destDepth = info.sourceDepth;
        t.destBranch = info.sourceBranch;
        t.destId = info.sourceId;     // 配对
        
        transitions.add(t);
        map[cell] = Terrain.ENTRANCE;
    }
}
```

### 3.3 问题：如何知道"有哪些楼梯指向当前楼层"？

**方案：在存档中记录所有过渡点约定**

```java
// Dungeon.java
public class Dungeon {
    // 新增：全局过渡点约定表
    // 记录所有楼层生成时创建的楼梯信息
    public static Map<String, TransitionContract> transitionContracts;
}

public class TransitionContract {
    public String id;           // 楼梯 ID
    public int depth;           // 所在层数
    public String branch;       // 所在分支
    public int destDepth;       // 目标层数
    public String destBranch;   // 目标分支
    public String destId;       // 目标楼梯 ID
}
```

**流程：**

```
1. 主线2层生成 → 创建楼梯 A → 记录约定：main_2_to_moss_1 → moss_1_to_main_2

2. 苔藓1层生成 → 查询约定表 → 发现有约定 destId="moss_1_to_main_2"
              → 创建楼梯 B，id="moss_1_to_main_2"

3. 玩家踩楼梯 A → 查找 moss_1_to_main_2 → 精确定位到楼梯 B
```

---

## 四、完整实现

### 4.1 TransitionContract（过渡约定）

```java
public class TransitionContract implements Bundlable {
    public String id;
    public int depth;
    public String branch;
    public int destDepth;
    public String destBranch;
    public String destId;
    
    // Bundlable 实现...
}
```

### 4.2 Dungeon 增加约定表

```java
public class Dungeon {
    // 存档级别的约定表
    private static final String CONTRACTS = "contracts";
    public static Map<String, TransitionContract> transitionContracts = new HashMap<>();
    
    // 注册约定（楼层生成时调用）
    public static void registerContract(TransitionContract contract) {
        transitionContracts.put(contract.id, contract);
    }
    
    // 查找指向目标楼层的约定
    public static List<TransitionContract> findContractsTo(int destDepth, String destBranch) {
        List<TransitionContract> result = new ArrayList<>();
        for (TransitionContract c : transitionContracts.values()) {
            if (c.destDepth == destDepth && c.destBranch.equals(destBranch)) {
                result.add(c);
            }
        }
        return result;
    }
}
```

### 4.3 Level 生成时创建楼梯

```java
// 在 ExitRoom 或 SpecialRoom 中创建下楼楼梯
LevelTransition t = new LevelTransition(level, cell, Type.BRANCH_EXIT);
t.id = "main_2_to_moss_1";
t.destDepth = 1;
t.destBranch = "moss";
t.destId = "moss_1_to_main_2";
level.transitions.add(t);

// 注册约定
TransitionContract c = new TransitionContract();
c.id = t.id;
c.depth = Dungeon.depth;
c.branch = "main";
c.destDepth = t.destDepth;
c.destBranch = t.destBranch;
c.destId = t.destId;
Dungeon.registerContract(c);
```

### 4.4 支线楼层生成时查找并创建

```java
// Dungeon.newLevel() 中
public static Level newLevel() {
    Level level = createLevelFor(depth, branch);
    level.create();
    
    // 创建指向当前楼层的入口楼梯
    List<TransitionContract> sources = findContractsTo(depth, branch);
    for (TransitionContract c : sources) {
        int cell = level.findSuitableEntrance();
        
        LevelTransition t = new LevelTransition(level, cell, Type.BRANCH_ENTRANCE);
        t.id = c.destId;           // 约定的 ID
        t.destDepth = c.depth;     // 来源层数
        t.destBranch = c.branch;   // 来源分支
        t.destId = c.id;           // 来源楼梯 ID
        level.transitions.add(t);
        level.map[cell] = Terrain.ENTRANCE;
    }
    
    return level;
}
```

### 4.5 InterlevelScene 精确导航

```java
// InterlevelScene.java
case DESCEND:
case BRANCH:
    // 加载目标楼层
    level = Dungeon.loadLevel(destDepth, destBranch);
    
    // 根据 destId 精确定位
    LevelTransition dest = level.getTransitionById(curTransition.destId);
    if (dest != null) {
        hero.pos = dest.centerCell;
    } else {
        // 兜底：使用默认入口
        hero.pos = level.entrance();
    }
```

---

## 五、多楼梯示例

### 场景：主线2层有两个通往不同支线的楼梯

```java
// 楼梯 A：通往苔藓
t.id = "main_2_to_moss_1";
t.destDepth = 1;
t.destBranch = "moss";
t.destId = "moss_1_to_main_2";

// 楼梯 B：通往采矿
t.id = "main_2_to_mining_1";
t.destDepth = 1;
t.destBranch = "mining";
t.destId = "mining_1_to_main_2";
```

### 苔藓1层生成时

```java
// 查找约定 → 发现 destBranch="moss" 的约定
// 创建楼梯 id="moss_1_to_main_2"
// 记录 destId="main_2_to_moss_1"
```

### 返回时

```java
// 玩家在苔藓1层踩上楼梯 moss_1_to_main_2
// destId="main_2_to_moss_1"
// 精确定位到主线2层的楼梯 A
```

---

## 六、迁移要点

### 需要修改的文件

| 文件 | 修改内容 |
|------|----------|
| `LevelTransition.java` | 添加 `id` 和 `destId` 字段 |
| `Dungeon.java` | 添加约定表和查询方法 |
| `Level.java` | 添加 `getTransitionById()` 方法 |
| `InterlevelScene.java` | 使用 ID 精确定位 |
| `SmallGrassEnterRoom.java` | 使用新约定系统 |
| `SmallGrassMiniLevel.java` | 移除硬编码，使用约定表生成入口 |
| `GamesInProgress.java` | 支持新存档命名 |

---

## 七、简化后的 API

```java
// 创建下楼楼梯（在任意楼层）
LevelTransition t = new LevelTransition(level, cell, Type.BRANCH_EXIT);
t.id = "moss_3_to_garden_1";
t.destDepth = 1;
t.destBranch = "hidden_garden";
t.destId = "garden_1_to_moss_3";
level.transitions.add(t);

// 注册约定
Dungeon.registerContract(...);

// 目标楼层生成时自动创建对应入口
// 无需手动处理
```

---

这个方案的核心是**约定表**：楼层生成时记录楼梯信息，目标楼层生成时查询并创建对应楼梯。不需要预先在未生成的楼层中安排。