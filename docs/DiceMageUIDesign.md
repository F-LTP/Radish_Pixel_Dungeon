# Radish Pixel Dungeon UI 设计与布局方案

## 一、概述

Radish Pixel Dungeon 的 UI 系统基于 Noosa 游戏框架构建，采用 Component（组件）层叠式布局架构。所有 UI 元素继承自 `com.watabou.noosa.ui.Component`，通过 `createChildren()` 创建子组件，`layout()` 方法计算位置和尺寸。

## 二、核心组件架构

### 2.1 Component 基类

位置：`SPD-classes/src/main/java/com/watabou/noosa/ui/Component.java`

Component 是所有 UI 组件的基类，继承自 Group（可包含多个 Visual 对象）。

```java
public class Component extends Group {
    protected float x, y;          // 左上角坐标
    protected float width, height; // 尺寸
    
    // 关键方法
    protected void createChildren() {}  // 创建子组件（构造时调用）
    protected void layout() {}          // 布局计算（位置/尺寸变化时调用）
    
    // 链式设置方法
    public Component setPos(float x, float y);
    public Component setSize(float width, float height);
    public Component setRect(float x, float y, float width, float height);
}
```

**布局原则：**
- `setRect()` / `setPos()` / `setSize()` 会触发 `layout()` 调用
- 子类必须重写 `layout()` 来定位子组件
- 坐标系统：左上角为原点 (0,0)，y 轴向下

### 2.2 Button 交互组件

位置：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/Button.java`

Button 继承 Component，添加 PointerArea 处理触摸/鼠标事件。

```java
public class Button extends Component {
    protected PointerArea hotArea;  // 触摸区域
    
    // 交互回调
    protected void onClick() {}        // 左键点击
    protected void onRightClick() {}   // 右键点击
    protected void onLongClick() {}    // 长按
    protected void onPointerDown() {}  // 按下
    protected void onPointerUp() {}    // 抬起
    
    // layout() 中必须设置 hotArea 的位置
    protected void layout() {
        hotArea.x = x;
        hotArea.y = y;
        hotArea.width = width;
        hotArea.height = height;
    }
}
```

### 2.3 Window 窗口系统

位置：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/Window.java`

Window 是所有弹窗的基类，管理独立的 Camera 和 Chrome（九宫格背景）。

```java
public class Window extends Group {
    protected int width, height;        // 窗口尺寸
    protected NinePatch chrome;         // 九宫格背景
    protected ShadowBox shadow;         // 阴影效果
    protected PointerArea blocker;      // 背景遮罩（点击关闭）
    
    // 常用颜色常量
    public static final int TITLE_COLOR = 0xFFFF44;  // 标题黄色
    public static final int WHITE = 0xFFFFFF;
}
```

**窗口布局流程：**
1. 构造函数创建 chrome、shadow、blocker
2. 子类调用 `add()` 添加内容组件
3. 最后调用 `resize(width, height)` 确定最终尺寸
4. 窗口居中显示，由独立 Camera 管理

## 三、核心 UI 组件详解

### 3.1 StatusPane 状态面板

位置：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/StatusPane.java`

StatusPane 是游戏主界面顶部的 HUD 面板，显示英雄头像、血条、经验条、等级等信息。

#### 3.1.1 核心结构

```java
public class StatusPane extends Component {
    // 背景
    private NinePatch bg;
    
    // 头像区域
    private Image avatar;
    private Button heroInfo;          // 点击头像打开英雄信息
    
    // 血条
    private Image rawShielding;       // 护盾溢出部分（半透明）
    private Image shieldedHP;         // 护盾覆盖的血量
    private Image hp;                 // 当前血量
    private BitmapText hpText;        // 血量数字
    
    // Vitae 系统
    private Image vitae;
    private BitmapText vitaeText;
    
    // 经验条
    private Image exp;
    private BitmapText expText;
    private BitmapText level;         // 等级数字
    
