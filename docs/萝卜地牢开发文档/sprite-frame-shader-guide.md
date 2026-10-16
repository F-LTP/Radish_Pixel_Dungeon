# Sprite 逐帧 Shader 动画

本文说明如何像 `WandererSprite` 的 affix 一样，为 Sprite 的每个动画帧附加 Shader 及其参数。该方案不引入新的动画系统：继续使用现有的 `Animation`、`curAnim` 和 `curFrame`，只在帧切换时同步视觉附加数据。

## 设计目标

- 普通 Sprite 不配置 Shader 时保持现有行为。
- 每个动画帧可以选择不同的 Shader 类型。
- 每个帧可以拥有独立的 `progress`、`time` 和 Shader 专用参数。
- Shader 对象可以复用，不在每次切帧时创建新的 `Gizmo`。
- 死亡动画只是普通多帧动画，不再需要单独的“死亡 Shader”流程。

## 帧参数对象

在 `effects` 或 `sprites` 包中增加不可变的帧参数对象：

```java
public final class ShaderFrame {
    public final ShaderEffect.ShaderType type;
    public final float progress;
    public final float time;
    public final float[] values;

    public ShaderFrame(ShaderEffect.ShaderType type,
                       float progress,
                       float time,
                       float... values) {
        this.type = type;
        this.progress = progress;
        this.time = time;
        this.values = values;
    }
}
```

`null` 表示该帧不使用 Shader。`values` 的含义由具体 Shader 类型约定；如果参数数量逐渐增多，可以改为专用的 `ShaderParams` 对象。

## 在 Sprite 中声明帧数据

每个 Sprite 按动画分别声明数组：

```java
private static final ShaderFrame[] DIE_SHADER_FRAMES = {
        new ShaderFrame(ShaderEffect.ShaderType.CUT, 0.00f, 0.00f),
        new ShaderFrame(ShaderEffect.ShaderType.CUT, 0.35f, 0.08f),
        new ShaderFrame(ShaderEffect.ShaderType.BURN, 0.65f, 0.16f, 0.7f),
        new ShaderFrame(ShaderEffect.ShaderType.ALPHA, 1.00f, 0.24f)
};
```

建议让数组长度与对应 `Animation.frames.length` 一致。长度不一致时应按最小长度读取，避免资源热修或动画调整导致越界。

## 根据当前帧读取 Shader

在 `CharSprite` 中增加可覆写入口：

```java
protected ShaderFrame shaderFrameFor(Animation animation, int frame) {
    return null;
}
```

具体 Sprite 只需要声明自己关心的动画：

```java
@Override
protected ShaderFrame shaderFrameFor(Animation animation, int frame) {
    if (animation == die && frame < DIE_SHADER_FRAMES.length) {
        return DIE_SHADER_FRAMES[frame];
    }
    return null;
}
```

## 帧切换检测

仿照 `WandererSprite` 的 affix 更新逻辑，在 `CharSprite` 中缓存上一次动画和帧：

```java
private Animation lastShaderAnimation;
private int lastShaderFrame = -1;

protected void updateShaderFrame() {
    if (curAnim == lastShaderAnimation && curFrame == lastShaderFrame) {
        return;
    }

    lastShaderAnimation = curAnim;
    lastShaderFrame = curFrame;
    applyShaderFrame(shaderFrameFor(curAnim, curFrame));
}
```

在 `update()` 中调用 `updateShaderFrame()`。如果项目中已有统一的动画帧变化回调，优先放入该回调，避免在多个 Sprite 中重复检测。

## 复用 ShaderEffect

逐帧 Shader 不应调用一次性入口 `ShaderEffect.apply(...)`，因为该入口会启动独立计时器并可能销毁 Sprite。增加一个只更新当前绘制状态的方法：

```java
public void applyFrame(ShaderFrame frame) {
    // Shader 类型变化时切换程序；类型不变时只更新 uniforms。
    // progress、time 和 values 在 drawWithShader() 前上传。
}
```

Sprite 侧应复用同一个 `ShaderEffect`：

```java
protected void applyShaderFrame(ShaderFrame frame) {
    if (frame == null) {
        if (shaderEffect != null) shaderEffect.clearFrame();
        return;
    }

    if (shaderEffect == null) {
        shaderEffect = new ShaderEffect(this, frame.type, Float.MAX_VALUE);
        if (parent != null) parent.add(shaderEffect);
    }

    shaderEffect.applyFrame(frame);
}
```

切换到 `null` 时必须清除 Shader，否则上一帧的 Shader 会残留到下一个动画。

## 与 affix 同时使用

如果同一帧既有 affix 又有 Shader，可以继续使用两个数组；更推荐使用一个组合对象，避免数组错位：

```java
public final class FrameVisual {
    public final AffixSpec affix;
    public final ShaderFrame shader;
}
```

帧切换时按固定顺序处理：先更新贴图，再更新 affix，最后更新 Shader。这样 Shader 渲染的一定是当前帧的最终 Sprite 图像。

## 死亡动画

死亡效果直接配置到 `die` 动画和 `DIE_SHADER_FRAMES` 中：

```java
die = new Animation(0.08f, false,
        frame0, frame1, frame2, frame3);
```

`CharSprite.die()` 仍负责播放 `die`，动画结束和 `killAndErase()` 仍沿用现有流程。Shader 只改变当前帧的绘制，不负责决定角色何时死亡或 Sprite 何时销毁。

因此应避免再把逐帧效果接入 `pendingDeathAfterShader`、`dieAfterShader()` 或 `ShaderEffect.apply(...)`。

## 参数上传建议

当前 `ShaderEffect.uploadShaderParams()` 按 Shader 类型分支上传参数。短期可以继续使用该结构；新增参数时保持每种 Shader 的参数集中处理。若参数种类变多，再引入：

```java
public interface ShaderProgramAdapter {
    void upload(ShaderFrame frame);
}
```

不要在 Sprite 中直接操作 GLSL uniform，Sprite 只提供帧数据，ShaderEffect 负责渲染细节。

## 检查清单

- `ShaderFrame[]` 长度是否与动画帧数量匹配。
- 动画切换时是否清除了上一动画遗留的 Shader。
- Shader 类型不变时是否复用了 `ShaderEffect`。
- 不使用 Shader 的帧是否返回 `null`。
- `die` 动画是否设置为非循环。
- Shader 是否只影响绘制，不改变 Actor 的死亡状态机。
- 所有 GPU 状态更新是否发生在渲染线程。

