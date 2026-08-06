# GLSL 着色器系统设计文档

## 一、概述

本系统为 Radish Pixel Dungeon 提供自定义 GLSL 着色器支持，基于 Noosa 渲染框架和 LibGDX OpenGL 层实现。主要用途是为骰子法师（Dice Mage）提供 Slice&Dice 风格的死亡特效，同时保持架构通用性，便于后续扩展其他着色器效果。

### 1.1 设计原则

- **外部文件存储**：着色器源码存放在 `assets/shaders/` 目录，不再硬编码到 Java 类中
- **统一注册管理**：通过 `Shaders` 类集中管理所有着色器的路径和实例
- **命名约定适配**：自动将 Slice&Dice 命名转换为 Noosa 命名

## 二、技术背景

### 2.1 Noosa 着色器架构

```
┌─────────────────────────────────────────────────────────────┐
│  Noosa 渲染流程                                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Script (抽象基类)                                          │
│      │                                                      │
│      ▼                                                      │
│  NoosaScript (默认着色器)                                    │
│      │                                                      │
│      ├── compile() ── 编译内嵌 GLSL                         │
│      ├── use() ────── 激活着色器程序                        │
│      └── drawQuad() ─ 绘制四边形                            │
│                                                             │
│  扩展点：继承 Script，实现自定义着色器                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Slice&Dice 着色器差异适配

| Slice&Dice (LibGDX) | Noosa 等价 | 说明 |
|---------------------|-----------|------|
| `a_position` | `aXYZW` | 顶点位置属性 |
| `a_texCoord0` | `aUV` | 纹理坐标属性 |
| `v_texCoords` | `vUV` | 传递到片段的纹理坐标 |
| `u_texture` | `uTex` | 纹理采样器 |
| `u_projTrans` | `uCamera * uModel` | 变换矩阵 |

### 2.3 文件结构

```
core/src/main/assets/shaders/
├── cut.vert / cut.frag          # 切割着色器
├── burn.vert / burn.frag        # 燃烧着色器
├── ellipse.vert / ellipse.frag  # 椭圆消失着色器
├── hsl.vert / hsl.frag          # HSL 颜色调整着色器
├── singularity.vert / singularity.frag  # 奇点收缩着色器
├── acid.vert / acid.frag        # 酸蚀溶解着色器
├── wipe.vert / wipe.frag        # 方向擦除着色器
├── alpha.vert / alpha.frag      # 透明淡出着色器
├── noise.vert / noise.frag      # 噪声溶解着色器
└── noise_texture.png            # Slice&Dice 噪声纹理

SPD-classes/src/main/java/com/watabou/glwrap/
└── GlslShaderScript.java        # 通用 GLSL 着色器基类

core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/
├── effects/
│   ├── Shaders.java             # 着色器注册表
│   └── ShaderEffect.java        # 着色器效果控制器（待实现）
│
└── effects/shaders/
    ├── CutShader.java
    ├── BurnShader.java
    ├── EllipseShader.java
    ├── HslShader.java
    ├── SingularityShader.java
    ├── AcidShader.java
    ├── WipeShader.java
    ├── AlphaShader.java
    └── NoiseShader.java
```

## 三、核心类设计

### 3.1 GlslShaderScript（基类）

**位置：** `SPD-classes/src/main/java/com/watabou/glwrap/GlslShaderScript.java`

```java
package com.watabou.glwrap;

import com.watabou.glscripts.Script;
import com.watabou.glwrap.Shader;
import com.watabou.glwrap.Uniform;
import com.watabou.glwrap.Attribute;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * 通用 GLSL 着色器基类。
 * 
 * 从 assets/shaders/{name}.vert 和 assets/shaders/{name}.frag 加载着色器源码，
 * 自动适配 Noosa 命名约定。
 */
public abstract class GlslShaderScript extends Script {

    // ============== 通用 Uniform ==============
    protected Uniform uTime;        // 动画时间
    protected Uniform uAlpha;       // 整体透明度
    protected Uniform uCamera;      // 摄像机矩阵
    
    // ============== 通用 Attribute ==============
    protected Attribute aXYZW;      // 顶点位置
    protected Attribute aUV;        // 纹理坐标
    
    // 着色器名称（用于加载文件）
    private final String shaderName;
    
    /**
     * 构造函数
     * @param shaderName 着色器名称（不含扩展名），如 "cut"、"burn"
     */
    protected GlslShaderScript(String shaderName) {
        this.shaderName = shaderName;
    }
    
