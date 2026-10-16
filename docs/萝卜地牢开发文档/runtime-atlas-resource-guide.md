# Runtime Atlas 图标资源维护指南

> 状态：**已实现**。

Item、Talent 和 Buff 图标已经不再使用手工拼接的整张 atlas。每个图标都是独立 PNG，游戏启动后按目录中的 `manifest.txt` 动态生成纹理页。

本文中的路径均相对于项目根目录。

## 通用规则

资源 ID 只能包含小写英文字母、数字和下划线：

```text
[a-z0-9_]+
```

Java 符号使用大写下划线形式，PNG 使用对应的小写形式：

```text
Java: POTION_HEALING
PNG:  potion_healing.png
```

所有 PNG 必须：

- 使用透明背景。
- 使用 PNG 格式，文件扩展名为小写 `.png`。
- 文件名与 Java 中的资源 ID 完全一致。
- 不使用空格、连字符、中文或大写字母作为资源 ID。

`manifest.txt` 由 Gradle 根据目录中的 PNG 自动生成。不要手工编辑；执行 Desktop 或 Android 构建时，Gradle 会在复制和打包资源前更新全部 manifest。

需要在不编译游戏的情况下单独生成并校验资源时，在项目根目录运行：

```bash
./gradlew generateRuntimeAtlasManifests
```

该任务只使用 Gradle/Groovy，不依赖 Python。它会按文件名排序生成 manifest，并检查文件名、Item 常量、Talent 枚举、Buff 常量及对应 PNG 是否一致。

## 新增 Item 贴图

Item 资源目录：

```text
core/src/main/assets/sprites/items/
```

Item 名称常量位于：

```text
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java
```

### 操作步骤

假设新增物品 `Moon Blade`，资源 ID 使用 `moon_blade`。

1. 添加图片：

```text
core/src/main/assets/sprites/items/moon_blade.png
```

2. 在 `ItemSpriteSheet` 外层声明字符串常量：

```java
public static final String MOON_BLADE = "moon_blade";
```

不要把它加入 `ItemSpriteSheet.Icons`。`Icons` 是物品右上角的 8x8 小图标系统，使用独立的 `sprites/item_icons` Runtime Atlas。新增角标时，需要把 PNG 加入该目录及其 `manifest.txt`，并在 `ItemSpriteSheet.Icons` 中声明值为文件名的字符串常量。

3. 在物品类中引用常量：

```java
{
    image = ItemSpriteSheet.MOON_BLADE;
}
```

需要动态切换图片时，字段和返回值都使用字符串：

```java
@Override
public String image() {
    return active ? ItemSpriteSheet.MOON_BLADE_ACTIVE : ItemSpriteSheet.MOON_BLADE;
}
```

每个动态变体都必须拥有独立常量和 PNG，不能再使用常量加数字的方式选择相邻帧。

4. 正常构建游戏，或运行 `./gradlew generateRuntimeAtlasManifests`。不需要编辑 manifest。

### 尺寸和居中

Item PNG 可以使用 16x16 画布，也可以直接使用更小的紧凑图片。运行时会扫描 alpha 值不低于 16 的像素，计算非透明外接矩形，并使用裁剪后的宽高在背包格子和地面位置中居中。

因此：

- 不需要手工调用 `assignItemRect`。
- 不需要维护 atlas 坐标。
- 不要依靠透明边距制造显示偏移，透明边距会被自动裁掉。
- 全透明 Item 会保留原始范围，但通常意味着资源制作错误。

### 大于 16x16 的 Item 图片

Runtime Atlas 支持宽度或高度超过 16 像素的 Item PNG。装箱时使用 PNG 的实际宽高，并在图片四周额外保留 1px 边缘扩展区域。例如一张 32x24 的图片会占用 34x26 的 atlas 区域，其他图片不会覆盖或进入该区域。

大图可能会增大 atlas 页或产生更多纹理页，但不会在生成 atlas 时被压缩、裁断或与其他图片重叠。单张图片加上边缘扩展后不能超过设备的 OpenGL 最大纹理尺寸。

当前显示阶段只会裁剪透明边界，不会自动缩小超过 16x16 的非透明外接矩形。因此，大图虽然能够安全进入 atlas，但会按实际尺寸显示，可能超出背包格子或标准地面物品范围。在自动适配缩放功能完成前，普通 Item 的非透明外接矩形仍建议控制在 16x16 以内。

