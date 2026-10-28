# Changelog 撰写指南

> 状态：📖 指南。

## 概述

本项目的 changelog（改动日志）由 Java 代码驱动，运行时会渲染成主菜单的「Changes」界面。**萝卜地牢专属的改动不写进原版版本文件，而是放在独立的 `rapd/` 子目录中**。

## 文件位置

```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/
├── ChangeButton.java      // 单条改动按钮
├── ChangeInfo.java        // 版本分组信息
├── ChangesWindow.java
├── rapd/                  // ★ 萝卜地牢专属改动放这里
│   ├── RA_v0_3_X_Changes.java
│   ├── RA_v0_4_X_Changes.java
│   ├── ...
│   └── RA_v0_8_X_Changes.java
├── v0_1_X_Changes.java    // 原版版本文件（萝卜地牢不直接改动）
└── ...
```

## 萝卜地牢版本文件的组织方式

每个 `RA_v0_X_X_Changes.java` 代表一个 0.x 版本系列。文件内通过 `addAllChanges` 依次调用各子版本方法：

```java
package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.noosa.Image;
import java.util.ArrayList;

public class RA_v0_8_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v08_3_Changes(changeInfos);
        add_v08_2_Changes(changeInfos);
        add_v08_1_Changes(changeInfos);
        add_v08_0_Changes(changeInfos);
    }

    public static void add_v08_3_Changes(ArrayList<ChangeInfo> changeInfos) {
        // 主版本标题
        ChangeInfo changes = new ChangeInfo("v0.8.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        // 分组标题（如"新内容"）
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        // 单条改动
        changes.addButton(new ChangeButton(
            new TalentIcon(Talent.SCHOOL_FIRE),
            "骰子法师学派系统",
            "骰子法师的法术体系全面重做为学派系统！\n\n" +
            "_-_ 新增火焰、刀刃等十大学派\n" +
            "_-_ 每个学派投入点数逐级习得对应法术"
        ));
    }
}
```

## 添加新条目的步骤

1. **找到版本文件**：在 `rapd/` 下找到对应版本系列的 `RA_v0_X_X_Changes.java`（如 0.8.x → `RA_v0_8_X_Changes.java`）。
2. **确保 `addAllChanges` 调用了对应子版本方法**：若无对应子版本方法（如 `add_v08_3_Changes`），先新增一个，并在 `addAllChanges` 中加入调用。
3. **在子版本方法内添加内容**：
   - 用 `new ChangeInfo("vX.X.X", true, "")` 添加主版本标题（`true` = 主标题）。
   - 用 `new ChangeInfo(Messages.get(ChangesScene.class, "new"/"changes"/...), false, null)` 添加分组（`false` = 子标题）。
   - 用 `changes.addButton(new ChangeButton(icon, "标题", "内容"))` 添加单条改动。

## ChangeButton 参数

```java
// 用物品对象做图标
new ChangeButton(Item item, String title, String... messages)

// 用自定义 Image 图标
new ChangeButton(Image icon, String title, String... messages)
```

### 常用图标

- 物品：`new ItemSprite(ItemSpriteSheet.你的物品)`
- 天赋：`new TalentIcon(Talent.你的天赋)`
- UI：`Icons.get(Icons.SHPX)`、`Icons.get(Icons.BUFFS)`、`Icons.get(Icons.PREFS)`、`Icons.get(Icons.TALENT)`
- 精灵：`new HeroSprite()`、`new GhostSprite()`、`new ShopkeeperSprite()`

## 文本格式（Markdown 子集）

| 语法 | 效果 | 示例 |
|-----|------|------|
| `_文字_` | 斜体 | `_斩舰刀_` → _斩舰刀_ |
| `**文字**` | 粗体 | `**重要**` → **重要** |
| `\n` | 换行 | `"第一行\n第二行"` |
| `_-_ 列表` | 列表项 | `"_-_ 改动 1\n"_-_ 改动 2"` |

### 颜色设置

```java
changes.hardlight(Window.TITLE_COLOR);  // 标题颜色（白色）
changes.hardlight(CharSprite.WARNING);  // 分组强调色
changes.hardlight(0xFF6644);            // 自定义颜色
```

## 分组标题（ChangesScene）

用 `Messages.get(ChangesScene.class, "key")` 引用预定义分组，常用 key：

- `new` — 新内容
- `changes` — 改动/平衡性调整
- `bugfixes` — Bug 修复（如存在）

> 具体有哪些 key 以 `ChangesScene` 的消息文件为准，可查看 `ui/changelist/ChangesWindow.java` 或 `assets/messages/` 下的对应文案。

## 注意事项

1. **版本号**：必须与 `appVersionName` 一致。
2. **文本**：保持简洁，支持 `\n` 与 `_-_` 列表。
3. **多语言**：UI 文案用 `Messages.get()`，避免硬编码。
4. **验证**：添加后运行游戏查看主菜单「Changes」界面是否正常显示。