    /**
     * 从文件加载顶点着色器源码
     */
    protected String loadVertexShader() {
        FileHandle file = Gdx.files.internal("shaders/" + shaderName + ".vert");
        if (!file.exists()) {
            throw new RuntimeException("Vertex shader not found: " + file.path());
        }
        return adaptVertexShader(file.readString());
    }
    
    /**
     * 从文件加载片段着色器源码
     */
    protected String loadFragmentShader() {
        FileHandle file = Gdx.files.internal("shaders/" + shaderName + ".frag");
        if (!file.exists()) {
            throw new RuntimeException("Fragment shader not found: " + file.path());
        }
        return adaptFragmentShader(file.readString());
    }
    
    /**
     * 子类可覆盖以提供硬编码源码（兼容旧方式）
     */
    protected String vertexShaderSource() {
        return null; // null 表示从文件加载
    }
    
    /**
     * 子类可覆盖以提供硬编码源码（兼容旧方式）
     */
    protected String fragmentShaderSource() {
        return null; // null 表示从文件加载
    }

    /**
     * 构造函数
     * @param shaderName 着色器名称（不含扩展名），如 "cut"、"burn"
     */
    protected GlslShaderScript(String shaderName) {
        this.shaderName = shaderName;
        compile(buildShaderSource());
        
        // 获取通用 uniform/attribute
        uTime = uniform("uTime");
        uAlpha = uniform("uAlpha");
        uCamera = uniform("uCamera");
        aXYZW = attribute("aXYZW");
        aUV = attribute("aUV");
    }
    
    /**
     * 构建合并的着色器源码（vertex + "//\n" + fragment）
     */
    private String buildShaderSource() {
        String vertex = loadVertexShader();
        String fragment = loadFragmentShader();
        return vertex + "//\n" + fragment;
    }

    /**
     * 适配顶点着色器：转换 Slice&Dice 命名为 Noosa 命名
     */
    protected String adaptVertexShader(String src) {
        return src
            .replace("a_position", "aXYZW")
            .replace("a_texCoord0", "aUV")
            .replace("v_texCoords", "vUV");
    }

    /**
     * 适配片段着色器
     */
    protected String adaptFragmentShader(String src) {
        return src
            .replace("v_texCoords", "vUV")
            .replace("u_texture", "uTex");
    }

    /**
     * 设置时间参数（用于动画）
     */
    public void setTime(float time) {
        if (uTime != null) {
            uTime.value1f(time);
        }
    }

    /**
     * 设置透明度
     */
    public void setAlpha(float alpha) {
        if (uAlpha != null) {
            uAlpha.value1f(alpha);
        }
    }

    /**
     * 设置摄像机矩阵
     */
    public void setCamera(float[] matrix) {
        if (uCamera != null) {
            uCamera.valueM4(matrix);
        }
    }
}
```

### 3.2 Shaders（着色器注册表）

**位置：** `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/Shaders.java`

```java
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.BurnShader;
import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.CutShader;
import com.watabou.glscripts.Script;

/**
 * 着色器注册表。
 * 
 * 添加新着色器只需一行：
 * public static final CutShader cut = load(CutShader.class);
 */
public class Shaders {

    // ============== 着色器实例（一行一个）==============
    
    public static final CutShader cut = load(CutShader.class);
    public static final BurnShader burn = load(BurnShader.class);
    
    // ============== 加载方法 ==============
    
    /**
     * 加载着色器实例（延迟初始化）
     */
    private static <T extends GlslShaderScript> T load(Class<T> shaderClass) {
        return Script.use(shaderClass);
    }
    
    /**
     * 预编译所有着色器（可选，避免首次使用延迟）
     */
    public static void init() {
        cut.getClass();
        burn.getClass();
    }
    
    /**
     * 清理所有着色器
     */
    public static void reset() {
        Script.reset();
    }
}
```

### 3.3 CutShader（切割着色器）

**位置：** `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/shaders/CutShader.java`

```java
package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * Slice&Dice 风格切割着色器。
 */
public class CutShader extends GlslShaderScript {

    private Uniform uCutLine;
    private Uniform uCutAlpha;
    private Uniform uCutSide;

    public CutShader() {
        super("cut");  // 对应 shaders/cut.vert 和 shaders/cut.frag
        uCutLine = uniform("uCutLine");
        uCutAlpha = uniform("uCutAlpha");
        uCutSide = uniform("uCutSide");
    }

