# 动态 Runtime Atlas 系统重构计划

## 一、背景与目标

当前项目中的物品、天赋和 Buff 图标采用预先拼接的大图，并通过数字索引定位：

- 物品使用 `sprites/items.png`、`ItemSpriteSheet` 和 `TextureFilm`。
- 天赋使用 `interfaces/talent_icons.png` 和 `Talent.icon()` 的数字索引。
- Buff 使用 `interfaces/buffs.png`、`interfaces/large_buffs.png` 和 `BuffIndicator` 的数字常量。

这种结构要求开发者手工维护 atlas 坐标和数字编号。增加或调整图片时容易产生编号冲突、空洞和错误引用，也无法通过文件名直接判断代码使用的资源。

本次重构的目标是：

- 将每个图标保存为独立 PNG。
- 游戏运行时将同一目录中的 PNG 动态拼接为 atlas。
- Java 代码通过稳定的字符串名称获取图片。
- Item、Talent 和 Buff 使用完全相同的底层 atlas 实现。
- 三个业务系统只负责提供各自的图片名称和资源目录。
- 怪物、英雄和 NPC sprite 不进入本系统。
- 保持现有业务调用形式，避免人工修改数百个用例。

## 二、已确定的设计原则

### 2.1 底层只有一套 atlas

禁止分别实现 `ItemAtlas`、`TalentAtlas` 和 `BuffAtlas`。底层只包含通用组件：

```text
RuntimeAtlas
RuntimeAtlasRegistry
AtlasFrame
AtlasSource
```

`RuntimeAtlas` 不得依赖 `Item`、`Talent`、`Buff`、`ItemSpriteSheet` 或 `BuffIndicator`。它只接收目录、帧名称和少量通用配置。

系统之间允许不同的只有资源目录和 fallback 名称。扫描、校验、装箱、分页、纹理创建、查询、释放和重建规则必须完全一致。

### 2.2 业务层负责名称和目录

三个业务系统可以提供自己的名称方法：

```java
Item.imageName()
Talent.iconName()
Buff.iconName()
```

它们也可以提供或选择自己的 atlas 目录，但不能自行加载、拼接或解析图片。

调用最终统一为：

```java
RuntimeAtlasRegistry.get(directory).frame(name)
```

推荐使用具体方法名而不是笼统的 `getName()`，避免和本地化显示名称混淆。

### 2.3 统一命名规则

三个系统统一采用：

```text
Java 符号名转小写 = PNG 文件名（不含扩展名）
```

示例：

```text
ItemSpriteSheet.POTION_HEALING -> potion_healing.png
Talent.HEARTY_MEAL             -> hearty_meal.png
BuffIndicator.MIND_VISION      -> mind_vision.png
```

资源 ID 仅允许：

```regex
[a-z0-9_]+
```

转换必须使用 `Locale.ROOT`，不能依赖设备语言环境。

禁止在运行时根据文件顺序生成数字 ID，禁止维护数字到字符串的兼容映射，禁止为某个业务系统增加专用命名规则。

### 2.4 源代码兼容，而非二进制兼容

项目会重新编译，因此不要求兼容已经编译的旧 class。兼容目标是让现有 Java 调用尽量不变。

例如现有代码：

```java
image = ItemSpriteSheet.POTION_HEALING;
return BuffIndicator.POISON;
new TalentIcon(Talent.HEARTY_MEAL);
```

迁移后仍保持相同写法，只改变常量、字段和方法的静态类型。

## 三、目标资源结构

建议资源目录如下：

```text
core/src/main/assets/
├── sprites/
│   └── items/
│       ├── manifest.txt
│       ├── something.png
│       ├── potion_healing.png
│       └── sword.png
└── interfaces/
    ├── talents/
    │   ├── manifest.txt
    │   ├── developing.png
    │   └── hearty_meal.png
    └── buffs/
        ├── small/
        │   ├── manifest.txt
        │   ├── none.png
        │   └── poison.png
        └── large/
            ├── manifest.txt
            ├── none.png
            └── poison.png
```

药水、卷轴、种子、武器、护甲和其他可见物品都属于 `sprites/items`，不再因为物品类别建立不同加载规则。

Buff 的 small 和 large 是两个独立 atlas 实例，但名称集合原则上相同。二者依然使用相同的 `RuntimeAtlas` 类。

每个目录都包含相同格式的 `manifest.txt`。运行时以 manifest 为权威输入，避免 Android APK 或桌面 JAR 中目录枚举行为不同。

## 四、底层数据模型

### 4.1 AtlasSource

`AtlasSource` 表示一个 atlas 的通用配置：

```java
public final class AtlasSource {
    public final String directory;
    public final String fallback;
}
```

第一版只保留真正通用且必要的配置。不要加入 `type = ITEM` 一类业务标记。