    // 其他
    private BuffIndicator buffs;      // Buff 图标
    private Compass compass;          // 指南针
    private BusyIndicator busy;       // 忙碌指示器
    private CircleArc counter;        // 回合计数器
}
```

#### 3.1.2 血条渲染机制（核心问题）

**标准模式（非骰子法师）：**

血条使用 Image 的 `scale.x` 属性实现横向缩放：

```java
// update() 中的血量计算
hp.scale.x = Math.max(0, (health - shield) / (float)max);
shieldedHP.scale.x = health / (float)max;
rawShielding.scale.x = shield > health ? Math.min(1, shield / (float)max) : 0;
```

**纹理坐标：**
- 大模式：`(0, 103, 128, 9)` - 来自 status.png
- 小模式：`(0, 36, 50, 4)` - 同一贴图

**颜色叠加：**
```java
hp.hardlight(DiceMageUI.CREAM);         // 血量 - 奶油色
shieldedHP.hardlight(DiceMageUI.BLUE);   // 护盾 - 蓝色
rawShielding.hardlight(DiceMageUI.PURPLE); // 护盾溢出 - 紫色
```

**布局位置：**
```java
// layout() 中
hp.x = shieldedHP.x = rawShielding.x = x + 30;
hp.y = shieldedHP.y = rawShielding.y = large ? y + 19 : y + 3;
```

#### 3.1.3 骰子法师模式

骰子法师激活时，使用 Slice&Dice 风格的卡片式 UI：

```java
private DiceMageUI.Frame diceCard;      // 卡片背景
private RenderedTextBlock diceName;      // 角色名
private RenderedTextBlock diceLvl;       // 等级
private DiceMageUI.HealthPips diceHp;    // 血量格子条
```

**切换逻辑：**
```java
// layout() 中根据 DiceMageUI.active() 切换
if (dice) {
    // 隐藏旧 UI
    bg.visible = false;
    hp.visible = shieldedHP.visible = rawShielding.visible = false;
    hpText.visible = false;
    
    // 显示 S&D 卡片
    diceCard.visible = diceName.visible = diceLvl.visible = diceHp.visible = true;
}
```

### 3.2 HealthBar 通用血条

位置：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/HealthBar.java`

HealthBar 是一个简单的血条组件，用于 CharHealthIndicator（角色头顶血条）。

```java
public class HealthBar extends Component {
    private static final int COLOR_BG   = 0xFFCC0000;    // 背景（深红）
    private static final int COLOR_HP    = 0xFF00EE00;   // 血量（绿色）
    private static final int COLOR_SHLD  = 0xFFBBEEBB;   // 护盾（浅绿）
    
    private ColorBlock Bg;      // 背景色块
    private ColorBlock Shld;    // 护盾色块
    private ColorBlock Hp;      // 血量色块
    
    // 设置血量百分比
    public void level(float health, float shield) {
        this.health = health;
        this.shield = shield;
        layout();
    }
}
```

**布局特点：**
- 使用 `ColorBlock` 纯色块，非纹理
- 通过 `size(width * percentage, height)` 控制宽度
- 像素对齐：`Math.ceil(health * pixelWidth) / pixelWidth`

### 3.3 DiceMageUI.HealthPips 格子血条

位置：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/DiceMageUI.java`

Slice&Dice 风格的血量格子显示，每个格子代表固定血量。

```java
public static class HealthPips extends Component {
    private Image[] pips;  // 格子数组
    
    // 每格代表10点血
    private static final int PIP_PER_HP = 10;
    private static final int PIP_W = 3;    // 格子宽度
    private static final int PIP_H = 3;    // 格子高度
    private static final int PIP_GAP = 1;  // 格子间距
    
    public void level(int hp, int shield, int max) {
        int count = pipCount(max);  // 计算需要的格子数
        // 创建/更新格子
        // 根据血量/护盾状态染色
        for (int i = 0; i < count; i++) {
            if (i < filled) {
                pips[i].hardlight(RED);      // 有血
            } else if (i < filled + shldPips) {
                pips[i].hardlight(BLUE);      // 有护盾
            } else {
                pips[i].hardlight(GREY_LINE); // 空格
            }
        }
    }
}
```

**纹理来源：**
- 贴图集路径：`snd/atlas_image.png`
- 坐标：`(119, 480)` - 3x3 像素的竖条纹理

## 四、布局模式详解

### 4.1 大/小模式切换

StatusPane 根据屏幕尺寸切换 large 模式：

```java
// 构造函数
public StatusPane(boolean large) {
    this.large = large;
    
    if (large) {
        bg = new NinePatch(asset, 0, 64, 41, 39, 33, 0, 4, 0);
    } else {
        bg = new NinePatch(asset, 0, 0, 128, 36, 85, 0, 45, 0);
    }
}
```

**尺寸差异：**

| 属性 | Large 模式 | Small 模式 |
|------|-----------|-----------|
| 面板高度 | 39px | 32px |
| 血条尺寸 | 128×9 | 50×4 |
| 头像位置 | (15, 15) | (15, 16) |
| 血条位置 | (x+30, y+19) | (x+30, y+3) |

### 4.2 横竖屏适配

窗口类通过 `PixelScene.landscape()` 检测方向：

```java
int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