    public void setCutLine(float startX, float startY, float dirX, float dirY) {
        uCutLine.value4f(startX, startY, dirX, dirY);
    }

    public void setCutProgress(float progress) {
        uCutAlpha.value1f(progress);
    }

    public void setCutSide(int side) {
        uCutSide.value1f((float) side);
    }
}
```

### 3.4 BurnShader（燃烧着色器）

**位置：** `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/shaders/BurnShader.java`

```java
package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * Slice&Dice 风格燃烧着色器。
 */
public class BurnShader extends GlslShaderScript {

    private Uniform uBounds;
    private Uniform uBurnProgress;
    private Uniform uRandom;

    public BurnShader() {
        super("burn");  // 对应 shaders/burn.vert 和 shaders/burn.frag
        uBounds = uniform("uBounds");
        uBurnProgress = uniform("uBurnProgress");
        uRandom = uniform("uRandom");
    }

    public void setBounds(float x, float y, float width, float height) {
        uBounds.value4f(x, y, width, height);
    }

    public void setBurnProgress(float progress) {
        uBurnProgress.value1f(progress);
    }

    public void setRandom(float random) {
        uRandom.value1f(random);
    }
}
```

## 四、着色器文件

### 4.1 cut.vert（顶点着色器）

**位置：** `core/src/main/assets/shaders/cut.vert`

```glsl
#ifdef GL_ES
  precision highp float;
#endif

uniform mat4 uCamera;
attribute vec4 aXYZW;
attribute vec2 aUV;
varying vec2 vUV;

void main() {
  gl_Position = uCamera * aXYZW;
  vUV = aUV;
}
```

### 4.2 cut.frag（片段着色器）

**位置：** `core/src/main/assets/shaders/cut.frag`

```glsl
#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uCutLine;      // 切割线参数
uniform float uCutAlpha;    // 切割进度
uniform int uCutSide;       // 切割方向
uniform int uDisappear;     // 是否完全消失

// 计算点到线段的距离
float DistToLine(vec2 a, vec2 b, vec2 p) {
  vec2 v = a, w = b;
  float l2 = pow(distance(w, v), 2.0);
  if (l2 == 0.0) return distance(p, v);
  float t = clamp(dot(p - v, w - v) / l2, 0.0, 1.0);
  vec2 j = v + t * (w - v);
  return distance(p, j);
}

void main() {
  vec4 col = texture2D(uTex, vUV);
  
  // 计算切割效果
  vec2 lineStart = uCutLine.xy;
  vec2 lineEnd = uCutLine.xy + (uCutLine.zw * uCutAlpha);
  vec2 point = gl_FragCoord.xy;
  float dst = DistToLine(lineStart, lineEnd, point);
  
  // 边缘效果
  float tmp = dst - (1.0 + float(uDisappear));
  if (tmp < 0.0) {
    col.a = 0.0;
  } else if (tmp < 1.0) {
    col.rgb = vec3(0.7);  // 白色边缘
  } else if (tmp < 5.0) {
    col.r += (5.0 - tmp) * 0.1;
    col.g += (5.0 - tmp) * 0.01;
  }
  
  // 根据切割方向决定哪边消失
  vec2 v0 = gl_FragCoord.xy - uCutLine.xy;
  vec2 v1 = uCutLine.zw;
  float dot = v0.x * v1.y - v0.y * v1.x;
  col.a = (dot * float(uCutSide)) <= 0.0 ? col.a : 0.0;
  
  // 应用整体透明度
  col.a *= uAlpha;
  
  gl_FragColor = col;
}
```

### 4.3 burn.vert（顶点着色器）

**位置：** `core/src/main/assets/shaders/burn.vert`

```glsl
#ifdef GL_ES
  precision highp float;
#endif

uniform mat4 uCamera;
attribute vec4 aXYZW;
attribute vec2 aUV;
varying vec2 vUV;

void main() {
  gl_Position = uCamera * aXYZW;
  vUV = aUV;
}
```

### 4.4 burn.frag（片段着色器）

**位置：** `core/src/main/assets/shaders/burn.frag`

```glsl
#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uBounds;
uniform float uBurnProgress;
uniform float uRandom;

