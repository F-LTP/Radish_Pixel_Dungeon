# 刺客斜向刀光斩杀特效设计文档

## 1. 现有刺客斩杀机制分析

### 1.1 当前实现
- **位置**: `actors/buffs/Preparation.java` 和 `actors/Char.java`
- **触发条件**: 刺客处于 `Preparation`（潜伏）状态下攻击
- **斩杀阈值**: 根据潜伏回合数和天赋等级决定（3%-100% 生命值）
- **当前特效**: 仅显示文字状态 `"assassinated"`（紫色 NEGATIVE 状态）
- **伤害计算**: 多次伤害掷骰取最大值，并有额外伤害加成

### 1.2 现有相关特效参考
- **血液飞溅**: `enemy.sprite.bloodBurstA(sprite.center(), effectiveDamage)`
- **闪光效果**: `enemy.sprite.flash()`
- **烟雾粒子**: `CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6)`

---

## 2. 新特效设计概述

### 2.1 特效目标
创建一个视觉冲击力强的斜向刀光斩杀特效，包含：
1. **斜向刀光动画** - 从刺客位置向敌人方向挥砍
2. **敌人贴图分割** - 沿刀光方向将敌人分为两半
3. **残骸抛物线散落** - 尸体碎片以随机抛物线轨迹落到周围地面

### 2.2 触发时机
- 当刺客使用 `Preparation` 状态成功斩杀敌人时
- 替换现有的单纯文字提示

---

## 3. 技术实现方案

### 3.1 核心类设计

#### 3.1.1 `AssassinateSlash.java` - 刀光特效主类
```
位置：core/src/main/java/.../effects/AssassinateSlash.java
继承：com.watabou.noosa.Image
```

**职责**:
- 创建斜向刀光贴图
- 控制刀光的挥砍动画（出现→挥砍→消失）
- 计算刀光角度（从刺客指向敌人）

**关键属性**:
- `PointF origin` - 刀光起点（刺客位置）
- `PointF target` - 刀光终点（敌人位置）
- `float angle` - 刀光旋转角度
- `float duration` - 特效持续时间（建议 0.3-0.5 秒）
- `Phase` - 动画阶段（IN → SLASH → OUT）

**方法**:
- `static void show(Char attacker, Char victim)` - 静态展示方法
- `update()` - 每帧更新动画状态
- `draw()` - 绘制刀光（使用发光混合模式）

---

#### 3.1.2 `SplitSprite.java` - 分割贴图类
```
位置：core/src/main/java/.../effects/SplitSprite.java
继承：com.watabou.noosa.Image
```

**职责**:
- 复制敌人贴图并沿刀光方向分割为两半
- 控制两半贴图的分离动画

**关键属性**:
- `Image topHalf` - 上半身贴图
- `Image bottomHalf` - 下半身贴图
- `float splitAngle` - 分割线角度（垂直于刀光方向）
- `Vector2 separationVelocity` - 分离速度向量

**方法**:
- `static SplitSprite createFrom(Char victim, float slashAngle)` - 从敌人创建分割贴图
- `update()` - 更新分离动画
- `mask()` - 使用遮罩裁剪贴图（只保留一半）

---

#### 3.1.3 `DebrisParticle.java` - 残骸粒子类
```
位置：core/src/main/java/.../effects/particles/DebrisParticle.java
继承：com.watabou.noosa.particles.PixelParticle
```

**职责**:
- 模拟尸体碎片的抛物线运动
- 控制碎片的大小、颜色、透明度渐变

**关键属性**:
- `Vector2 velocity` - 初始速度（随机方向）
- `float gravity` - 重力加速度
- `float bounceFactor` - 弹跳系数
- `Color color` - 碎片颜色（基于敌人贴图颜色）

**工厂方法**:
- `FACTORY` - 标准发射器工厂
- `DEBRIS_BURST` - 爆发式发射器（用于斩杀瞬间）