// 常用窗口尺寸
protected static final int WIDTH_P = 120;  // 竖屏
protected static final int WIDTH_L = 144;  // 横屏
```

**WndOptions 特殊处理：**
```java
// 横屏且两个选项时使用骰子牌布局
boolean twoCards = options.length == 2 && PixelScene.landscape();
if (twoCards) {
    width = 164;
    int cardWidth = (width - MARGIN) / 2;
    // 两个按钮并排显示
}
```

### 4.3 NinePatch 九宫格背景

UI 背景使用 NinePatch 实现可伸缩的边框：

```java
// Chrome.Type.WINDOW 标准窗口背景
NinePatch chrome = Chrome.get(Chrome.Type.WINDOW);
chrome.size(width + marginHor(), height + marginVer());

// 九宫格参数：左边距、上边距、右边距、下边距
bg = new NinePatch(asset, 0, 64, 41, 39, 33, 0, 4, 0);
```

## 五、Slice&Dice 风格 UI

### 5.1 调色板

DiceMageUI 定义了 S&D 风格的颜色常量：

| 常量 | 颜色值 | 用途 |
|------|--------|------|
| DARK | 0x120F17 | 主背景 |
| BLACK | 0x09070B | 极暗背景/卡片底色 |
| PANEL | 0x211A20 | 面板背景 |
| PANEL_ALT | 0x2A2022 | 交替面板 |
| CREAM | 0xF1E5B5 | 文本/血量 |
| GOLD | 0xB59E09 | 标题/等级 |
| RED | 0xAD1F1F | 血量/危险 |
| BLUE | 0x217B91 | 护盾/冰霜 |
| PURPLE | 0x6A4484 | 神秘/未鉴定 |
| GREEN | 0x388044 | 治疗 |
| GREY_LINE | 0x51464D | 边框/分割线 |

### 5.2 Frame 卡片组件

Frame 是 S&D 风格的基础卡片容器：

```java
public static class Frame extends Component {
    private ColorBlock fill;       // 内部填充
    private ColorBlock top, bottom, left, right;  // 1px 边框
    
    public Frame(int fillColor, int lineColor) {
        // 创建背景和四边
    }
    
    protected void layout() {
        fill.size(width, height);
        top.size(width, 1);        // 上边框
        bottom.y = y + height - 1; // 下边框
        left.size(1, height);      // 左边框
        right.x = x + width - 1;   // 右边框
    }
}
```

### 5.3 DiceButton 按钮

DiceButton 替代 RedButton 用于 S&D 风格：

```java
public static class DiceButton extends Button {
    private Frame bg;
    private RenderedTextBlock text;
    
    // 按下时边框变金色
    protected void onPointerDown() {
        bg.top.hardlight(GOLD);
        bg.bottom.hardlight(GOLD);
        bg.left.hardlight(GOLD);
        bg.right.hardlight(GOLD);
    }
}
```

### 5.4 门控模式

所有 S&D 风格 UI 通过 `DiceMageUI.active()` 门控：

```java
public static boolean active() {
    return Dungeon.hero != null && 
           Dungeon.hero.subClass == HeroSubClass.DICE_MAGE;
}

// 在 layout() 中切换
if (DiceMageUI.active()) {
    // 显示 S&D 风格
} else {
    // 显示标准风格
}
```

## 六、InventorySlot 背包格子

### 6.1 结构

InventorySlot 继承 ItemSlot，添加背景和边框：

```java
public class InventorySlot extends ItemSlot {
    private ColorBlock bg;           // 背景色块
    private ColorBlock lineTop;      // 四边框
    private ColorBlock lineBottom;
    private ColorBlock lineLeft;
    private ColorBlock lineRight;
}
```

### 6.2 动态背景色

根据物品状态切换背景：

```java
public void item(Item item) {
    boolean diceMage = DiceMageUI.active();
    
    if (item != null) {
        boolean equipped = item.isEquipped(Dungeon.hero);
        
        if (diceMage) {
            // 骰子法师：黑色/交替面板背景
            bg.texture(TextureCache.createSolid(
                equipped ? DiceMageUI.PANEL_ALT : DiceMageUI.BLACK
            ));
            hardlightLines(DiceMageUI.itemLineColor(item, equipped));
        } else {
            // 标准：半透明背景
            bg.texture(TextureCache.createSolid(
                equipped ? EQUIPPED : NORMAL
            ));
        }
    }
}
```

### 6.3 ColorBlock 透明度问题

**重要：** ColorBlock 的颜色必须包含完整的 Alpha 通道：

```java
// 错误：缺少 Alpha 位，渲染时完全透明
private static final int NORMAL = 0x9953564D;  // ARGB 格式