### 4.2 AtlasFrame

一个 atlas 可能因设备最大纹理尺寸而拆成多页，因此帧必须同时包含纹理和 UV：

```java
public final class AtlasFrame {
    public final SmartTexture texture;
    public final RectF uv;
    public final int width;
    public final int height;
}
```

不能只返回 `RectF`，也不能假设同一目录永远只生成一张纹理。

### 4.3 RuntimeAtlas

建议接口：

```java
public final class RuntimeAtlas {
    public AtlasFrame frame(String name);
    public boolean contains(String name);
    public int pixel(String name, int x, int y);
    public void invalidate();
    public void dispose();
}
```

`frame()` 负责名称规范化、查询和 fallback。业务 UI 不直接访问内部帧表。

### 4.4 RuntimeAtlasRegistry

注册表按目录或 `AtlasSource` 缓存实例：

```java
RuntimeAtlasRegistry.get(source)
```

它只管理实例和生命周期，不提供 `items()`、`talents()` 等专用加载逻辑。业务层可以声明目录常量以避免路径散落，但这些常量不应进入底层包。

## 五、运行时构建规则

### 5.1 加载和校验

构建 atlas 时按以下顺序执行：

1. 读取目录中的 `manifest.txt`。
2. 校验每一行是合法、规范化的 PNG 相对路径。
3. 检查重复 ID。
4. 检查 manifest 声明的文件是否存在。
5. 加载所有 PNG 为 `Pixmap`。
6. 校验宽高、格式和透明图片等异常情况。
7. 按确定性规则排序并装箱。
8. 创建一个或多个 `SmartTexture`。
9. 建立名称到 `AtlasFrame` 的只读映射。
10. 释放源 `Pixmap` 和临时 atlas `Pixmap`。

正式运行时缺失普通帧应记录一次警告并返回 fallback。fallback 本身缺失时创建内存错误纹理，不能递归查询 fallback。

### 5.2 确定性装箱

装箱结果必须可重复：

- 优先按最大边、面积、名称排序。
- 同样的资源集合必须产生相同布局。
- 帧之间保留至少 1px padding。
- 必要时复制边缘像素，防止采样串色。
- atlas 使用最接近需求的合理尺寸，不固定为现有大图尺寸。
- 查询 OpenGL 最大纹理尺寸。
- 单页无法容纳时自动创建下一页。
- 单张图片超过最大纹理尺寸时明确报错。

现有 `AtlasGenerator` 的二叉树装箱、2048 硬上限和失败后继续生成不完整 atlas 的行为不能保留。

### 5.3 纹理类型

底层必须使用 Noosa 的 `SmartTexture`，并与 `TextureCache` 生命周期协调。不能返回孤立的 libGDX `Texture` 或 `TextureRegion` 给业务层。

纹理过滤默认使用像素画适用的 nearest，wrap 使用 clamp。任何系统都不能单独覆盖这套规则，除非以后把它提升为所有 atlas 共有的显式配置。

## 六、业务层接入

### 6.1 Item

`ItemSpriteSheet` 保留为编译期名称常量表：

```java
public static final String SOMETHING = "something";
public static final String POTION_HEALING = "potion_healing";
public static final String SWORD = "sword";
```

删除数字坐标职责：

```text
xy()
WIDTH
分组基准常量
TextureFilm film
基于常量的加减运算
```

`Item` 修改为：

```java
public String image = ItemSpriteSheet.SOMETHING;

public String imageName() {
    return image;
}
```

如需保留现有 `image()` 方法名，也可令其返回 `String`，但必须避免和字段含义不清。最终只能有一个权威图片名称入口。

`ItemSprite` 的字符串构造器和 `view()` 通过物品目录取得通用 atlas：

```java
RuntimeAtlasRegistry.get(ItemAtlasSource.SOURCE).frame(imageName)
```

`ItemSprite.pick()` 改为按名称读取帧内像素，而不是按整张 `items.png` 的行列计算。

现有 SND 物品图标旁路不在第一阶段强制并入，但不得成为新动态图集的第二套底层实现。后续若迁移，应同样以目录和名称接入 `RuntimeAtlas`。

### 6.2 Talent

Talent 是 enum，默认图片名直接来自枚举名：

```java
public String iconName() {
    return name().toLowerCase(Locale.ROOT);
}
```

迁移后构造器中的数字参数不再表示图标索引，只保留 `maxPoints` 等业务数据：

```java
Talent()
Talent(int maxPoints)
```

`TalentIcon` 只负责指定天赋目录并把 `iconName()` 交给通用 atlas。

动态变体也必须返回普通资源名称。例如 `HEROIC_ENERGY`：

```text
heroic_energy_warrior
heroic_energy_mage
heroic_energy_rogue
heroic_energy_huntress
heroic_energy_rat
```