计划中的显示适配规则是：当裁剪后的外接矩形超过 16x16 时，不修改 PNG 或 atlas，只对 `ItemSprite` 的显示应用等比缩小，使其宽高都不超过 16 像素；16x16 及更小的图片保持原始像素尺寸。

新增装备后，还需要按项目约定重新生成押韵表。

## 新增 Talent 贴图

Talent 资源目录：

```text
core/src/main/assets/interfaces/talents/
```

Talent 枚举位于：

```text
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java
```

### 普通 Talent

普通 Talent 的图片名由枚举名自动转换，不需要声明图标字段。

1. 声明枚举：

```java
MOON_BLESSING,
```

如果最大点数不是默认的 2，在构造参数中只写最大点数：

```java
MOON_BLESSING(3),
```

该数字是最大点数，不是图标编号。

2. 添加图片：

```text
core/src/main/assets/interfaces/talents/moon_blessing.png
```

3. 正常构建游戏，或运行 `./gradlew generateRuntimeAtlasManifests`。不需要编辑 manifest。

推荐 Talent 图片尺寸为 16x16。Talent 图标不会应用 Item 的透明外接矩形裁剪。

### 动态 Talent 图标

只有确实需要根据职业或状态切换图片时，才覆盖或扩展 `Talent.icon()` 的业务逻辑。返回值必须是普通资源 ID：

```java
public String icon() {
    return empowered ? "moon_blessing_empowered" : "moon_blessing";
}
```

两个名称都必须有 PNG；manifest 条目会自动生成。不要返回数字，也不要让 Runtime Atlas 判断英雄职业或游戏状态。

`developing.png` 是 Talent atlas 的 fallback。缺失普通图标时游戏会显示它并记录错误，但资源校验器仍会失败。

骰子法师 Talent 的 Slice&Dice 致敬文本属于显示文本，不是资源 ID，不要因图片命名修改这些文本。

## 新增 Buff 贴图

Buff 使用两个独立目录：

```text
core/src/main/assets/interfaces/buffs/small/
core/src/main/assets/interfaces/buffs/large/
```

名称常量位于：

```text
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/BuffIndicator.java
```

### 操作步骤

假设新增 Buff 图标 `Moon Shield`，资源 ID 使用 `moon_shield`。

1. 在 `BuffIndicator` 中声明：

```java
public static final String MOON_SHIELD = "moon_shield";
```

2. 同时添加 small 和 large 图片：

```text
core/src/main/assets/interfaces/buffs/small/moon_shield.png
core/src/main/assets/interfaces/buffs/large/moon_shield.png
```

推荐尺寸：

- small：7x7
- large：16x16

两个目录必须具有完全相同的资源名称集合。

3. 修改 Buff 的图标方法：

```java
@Override
public String icon() {
    return BuffIndicator.MOON_SHIELD;
}
```

隐藏状态栏图标时返回：

```java
return BuffIndicator.NONE;
```

不要返回 `0`。`NONE` 对应两个目录中的透明 `none.png`。

4. 正常构建游戏，或运行 `./gradlew generateRuntimeAtlasManifests`。两个 manifest 都会自动更新。

Buff 图标不会应用 Item 的透明外接矩形裁剪。small 和 large 图片应在各自画布中按最终显示位置制作。

## 常见错误

### 游戏显示 fallback

检查以下内容：

- Java 返回的字符串是否拼写正确。
- PNG 文件名是否全部小写。
- PNG 是否位于正确目录。
- 是否在添加 PNG 后重新构建或运行了 manifest 生成任务。

Item fallback 是 `something.png`，Talent fallback 是 `developing.png`，Buff fallback 是 `none.png`。

### 校验器报告 missing Java resources

Java 已声明或引用该资源，但对应目录缺少 PNG。补充 PNG，不要删除 Java 名称来绕过错误。

### 校验器报告 unexpected resources

Talent 和 Buff 目录中存在没有对应 Java 名称的 PNG。删除无用 PNG，或补充正确的枚举/常量。

Item 目录允许保留由其他物品图标流程产生的资源；这些 PNG 也会被自动写入 manifest。

### 修改图片后仍显示旧内容

Runtime Atlas 会缓存生成的纹理页。开发时修改资源后，应重新启动游戏，或使用调试设置中的纹理重载并重置场景。

不要重新创建 `items.png`、`talent_icons.png`、`buffs.png` 或 `large_buffs.png`，也不要重新引入数字索引和 `TextureFilm`。
