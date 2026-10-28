# 楼梯配对 ID 系统（Stair Pair ID System）

> 状态：**已实现**。

## 背景问题

一层存在多个楼梯时，当前 `Level.getTransition(Type)` 只按类型返回**第一个**匹配楼梯（`InterlevelScene.descend()/ascend()` 均依赖此逻辑），导致：
- 从楼梯 A 下去，从对应楼梯上来时可能出现在楼梯 B（串位）
- 下一层有多个同类型楼梯时，一旦 fallback 配对错误，错误会随存档**永久固化**


## 数据结构改动

### 1. `LevelTransition.java` — 新增字段

```java
public String pairId = null;   // 配对组 ID；null = 尚未分配（旧存档/待处理）

### 3. `Level.java` — 新增循环游标 + 精确匹配查询

```java
// 循环游标：pairId → 该组楼梯中"下次应落点"的索引
public HashMap<String, Integer> stairCursor = new HashMap<>();

// storeInBundle / restoreFromBundle 中存取：
//   put("stair_cursor_keys", stairCursor.keySet().toArray(new String[0]))
//   put("stair_cursor_vals", 对应 int[])

/** 按类型 + pairId 精确匹配；matches 为空返回 null */
public LevelTransition getTransition(Type type, String pairId){
    for (LevelTransition t : transitions){
        if (t.type == type && t.pairId != null && t.pairId.equals(pairId)){
            return t;
        }
    }
    return null;
}

/** 按类型 + pairId 匹配，多楼梯时循环选择落点并推进游标 */
public LevelTransition getTransition(Type type, String pairId, boolean advance){
    ArrayList<LevelTransition> matches = new ArrayList<>();
    for (LevelTransition t : transitions){
        if (t.type == type && t.pairId != null && t.pairId.equals(pairId)){
            matches.add(t);
        }
    }
    if (matches.isEmpty()) return null;

    int idx = stairCursor.getOrDefault(pairId, 0) % matches.size();
    if (advance) stairCursor.put(pairId, idx + 1);
    return matches.get(idx);
}
```

> 原有 `getTransition(Type)` 保持不变，`exit()/entrance()/switchLevel()` 等调用点不受影响。

## ID 分配与继承规则

### 分配时机

| 场景 | 规则 |
|------|------|
| 新层生成，楼梯**无继承对象**（如主线 EXIT 创建时目标层未生成） | 创建后分配新 id（`StairRegistry.allocate()`） |
| 新层生成，楼梯**有继承对象**（type == 源楼梯 destType） | **继承**源楼梯的 pairId（不分配新 id） |
| 目标层已生成（`loadLevel`） | 不动，楼梯 id 已在存档中 |

### 继承上下文传递

继承信息通过 `InterlevelScene.curTransition` 传递：新层生成后、`switchLevel` 前，遍历新层 `transitions`：

```java
if (isNewLevel){
    for (LevelTransition t : level.transitions){
        if (t.type == curTransition.destType){
            t.pairId = curTransition.pairId;      // 继承：入口侧楼梯
        } else if (t.pairId == null){
            t.pairId = StairRegistry.allocate();  // 其他楼梯：独立分配
        }
    }
}
```

**注意**：`curTransition.pairId` 若为 null（旧存档楼梯），必须在 `Dungeon.saveAll()` **之前**补分配，使当前层存档包含该 id。

## 过渡匹配逻辑（`InterlevelScene`）

### descend()（向下，出口 → 入口）改造

```java
// 1. 确保源楼梯有 id（必须在 saveAll 之前，使当前层存档写入 id）
if (curTransition.pairId == null){
    curTransition.pairId = StairRegistry.allocate();
}

Dungeon.saveAll();

Level level;
Dungeon.depth = curTransition.destDepth;
Dungeon.branchId = curTransition.destBranchId;

boolean isNewLevel = false;
if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branchId)) {
    level = Dungeon.loadLevel( GamesInProgress.curSlot );
} else {
    level = Dungeon.newLevel();
    isNewLevel = true;
}

// 2. 新层：继承配对 id / 独立分配
if (isNewLevel){
    for (LevelTransition t : level.transitions){
        if (t.type == curTransition.destType){
            t.pairId = curTransition.pairId;
        } else if (t.pairId == null){
            t.pairId = StairRegistry.allocate();
        }
    }
}

// 3. 精确匹配（多入口循环），不再 fallback 到"第一个"
LevelTransition destTransition =
        level.getTransition(curTransition.destType, curTransition.pairId, true);
// 理论上必然命中；若为 null（异常/旧档极端情况）退回原逻辑作为兜底
if (destTransition == null){
    destTransition = level.getTransition(curTransition.destType);
}

curTransition = null;
Dungeon.switchLevel( level, destTransition.cell() );
```

### ascend()（向上，入口 → 出口）对称改造

- 源楼梯（入口）pairId 为 null 时同样补分配（正常流程中入口已继承 id，此分支是兜底）
- 目标层（上层）几乎总是已生成 → `isNewLevel` 通常为 false，直接精确匹配
- 循环落点：主线若存在多个同 pairId 的出口（多对多设计时），同样循环

## 循环机制

- 游标存于**目标层**的 `Level.stairCursor`（随层存档，层删除时自然消失）
- `getTransition(type, pairId, true)` 按游标取模选择落点并推进
- 效果：玩家连续从同一出口下楼，会依次出现在 A → B → A → B……

## 存档与兼容性

| 数据 | 存储位置 | 说明 |
|------|----------|------|
| pairId | LevelTransition bundle（随层存档） | 新字段，旧档为 null |
| stairCursor | Level bundle（随层存档） | 新字段，旧档为空 Map |
| StairRegistry | Dungeon bundle（全局存档） | 新字段，旧档从 0 重建 |

**旧存档迁移**：旧档楼梯 pairId 均为 null，首次过渡时自动补分配（`curTransition` 补 id + 目标层继承），无需显式迁移代码。注册表从 0 重建不会与旧 id 冲突（旧档没有 id）。

## 改动文件清单

| 文件 | 改动 |
|------|------|
| `levels/features/LevelTransition.java` | +`pairId` 字段 + bundle 存取 |
| `levels/features/StairRegistry.java` | **新增**（注册表类） |
| `levels/Level.java` | +`stairCursor` Map + bundle 存取；+2 个 `getTransition(type, pairId)` 重载 |
| `scenes/InterlevelScene.java` | descend/ascend 改造（约 10 行/处） |
| `Dungeon.java` | storeInBundle/restoreFromBundle 挂载 StairRegistry |

**生成代码（各 Room.paint）零改动**——id 分配统一在 InterlevelScene 过渡时完成。

## 边界情况

1. **SURFACE 楼梯**（第 1 层入口，destType=null）：不参与配对，pairId 保持 null，匹配逻辑不涉及（原逻辑处理地表）。
2. **同一层多个不同分支的入口**（如主线 5 层有矿洞入口 + 苔藓入口）：两者 type 相同（BRANCH_EXIT）但 **destBranchId 不同** → 各分配独立 pairId，互不干扰。
3. **多对多设计**（多个主线出口 ↔ 多个苔藓入口全部同组）：全组共享一个 pairId，双向均循环。若需"X1 只能回 X1"，则出口侧也应各自独立 pairId（此时苔藓侧入口按继承上下文分组）。
4. **循环游标溢出**：`idx+1` 取模保证安全；游标只增不减，Long 溢出风险可忽略（int 足够）。
5. **存档文件定位**：仍沿用现有 `destDepth + destBranchId` 定位层文件，pairId 只负责**层内楼梯配对**，两者职责分离。