变体名称由 Talent 业务层决定；底层 atlas 不知道职业或天赋状态。

默认 fallback 为 `developing.png`。骰子法师天赋名称中的 Slice&Dice 致敬文本不属于图片 ID，不得因本次迁移修改。

### 6.3 Buff

`BuffIndicator` 保留为编译期名称常量表：

```java
public static final String POISON = "poison";
public static final String MIND_VISION = "mind_vision";
public static final String ARMOR = "armor";
```

`Buff.icon()` 及所有 override 的返回类型由 `int` 改为 `String`：

```java
public String iconName() {
    return BuffIndicator.NONE;
}
```

为了降低约 229 个 override 的迁移风险，可以保留 `icon()` 方法名并仅改变返回类型。待迁移稳定后再决定是否重命名为 `iconName()`。

`BuffIcon` 根据显示尺寸选择目录：

```java
AtlasSource source = large ? BUFFS_LARGE : BUFFS_SMALL;
RuntimeAtlasRegistry.get(source).frame(buff.icon());
```

这是业务层的目录选择，不是两套加载规则。

`NONE` 建议定义为字符串 `"none"`，small 和 large 目录都提供透明的 `none.png`。显示列表仍可在业务层排除 `NONE`，但 atlas 不需要识别特殊空值。

## 七、转换与校验工具

### 7.1 统一工具链

现有 `tools/convert_itemsheet.py` 已证明“Java 常量名小写对应 PNG”可行，但当前生成代码仍依赖 libGDX `TextureRegion`、旧 `TextureFilm`，且结构解析较脆弱，不能直接作为最终实现。

建议建立统一入口：

```text
tools/convert_runtime_atlas.py
```

它可以包含不同的 Java 符号提取器，因为 ItemSpriteSheet、Talent enum 和 BuffIndicator 的源代码结构不同；但以下逻辑必须共享：

- 标识符转小写。
- PNG 导出和命名。
- manifest 生成。
- 重复索引检测。
- 空帧检测。
- 文件集合校验。
- 报告例外项。

工具只用于从旧 atlas 完成一次性迁移和持续校验。游戏运行时不依赖 Python 工具。

### 7.2 物品现状

截至本计划编写时：

- `ItemSpriteSheet` 中有 515 个去重后的公开数字常量。
- `sprites/items` 中有 500 张 PNG。
- 其中 500 张都能通过常量名小写直接匹配。
- 当前没有无法映射到常量的多余 PNG。
- 未匹配项主要是旧分组常量、飞镖系列和 `DARKSWORD`。

迁移前必须逐项处理未匹配项：分组常量应删除，仍在使用的实际帧应从旧 atlas 补充导出，不得用运行时别名掩盖资源缺失。

### 7.3 Talent 和 Buff 拆分

Talent 图片名取 enum 常量名的小写形式；Buff 图片名取 `BuffIndicator` 常量名的小写形式。

旧数字可能存在空洞、同帧复用和动态选择。转换工具必须生成报告，要求人工确认：

- 同一数字被多个符号引用时，是生成多份同内容 PNG，还是让两个 Java 常量显式使用同一字符串。
- 透明空帧是否为 `none`，还是未实现资源。
- 动态数字返回值对应哪个稳定变体名。

工具不得静默选择别名或覆盖文件。

## 八、生命周期和线程约束

atlas 只能在拥有有效 OpenGL context 的渲染线程创建。

需要覆盖以下生命周期：

- 首次使用时惰性构建。
- 多个 Image 复用同一目录实例。
- `TextureCache.clear()` 后 atlas 同步失效。
- OpenGL context 丢失或恢复后重新构建。
- 应用关闭或资源重载时释放所有页纹理。
- 不重复释放已经由缓存接管的纹理。
- 不让注册表永久持有已删除 GL 纹理。

应先明确 `SmartTexture` 由 `RuntimeAtlas` 还是 `TextureCache` 唯一所有，禁止双重所有权。推荐使用可识别的缓存 key 将运行时生成纹理纳入 `TextureCache`，注册表只管理帧表和失效状态。

## 九、错误处理

开发和静态校验阶段必须报告：

```text
非法资源 ID
manifest 重复项
manifest 文件不存在
目录中 PNG 未写入 manifest
Java 名称没有对应 PNG
PNG 没有对应 Java 名称
fallback 缺失
重复符号或意外别名
单图超过纹理尺寸上限
atlas 分页失败
Pixmap 或纹理创建失败
```

游戏运行时普通帧缺失不得直接崩溃，应返回 fallback 并对同一问题只记录一次日志。开发工具和 CI 校验仍应以非零状态失败，不能因为运行时有 fallback 就允许资源错误进入版本。

## 十、迁移阶段

### 阶段一：通用底层