**运动方程**:
```
x = x0 + vx * t
y = y0 + vy * t + 0.5 * gravity * t²
rotation = initialRotation + angularVelocity * t
```

---

### 3.2 资源需求

#### 3.2.1 贴图资源
| 资源名称 | 文件路径 | 说明 |
|---------|---------|------|
| 刀光贴图 | `assets/effects/slash.png` | 新月形/弧形刀光，白色半透明 |
| 粒子贴图 | 使用现有 `assets/effects/effects.png` | 可复用现有粒子 |

#### 3.2.2 刀光贴图规格
- **尺寸**: 32x32 或 48x48 像素
- **格式**: PNG（带 Alpha 通道）
- **内容**: 弧形/新月形白色刀光轨迹
- **方向**: 默认水平向右，通过旋转适配角度

---

### 3.3 特效序列流程

```
时间线：
├─ T+0.00s: 刀光出现，敌人贴图替换为 SplitSprite
├─ T+0.10s: 刀光开始挥砍动画
├─ T+0.20s: 刀光命中，敌人完全分离
├─ T+0.25s: 触发残骸粒子爆发
├─ T+0.30s: 刀光开始淡出
├─ T+0.50s: 特效完全结束，清理对象
```

---

### 3.4 代码集成点

#### 3.4.1 修改 `Char.java` 的伤害处理逻辑
**位置**: 约 704 行附近
```java
// 当前代码
if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
    enemy.HP = 0;
    if (!enemy.isAlive()) {
        enemy.die(this);
    } else {
        enemy.damage(-1, this);
        DeathMark.processFearTheReaper(enemy);
    }
    enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
}
```

**修改为**:
```java
if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
    enemy.HP = 0;
    if (!enemy.isAlive()) {
        // 触发刺客斩杀特效
        AssassinateSlash.show(this, enemy);
        enemy.die(this);
    } else {
        enemy.damage(-1, this);
        DeathMark.processFearTheReaper(enemy);
    }
    enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
}
```

---

## 4. 详细实现步骤

### 4.1 第一阶段：基础刀光特效

#### 步骤 1.1: 创建刀光贴图资源
- 绘制 `assets/effects/slash.png`
- 包含 2-3 帧不同阶段的刀光（可选，用于帧动画）

#### 步骤 1.2: 实现 `AssassinateSlash` 类
- 基础显示功能
- 角度计算和旋转
- 淡入淡出动画

#### 步骤 1.3: 集成到 `Char.java`
- 添加调用代码
- 测试基础效果

---

### 4.2 第二阶段：敌人分割效果

#### 步骤 2.1: 实现 `SplitSprite` 类
- 贴图复制和裁剪
- 使用 `ScissorMask` 或自定义遮罩
- 分离动画

#### 步骤 2.2: 处理敌人死亡
- 隐藏原敌人贴图
- 显示分割贴图
- 同步位置

---

### 4.3 第三阶段：残骸粒子系统

#### 步骤 3.1: 实现 `DebrisParticle` 类
- 抛物线运动物理
- 随机方向和速度
- 颜色和大小变化

#### 步骤 3.2: 创建粒子爆发
- 在刀光命中时触发
- 发射 10-20 个粒子
- 粒子持续 0.5-1.0 秒

---

## 5. 性能优化考虑

### 5.1 对象池
- `AssassinateSlash` 使用对象池复用
- `DebrisParticle` 必须使用对象池（大量粒子）

### 5.2 渲染优化
- 刀光使用 `Blending.setLightMode()` 增强视觉
- 粒子使用批处理渲染

### 5.3 内存管理
- 特效结束后立即清理
- 分割贴图及时释放

---

## 6. 可配置参数

以下参数建议做成可配置的常量：

