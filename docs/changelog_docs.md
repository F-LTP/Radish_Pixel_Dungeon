# Changelog 撰写指南

## 文件位置

Changelog 文件位于：
```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/
```

### 版本文件命名
- `v2_X_Changes.java` - 2.x 版本系列
- `v1_X_Changes.java` - 1.x 版本系列
- `v0_9_X_Changes.java` - 0.9.x 版本系列
- 以此类推...

---

## 添加新条目的步骤

### 步骤 1：找到对应版本文件

根据你要添加的版本号，打开对应的文件。例如：
- v2.4.2 的改动 → `v2_X_Changes.java`
- v1.5.3 的改动 → `v1_X_Changes.java`

### 步骤 2：找到或创建版本区块

在 `addAllChanges()` 方法中确保调用了你的版本方法：

```java
public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
    add_v2_4_Changes(changeInfos);  // 确保这行存在
    add_v2_3_Changes(changeInfos);
    // ...
}
```

### 步骤 3：添加版本信息

在对应的 `add_vX_X_Changes()` 方法中添加：

```java
public static void add_v2_4_Changes(ArrayList<ChangeInfo> changeInfos) {
    
    // 主版本标题（如 v2.4）
    ChangeInfo changes = new ChangeInfo("v2.4", true, "");
    changes.hardlight(Window.TITLE_COLOR);
    changeInfos.add(changes);
    
    // 子版本标题（如 v2.4.2）
    changes = new ChangeInfo("v2.4.2", false, null);
    changes.hardlight(Window.TITLE_COLOR);
    changeInfos.add(changes);
    
    // 添加具体条目
    changes.addButton(new ChangeButton(
        new ItemSprite(ItemSpriteSheet.KILL_BOAT),  // 图标
        "斩舰刀",                                    // 标题
        "增加了新的攻击特效：\n" +                  // 内容（支持\n换行）
        "_-_ 攻击前需要等待一回合\n" +
        "_-_ 等待时播放充能特效"
    ));
}
```

---

## ChangeButton 参数说明

### 构造函数 1：使用物品图标
```java
new ChangeButton(
    Item item,           // 物品对象
    String title,        // 标题
    String... messages   // 描述内容（可变参数）
)
```

### 构造函数 2：使用自定义图标
```java
new ChangeButton(
    Image icon,          // 图标对象
    String title,        // 标题
    String... messages   // 描述内容（可变参数）
)
```

### 常用图标来源

#### 物品图标
```java
new ItemSprite(ItemSpriteSheet.你的物品)
```

#### UI 图标
```java
Icons.get(Icons.SHPX)      // 管理员图标
Icons.get(Icons.BUFFS)     // Buffs 图标
Icons.get(Icons.PREFS)     // 设置图标
Icons.get(Icons.TALENT)    // 天赋图标
```

#### 角色精灵
```java
new HeroSprite()
new GhostSprite()
new ShopkeeperSprite()
```

---

## 文本格式

### 支持 Markdown 子集

| 语法 | 效果 | 示例 |
|-----|------|------|
| `_文字_` | 斜体 | `_斩舰刀_` → _斩舰刀_ |
| `**文字**` | 粗体 | `**重要**` → **重要** |
| `\n` | 换行 | `"第一行\n第二行"` |
| `"_-_ 列表"` | 列表项 | `"_-_ 改动 1\n"_-_ 改动 2"` |

### 颜色设置

```java
changes.hardlight(Window.TITLE_COLOR);  // 标题颜色（白色）
changes.hardlight(0xCCCCCC);            // 灰色（即将推出）
changes.hardlight(0xFF6644);            // 自定义颜色
```

---

## 完整示例

### 示例 1：添加物品改动

```java
changes.addButton(new ChangeButton(
    new ItemSprite(ItemSpriteSheet.LEATHER),
    "皮甲",
    "增加了针对豺狼类敌人的特效：\n\n" +
    "_-_ 对豺狼类敌人的掉落率增加 _(25 + 15×等级)%_\n" +
    "_-_ 包括：普通豺狼、豺狼守卫、豺狼大酋长等"
));
```

### 示例 2：添加天赋改动

```java
changes.addButton(new ChangeButton(
    new TalentIcon(Talent.YOUR_TALENT),
    "天赋名称",
    "调整了天赋效果：\n\n" +
    "_-_ 等级 1: 效果 A\n" +
    "_-_ 等级 2: 效果 B\n" +
    "_-_ 等级 3: 效果 C"
));
```

### 示例 3：添加平衡性调整

```java
changes.addButton(new ChangeButton(
    Icons.get(Icons.BUFFS),
    "平衡性调整",
    "调整了以下游戏平衡性：\n\n" +
    "_-_ 斩舰刀：伤害从 10-60 提升至 12-65\n" +
    "_-_ 鳞甲：水中闪避加成增加\n" +
    "_-_ 板甲：伤害免疫阈值调整"
));
```

### 示例 4：即将推出的内容

```java
ChangeInfo comingSoon = new ChangeInfo("Coming Soon", true, "");
comingSoon.hardlight(0xCCCCCC);
changeInfos.add(comingSoon);

comingSoon.addButton(new ChangeButton(
    Icons.get(Icons.SHPX),
    "下一版本预览",
    "下一版本将专注于:\n\n" +
    "_-_ 新内容开发\n" +
    "_-_ Bug 修复\n" +
    "_-_ 性能优化"
));
```

---

## 分类建议

为了保持 changelog 清晰，建议按以下分类组织条目：

1. **新内容** - 新物品、新敌人、新关卡等
2. **平衡性调整** - 数值改动、机制调整
3. **Bug 修复** - 错误修复
4. **界面改进** - UI/UX 改进
5. **性能优化** - 性能提升

每个分类使用一个 `ChangeButton`，使用相应的图标。

---

## 注意事项

1. **版本号格式**：必须与 `appVersionName` 一致
2. **文本长度**：保持简洁，避免过长的段落
3. **图标一致性**：同一类别的改动使用相同图标
4. **多语言**：如果支持多语言，使用 `Messages.get()` 方法
5. **测试**：添加后运行游戏查看 changelog 界面显示效果

---

## 快速模板

复制以下模板开始撰写：

```java
// 在 add_vX_X_Changes() 方法中添加

changes = new ChangeInfo("vX.X.X", false, null);
changes.hardlight(Window.TITLE_COLOR);
changeInfos.add(changes);

changes.addButton(new ChangeButton(
    new ItemSprite(ItemSpriteSheet.你的物品),
    "改动标题",
    "改动描述：\n\n" +
    "_-_ 具体改动 1\n" +
    "_-_ 具体改动 2\n" +
    "_-_ 具体改动 3"
));
```

---

## 查看效果

运行游戏后，通过以下方式查看 changelog：
1. 主菜单 → "Changes" 按钮
2. 更新后自动弹出的新改动窗口
3. 设置界面中的版本信息
