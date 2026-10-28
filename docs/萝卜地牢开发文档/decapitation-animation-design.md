# 斩首刀砍头动画设计

## 目标

斩首刀命中有头的生物并将其斩杀时，播放独立的砍头效果：身体留在原地保持 idle，头部从身体分离后飞出、旋转并受重力影响落地，同时从颈部喷出大量血液。

## 有头生物接口

在 `CharSprite` 增加可覆写接口：

```java
public int headHeight() {
    return -1;
}
```

返回值含义：

- `-1`：该生物没有可斩首的头部，使用普通死亡效果。
- `0` 以上：从当前贴图帧顶部向下的头部高度，单位为贴图原始像素。

具体生物 Sprite 自行覆写，例如：

```java
@Override
public int headHeight() {
    return 6;
}
```

该字段只描述贴图，不参与角色碰撞或战斗逻辑。

## 效果结构

增加独立的 `DecapitationEffect`，在死亡瞬间记录原 Sprite 的贴图、当前帧、位置、缩放、翻转和颜色，并创建两个视觉对象：

```text
DecapitationEffect
|- body: 同一 Sprite，Shader 只显示切割线以下部分
`- head: 同一 Sprite，Shader 只显示切割线以上部分
```

两个对象都使用原始角色贴图，不生成临时 Bitmap。原角色 Sprite 可以从 Actor 系统移除，效果对象独立负责后续渲染。

## 裁切 Shader

现有 `cut.vert`、`cut.frag` 和 `CutShader` 可作为参数和局部 UV 计算参考，但斩首应增加独立的裁切模式，而不是复用当前的裂痕扩散逻辑。

建议参数：

```glsl
uniform float uCutY;   // 归一化切割高度
uniform float uPart;   // 0 = body, 1 = head
```

片元逻辑：

```glsl
vec2 localUV = (vUV - uFrame.xy) / uFrame.zw;

if (uPart > 0.5 && localUV.y > uCutY) discard;
if (uPart <= 0.5 && localUV.y < uCutY) discard;
```

`uCutY` 根据 `headHeight / 当前帧高度` 计算。需要确认图集 UV 的 Y 方向；如果贴图坐标从底部开始，应对 `uCutY` 做反转。

## 地面高度

地面高度在效果创建时记录，不实时查询地图地形：

```java
float groundY = originalSprite.y + originalSprite.height();
```

这样可以保留大型生物、浮空偏移和死亡瞬间的视觉位置。头部落地时使用可见头部高度换算其底部位置：

```java
float visibleHeadHeight = headHeight * Math.abs(sprite.scale.y);
float headBottom = head.y + visibleHeadHeight;
```

旋转后的头部不必计算精确旋转矩形，使用头部可见区域的圆形半径近似即可，避免像素动画落地时抖动。

## 头部物理

头部对象维护位置、速度和角速度：

```java
velocity.y += gravity * elapsed;
head.x += velocity.x * elapsed;
head.y += velocity.y * elapsed;
head.angle += angularVelocity * elapsed;
```

初速度根据攻击者与受害者的水平相对位置决定：

```java
velocity.x = victim.x < attacker.x ? -horizontalSpeed : horizontalSpeed;
velocity.y = -verticalSpeed;
```

落地后执行衰减反弹：

```java
head.y = groundY - visibleHeadHeight;
velocity.y = -velocity.y * 0.35f;
velocity.x *= 0.65f;
angularVelocity *= 0.6f;
```

速度低于阈值后停止，随后与身体一起淡出并销毁效果。

## 身体表现

身体对象：

- 保持原地位置和原 Sprite 缩放。
- 播放或循环 idle 动画。
- Shader 隐藏切割线以上区域。
- 不再绑定已死亡的 `Char`，避免影响 Actor 状态机。

## 血液效果

切割瞬间以切割线为中心生成大量 `BloodParticle` 或 `Splash`：

- 使用 `sprite.blood()` 作为颜色。
- 播放一次大范围血液爆发。
- 颈部额外持续喷血约 0.4 至 0.7 秒。
- 无血生物可通过覆写 `blood()` 返回特殊颜色或 0 来抑制血液。

## 触发和兼容性

- 只有斩首刀造成最终击杀，且 `target.sprite.headHeight() >= 0` 时触发。
- 无头生物、Boss 特殊死亡流程、已经由其他死亡 Shader 接管的目标继续使用原流程。
- 斩首效果只处理渲染，不修改 HP、死亡判定、掉落或 Actor 调度。
- 应在死亡动画/死亡 Shader 入口之前决定是否接管，避免两个效果同时控制同一个 Sprite。
- 所有 Shader 创建和视觉对象添加必须发生在渲染线程。

## 实现顺序

1. 在 `CharSprite` 增加 `headHeight()` 默认接口。
2. 为需要斩首的生物 Sprite 配置头部高度。
3. 增加裁切 Shader 的 head/body 模式和对应 Java 封装。
4. 实现 `DecapitationEffect` 的双对象渲染和物理更新。
5. 接入斩首刀的最终击杀分支。
6. 加入血液喷射、落地反弹、淡出和清理逻辑。
7. 检查翻转、不同帧尺寸、大型生物、浮空生物和无血生物。