// 正确：显式添加 Alpha
lineTop = new ColorBlock(1, 1, 0xFF000000 | DiceMageUI.GREY_LINE);
```

`TextureCache.createSolid(int color)` 将输入视为 ARGB，转换为 RGBA：
- 如果颜色缺少 Alpha 位（如 `0x51464D`），转换后 Alpha=0，完全透明
- 修复：总是传递完整 Alpha 的颜色（`0xFF000000 | color`）

## 七、血条渲染问题诊断

### 7.1 已知问题

1. **ColorBlock 透明度**
    - 问题：`GREY_LINE` 等颜色缺少 Alpha 位导致边框不可见
    - 解决：使用 `0xFF000000 | color` 确保完整 Alpha

2. **Image.scale 缩放方向**
    - 问题：`hp.scale.x` 水平缩放时可能因锚点位置导致偏移
    - 注意：Image 默认锚点在左上角，缩放从左向右

3. **HealthPips 位置计算**
    - 问题：格子位置在 `level()` 中计算，但 `setRect()` 已在 `layout()` 中调用
    - 需确保顺序：先 `setRect()` 定位，再 `level()` 渲染

### 7.2 调试建议

1. **可视化边界**
   ```java
   // 临时添加调试边框
   ColorBlock debug = new ColorBlock(width, height, 0xFFFF0000);
   add(debug);
   ```

2. **检查 scale 值**
   ```java
   // 验证血条缩放
   System.out.println("HP scale: " + hp.scale.x + ", width: " + hp.width);
   ```

3. **验证纹理加载**
   ```java
   // 检查贴图是否存在
   Image test = new Image(Assets.Interfaces.STATUS);
   System.out.println("Texture loaded: " + test.texture != null);
   ```

## 八、最佳实践

### 8.1 组件开发流程

1. 继承 Component 或其子类
2. 在 `createChildren()` 中创建子组件
3. 在 `layout()` 中定位子组件
4. 使用 `PixelScene.align()` 像素对齐
5. 通过 `add()` 将组件加入父容器

### 8.2 布局注意事项

1. **尺寸设置顺序**
    - 先设置父容器尺寸
    - 再调用子组件的 `setRect()` / `setPos()`
    - 最后触发 `layout()` 重新计算

2. **像素对齐**
   ```java
   PixelScene.align(avatar);  // 确保整数像素位置
   ```

3. **动态尺寸**
   ```java
   // 文本需要 measure() 后才能获取正确尺寸
   text.measure();
   float textWidth = text.width();
   ```

### 8.3 S&D 风格扩展

新增 S&D 风格 UI 时：

1. 在组件中添加 `DiceMageUI.Frame` 作为背景
2. 使用 `DiceMageUI.active()` 门控显示
3. 文本使用 `DiceMageUI.CREAM` / `DiceMageUI.GOLD` 颜色
4. 按钮使用 `DiceMageUI.DiceButton` 替代 `RedButton`

## 九、关键文件索引

| 文件 | 说明 |
|------|------|
| `ui/StatusPane.java` | 主 HUD 状态面板 |
| `ui/HealthBar.java` | 通用血条组件 |
| `ui/DiceMageUI.java` | S&D 风格工具类 |
| `ui/Window.java` | 窗口基类 |
| `ui/Button.java` | 按钮基类 |
| `ui/StyledButton.java` | 带样式的按钮 |
| `ui/InventorySlot.java` | 背包格子 |
| `ui/ItemSlot.java` | 物品格子基类 |
| `windows/WndInfoMob.java` | 怪物信息窗口 |
| `windows/WndInfoItem.java` | 物品信息窗口 |
| `windows/WndMessage.java` | 消息窗口 |
| `windows/WndOptions.java` | 选项窗口 |
| `noosa/ui/Component.java` | 组件基类 |

---

*文档版本：2026-01-21*
*适用项目：Radish Pixel Dungeon