1. 审计并替换现有未接入的 `AtlasGenerator`。
2. 实现 `AtlasSource`、`AtlasFrame`、`RuntimeAtlas` 和注册表。
3. 实现 manifest、确定性装箱、分页和 fallback。
4. 接入 `SmartTexture`、`TextureCache` 和 GL 生命周期。
5. 使用小型测试资源进行静态和运行时逻辑验证。

### 阶段二：Item

1. 修正统一转换工具的 Item 符号提取。
2. 删除分组基准，补齐仍在使用但未拆出的 PNG。
3. 将 `ItemSpriteSheet` 实际帧常量转换为字符串。
4. 将 `Item.image`、图片名称入口和 `ItemSprite` 改为字符串。
5. 迁移 `ItemSprite.pick()`、Heap 占位图、更新日志、测试工具等直接调用。
6. 清除物品范围内对 `Assets.Sprites.ITEMS` 和数字 `TextureFilm` 的依赖。

### 阶段三：Talent

1. 从旧 `talent_icons.png` 按 enum 名称导出 PNG。
2. 报告并处理索引空洞、重复与动态返回值。
3. 移除 Talent 图标数字字段。
4. 增加统一的 `iconName()`。
5. 修改 `TalentIcon` 使用 talents 目录的通用 atlas。
6. 验证所有天赋和职业动态变体。

### 阶段四：Buff

1. 同时拆分 small 和 large Buff atlas。
2. 校验两个目录的名称集合。
3. 将 `BuffIndicator` 常量转换为字符串。
4. 机械修改 `Buff.icon()` 及所有 override 返回类型。
5. 修改 `BuffIcon` 按大小选择目录并调用通用 atlas。
6. 验证隐藏 Buff、动态 Buff 图标、染色和淡出逻辑。

### 阶段五：清理

1. 删除目标范围内不再使用的旧整图资源。
2. 删除旧数字坐标、`TextureFilm` 和转换期兼容代码。
3. 更新资源贡献说明和新增图标流程。
4. 保留一次完整的资源一致性校验脚本。

每个阶段应独立 review，避免一次改动同时覆盖三个大规模业务系统。

## 十一、静态验收标准

按项目要求，Codex 修改后不执行 Gradle；至少完成以下静态验证：

- Item、Talent、Buff 最终调用同一个 `RuntimeAtlas` 实现。
- 底层代码不 import 三个业务类型。
- 三个系统没有专用装箱、扫描或纹理生命周期代码。
- 所有资源 ID 符合 `[a-z0-9_]+`。
- 每个 manifest 与目录 PNG 集合一致。
- 每个 Java 图片名称都有对应 PNG。
- Talent 默认名称与 enum 名称小写一致。
- Buff small 和 large 名称集合一致，明确允许的例外除外。
- 不存在目标常量上的数字加减运算。
- 不存在目标 atlas 的残留 `TextureFilm`。
- `ItemSprite.pick()` 等像素访问使用帧局部坐标。
- fallback 存在且不会递归失败。
- 动态 atlas 支持多页，不假设单一纹理。
- `TextureCache.clear()` 后不会返回已释放纹理。
- git diff 不修改怪物、英雄和 NPC sprite 加载体系。

## 十二、新增资源的最终流程

重构完成后，新增图片不再编辑 atlas 坐标。

### 新增 Item

```text
1. 添加 sprites/items/new_item.png
2. 在 ItemSpriteSheet 声明 NEW_ITEM = "new_item"
3. 将 new_item.png 写入或自动生成到 manifest
4. 在物品中使用 image = ItemSpriteSheet.NEW_ITEM
```

### 新增 Talent

```text
1. 声明枚举 NEW_TALENT
2. 添加 interfaces/talents/new_talent.png
3. 更新 manifest
```

默认不需要额外声明图片名称。

### 新增 Buff 图标

```text
1. 声明 BuffIndicator.NEW_BUFF = "new_buff"
2. 添加 small/new_buff.png 和 large/new_buff.png
3. 更新两个 manifest
4. Buff.icon() 返回 BuffIndicator.NEW_BUFF
```

新增怪物或装备仍需按项目约定提醒维护者重新生成押韵表；本 atlas 重构不改变该要求。

## 十三、明确不做的事情

- 不迁移怪物、英雄和 NPC sprite。
- 不让 RuntimeAtlas 根据 Java 类类型猜测目录。
- 不保留数字索引作为新系统的长期兼容层。
- 不为 Item、Talent、Buff 编写三套 atlas。
- 不在运行时扫描 Java 常量或使用反射获取字段名。
- 不依赖资源枚举顺序决定名称或布局。
- 不把本地化显示名称用作资源 ID。
- 不在本次重构中顺带改变 Talent、Buff 或 Item 的玩法逻辑。