```java
public class AssassinateConfig {
    // 刀光特效
    public static final float SLASH_DURATION = 0.4f;      // 刀光持续时间
    public static final float SLASH_WIDTH = 8f;           // 刀光宽度
    public static final float SLASH_LENGTH = 24f;         // 刀光长度
    
    // 分割效果
    public static final float SEPARATION_SPEED = 10f;     // 分离速度
    public static final float SEPARATION_DELAY = 0.1f;    // 分离延迟
    
    // 残骸粒子
    public static final int DEBRIS_COUNT = 15;            // 粒子数量
    public static final float DEBRIS_GRAVITY = 30f;       // 重力
    public static final float DEBRIS_LIFESPAN = 0.8f;     // 粒子寿命
    public static final float DEBRIS_SPREAD = 360f;       // 扩散角度
}
```

---

## 7. 扩展性设计

### 7.1 未来可能的扩展
- 不同武器类型的刀光变体（匕首/剑/斧）
- 不同元素效果的斩杀（火焰/冰霜/暗影）
- 天赋升级增强特效（更大/更亮/更多粒子）

### 7.2 复用性
- `AssassinateSlash` 可用于其他技能的挥砍动画
- `SplitSprite` 可用于其他分割/破碎效果
- `DebrisParticle` 可用于岩石破碎、木箱破坏等

---

## 8. 测试计划

### 8.1 功能测试
- [ ] 刀光正确指向敌人方向
- [ ] 敌人贴图正确分割
- [ ] 残骸粒子抛物线运动正常
- [ ] 特效结束后无内存泄漏

### 8.2 视觉测试
- [ ] 不同体型的敌人都能正常显示
- [ ] 不同距离的斩杀都适用
- [ ] 多个敌人连续斩杀时不卡顿

### 8.3 兼容性测试
- [ ] 与现有刺客天赋兼容
- [ ] 与 `DeathMark` 效果兼容
- [ ] 与 `BraceYourself` 效果兼容

---

## 9. 风险评估

### 9.1 技术风险
| 风险 | 可能性 | 影响 | 缓解措施 |
|-----|--------|------|---------|
| 贴图遮罩实现复杂 | 中 | 中 | 使用简化方案，仅做视觉分离 |
| 粒子性能问题 | 低 | 中 | 限制粒子数量，使用对象池 |
| 与现有系统冲突 | 低 | 高 | 充分测试，保留回退方案 |

### 9.2 美术风险
- 刀光风格可能与游戏整体风格不符
- **缓解**: 参考现有特效风格，保持像素艺术一致性

---

## 10. 验收标准

### 10.1 必须满足
1. 刺客斩杀时显示斜向刀光
2. 敌人贴图沿刀光方向分为两半
3. 尸体碎片向周围散落
4. 不影响游戏性能和稳定性

### 10.2 期望满足
1. 刀光角度根据刺客 - 敌人相对位置动态计算
2. 粒子颜色和敌人类型相关
3. 特效流畅，帧率稳定在 60FPS

### 10.3 可选满足
1. 不同潜伏等级有不同刀光大小/颜色
2. 天赋升级增强特效视觉效果

---

## 11. 时间估算

| 阶段 | 工作内容 | 预计时间 |
|-----|---------|---------|
| 第一阶段 | 基础刀光特效 | 2-3 小时 |
| 第二阶段 | 敌人分割效果 | 3-4 小时 |
| 第三阶段 | 残骸粒子系统 | 2-3 小时 |
| 集成测试 | 代码集成和调试 | 2-3 小时 |
| **总计** | | **9-13 小时** |

---

## 12. 参考资源

### 12.1 游戏内参考
- `effects/Beam.java` - 光束特效
- `effects/SpellSprite.java` - 法术图标
- `effects/particles/EnergyParticle.java` - 能量粒子
- `actors/buffs/Preparation.java` - 刺客潜伏 buff

### 12.2 外部参考
- Watabou 引擎文档：https://github.com/watabou/
- Pixel Dungeon 源码：https://github.com/00-Evan/shattered-pixel-dungeon

---

**文档版本**: 1.0  
**创建日期**: 2026 年 3 月 22 日  
**待审核**: 是