// 简化噪声函数（不需要噪声纹理）
float noise(vec2 p) {
  return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float fbm(vec2 p) {
  float val = 0.5;
  float amp = 0.5;
  for (int i = 0; i < 5; i++) {
    val += amp * noise(p);
    p *= 2.0;
    amp *= 0.5;
  }
  return val;
}

void main() {
  vec4 col = texture2D(uTex, vUV);
  
  // 计算相对位置
  vec2 ratioPos = (gl_FragCoord.xy - uBounds.xy) / uBounds.zw;
  
  // 噪声采样
  float noiseVal = fbm(gl_FragCoord.xy * 0.003 + uRandom * 10.0);
  float noiseStrength = 3.8 / pow(uBounds.w, 0.5);
  
  // 燃烧计算
  float burnAmt = (1.0 - ratioPos.y) - uBurnProgress * (1.0 + noiseStrength) + noiseVal * noiseStrength;
  
  // 已燃烧部分透明
  if (burnAmt < 0.0) {
    col.a = 0.0;
  }
  
  // 边缘火焰渐变
  float yellowStep = 0.9, orangeStep = 0.8, redStep = 0.7, sootStep = 0.6;
  float stepAlpha = 1.0 - burnAmt;
  
  float yellowAmt = smoothstep(yellowStep, 1.0, stepAlpha);
  float orangeAmt = smoothstep(orangeStep, yellowStep, stepAlpha);
  float redAmt = smoothstep(redStep, orangeStep, stepAlpha);
  float sootAmt = smoothstep(sootStep, redStep, stepAlpha);
  
  col.rgb = mix(col.rgb, vec3(0.1, 0.1, 0.1), sootAmt);
  col.rgb = mix(col.rgb, vec3(0.8, 0.2, 0.2), redAmt);
  col.rgb = mix(col.rgb, vec3(1.0, 0.5, 0.1), orangeAmt);
  col.rgb = mix(col.rgb, vec3(0.8, 0.8, 0.1), yellowAmt);
  
  // 应用整体透明度
  col.a *= uAlpha;
  
  gl_FragColor = col;
}
```

## 五、使用方式

### 5.1 初始化

在游戏启动时（如 `PixelScene` 或 `GameScene` 中）预编译着色器：

```java
// 预编译所有着色器，避免首次使用时的延迟
Shaders.init();
```

### 5.2 设置着色器参数

直接调用静态字段上的 setter 方法：

```java
// CutShader 参数
Shaders.cut.setCutLine(startX, startY, dirX, dirY);  // 切割线起点和方向
Shaders.cut.setCutProgress(0.5f);                     // 切割进度 0.0-1.0
Shaders.cut.setCutSide(1);                            // 切割方向 1 或 -1

// BurnShader 参数
Shaders.burn.setBounds(x, y, width, height);  // 目标边界
Shaders.burn.setBurnProgress(0.5f);           // 燃烧进度 0.0-1.0
Shaders.burn.setRandom(0.3f);                  // 随机种子（噪声变化）

// 通用参数（基类提供）
Shaders.cut.setTime(1.5f);    // 动画时间
Shaders.cut.setAlpha(0.8f);   // 整体透明度
Shaders.cut.setCamera(matrix); // 摄像机矩阵
```

#### CutShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setCutLine(startX, startY, dirX, dirY)` | 屏幕坐标 | 切割线起点和方向向量 |
| `setCutProgress(progress)` | 0.0 - 1.0 | 切割进度，0=未开始，1=完成 |
| `setCutSide(side)` | 1 或 -1 | 切割方向，决定哪边消失 |

#### BurnShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setBounds(x, y, width, height)` | 屏幕坐标 | 目标边界矩形 |
| `setBurnProgress(progress)` | 0.0 - 1.0 | 燃烧进度，从下往上燃烧 |
| `setRandom(seed)` | 任意浮点数 | 随机种子，控制火焰噪声变化 |

#### EllipseShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setBounds(x, y, width, height)` | 屏幕坐标 | 目标边界矩形 |
| `setProgress(progress)` | 0.0 - 1.0 | 消失进度 |
| `setRandom(seed)` | 任意浮点数 | 随机种子，控制噪声变化 |

**效果**：椭圆形区域向中心收缩消失，边缘带有蓝紫色调。

#### HslShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setHsl(h, s, l)` | -100 到 100 | HSL 颜色调整 |

**效果**：实时调整目标的色相、饱和度、亮度。

#### SingularityShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setBounds(x, y, width, height)` | 屏幕坐标 | 目标边界矩形 |
| `setProgress(progress)` | 0.0 - 1.0 | 收缩进度 |
| `setScale(scaleX, scaleY)` | 浮点数 | 收缩比例 |

**效果**：向中心点收缩消失，边缘带有粉紫色调。

#### AcidShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setProgress(progress)` | 0.0 - 1.0 | 溶解进度 |
| `setRandom(seed)` | 任意浮点数 | 随机种子，控制噪声变化 |

**效果**：酸蚀溶解效果，带有绿色调。

#### WipeShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setBounds(x, y, width, height)` | 屏幕坐标 | 目标边界矩形 |
| `setProgress(progress)` | 0.0 - 1.0 | 擦除进度 |
| `setDirection(dirX, dirY)` | 1.0 或 0.0 | 擦除方向 |

**效果**：从指定方向擦除目标。

#### AlphaShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setBounds(x, y, width, height)` | 屏幕坐标 | 目标边界矩形 |
| `setProgress(progress)` | 0.0 - 1.0 | 淡出进度 |

**效果**：简单的透明淡出，边缘带有微红色调。

#### NoiseShader 参数详解

| 方法 | 参数 | 说明 |
|------|------|------|
| `setupNoise()` | 无 | 初始化噪声纹理（首次使用时调用）|
| `setTime(time)` | 浮点数 | 动画时间 |
| `setProgress(progress)` | 0.0 - 1.0 | 溶解进度 |

**效果**：多 octave 噪声溶解，使用 Slice&Dice 噪声纹理。

**使用示例**：
```java
Shaders.noise.setupNoise();  // 初始化噪声纹理
Shaders.noise.setTime(0.5f);
Shaders.noise.setProgress(0.5f);
```

### 5.3 应用于特效

```java
// 设置参数后应用效果
Shaders.cut.setCutLine(sprite.x, sprite.y, 1, 1);
ShaderEffect.apply(sprite, Shaders.cut, 0.5f);  // 0.5秒动画
```

> **注意**：`ShaderEffect` 效果控制器尚未实现，后续需要创建它来管理动画时间线。

### 5.4 集成到 DiceMageSpellFX

```java
public static void kill(Char target, Type type) {
    if (target == null || target.sprite == null) return;

    DiceMageAudio.kill(type);
    
    switch (type) {
        case CUT:
        case CRUSH:
            ShaderEffect.apply(target.sprite, Shaders.cut, 0.5f);
            break;
        case BLAZE:
            ShaderEffect.apply(target.sprite, Shaders.burn, 0.5f);
            break;
        default:
            impact(target, type);
            target.sprite.killAndErase();
    }
}
```

## 六、扩展指南

### 6.1 添加新着色器

1. **创建着色器文件**
    - 在 `assets/shaders/` 创建 `{name}.vert` 和 `{name}.frag`
    - 使用 Noosa 命名约定（`aXYZW`, `aUV`, `vUV`, `uTex`）

2. **创建着色器类**
   ```java
   public class NewEffectShader extends GlslShaderScript {
       public NewEffectShader() {
           super("new_effect");  // 对应 shaders/new_effect.vert/.frag
           // 获取专用 Uniform
       }
   }
   ```

3. **在 Shaders 注册表添加一行**
   ```java
   public static final NewEffectShader newEffect = load(NewEffectShader.class);
   ```

### 6.2 从 Slice&Dice 移植着色器

1. 复制 GLSL 源码到 `.vert` / `.frag` 文件
2. 无需手动替换命名，基类会自动适配：
    - `a_position` → `aXYZW`
    - `a_texCoord0` → `aUV`
    - `v_texCoords` → `vUV`
    - `u_texture` → `uTex`
3. 处理噪声纹理（如需要）
4. 测试效果

## 七、注意事项

### 7.1 性能考虑

- 着色器在首次使用时编译，有延迟，建议启动时调用 `Shaders.init()` 预编译
- 避免在每帧创建新着色器实例
- 效果结束后及时清理

### 7.2 兼容性

- OpenGL ES 2.0 限制：不支持 `discard` 后的纹理采样
- 部分设备可能不支持高精度 float
- 需要在 GLSL 中使用 `#ifdef GL_ES` 处理精度

### 7.3 调试

- 使用 `Gdx.gl.glGetShaderInfoLog()` 查看编译错误
- 统一使用 `gl_FragCoord` 计算屏幕坐标
- 确保所有 Uniform 在使用前已设置

## 八、版本记录

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-01-21 | 初始设计文档 |
| v2.0 | 2026-07-21 | 重构为外部文件加载 + Shaders 注册表 |

---

*文档状态：已实现