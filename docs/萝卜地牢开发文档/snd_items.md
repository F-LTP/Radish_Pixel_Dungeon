# Slice&Dice 图标加载系统（SNDItems）

> 状态：**已实现**。

## 概述

`SNDItems` 提供从 Slice&Dice 贴图集中加载图标的能力。**当前实现不限于物品**：从 `snd/atlas_image.atlas` 动态解析全部区域，覆盖 `item/`（物品）、`ability/spell/`、`ability/tactic/`（法术/技能）、`trigger/`（触发）、`icon/`、`3dlink/`（卡牌面）等所有区域，名称与坐标以 `.atlas` 文件为准，无需手工维护索引。

## 文件位置

- **Java 类**: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/SNDItems.java`
- **贴图集**: `core/src/main/assets/snd/atlas_image.png`（1024×1024，RGBA8888）
- **坐标清单**: `core/src/main/assets/snd/atlas_image.atlas`（LibGDX 纹理图集格式）
- **名称列表**: `core/src/main/assets/snd_items_list.txt`（仅历史清单，非加载依据）

## 名称规则

`.atlas` 中区域名形如 `item/arrow`、`ability/spell/fireball`、`trigger/xxx`。`SNDItems` 的 key 处理：

- `item/xxx` → 短名 `xxx` 与全名均可查（如 `SNDItems.get("arrow")`）
- `ability/spell/xxx` → 短名 `xxx` 与全名均可查
- 其余区域 → 仅用完整路径名（如 `SNDItems.get("trigger/xxx")`）

## 使用方式

```java
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;
import com.watabou.noosa.Image;

// 获取物品图标（item/ 前缀可省略）
Image arrow = SNDItems.get("arrow");

// 获取法术/技能图标
Image fireball = SNDItems.get("ability/spell/fireball"); // 短名 "fireball" 亦可

// 安全获取（找不到时返回占位符）
Image img = SNDItems.getOrPlaceholder("some-item");

// 检查是否存在
if (SNDItems.has("ruby")) { /* ... */ }

// 获取 UV 坐标（直接贴图，不进 Image）
RectF uv = SNDItems.frame("item/arrow");

// 获取纹理（共享缓存）
SmartTexture tex = SNDItems.texture();

// 获取所有名称
String[] allNames = SNDItems.names();
```

### 关键行为

- **`get(name)`**：找不到时返回 `null`（不是占位符）。需要占位符请用 `getOrPlaceholder`。
- **`getOrPlaceholder(name)`**：找不到时返回固定占位符（`atlas_image.png` 中坐标 162,205 的默认图）。
- **`frame(name)` / `texture()`**：用于手动控制贴图绘制，返回 `RectF` UV / `SmartTexture`。
- **`REGIONS` 为空**：若 `.atlas` 缺失或解析失败，所有查询返回 `null`/占位符，调用方应自行兜底。

## 维护说明

新增图标时，把区域加入 `.atlas`（或通过贴图打包工具重新导出 `atlas_image.png` + `atlas_image.atlas`）即可，**无需改 Java 代码**。名称即资源 key，遵守上面的命名规则即可在代码中引用。

> 历史提示：早期版本曾仅加载物品图标并内置一份 475 个名称的硬编码清单，现已被 `.atlas` 动态解析取代，故旧清单只作参考，不再是加载依据。
