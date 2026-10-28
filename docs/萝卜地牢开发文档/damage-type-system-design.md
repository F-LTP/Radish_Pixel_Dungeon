# 伤害类型系统设计文档

> 状态：**已实现**（核心已落地，部分迁移进行中，见文末）。

## 一、概述

本文档描述伤害类型系统的设计与使用，包括核心类、伤害计算流程。

### 设计目标

1. **伤害信息完整封装** — `DamageInfo` 包含伤害值、类型、来源、modifier 等所有信息。
2. **暴击是属性而非类型** — 保留原始伤害类型，暴击作为独立属性。
3. **多阶段 Modifier 系统** — 支持多层伤害修正（加算、叠加乘区、乘算），便于天赋/Buff 扩展。
4. **调试友好** — 可追踪从基础伤害到最终伤害的完整计算过程。

## 二、核心架构

### 2.1 DamageInfo = 伤害计算单元

`DamageInfo` 不只是数据容器，而是完整的伤害计算单元：

```
基础伤害 → 应用 Modifiers → 最终伤害 → 传递给目标
```

**核心职责**：存储基础伤害、管理 modifier 列表、计算最终伤害、包装类型/来源/暴击等元信息。

### 2.2 Modifier 计算顺序

采用多阶段计算公式（与代码 `DamageInfo.calculateFinalDamage()` 一致）：

```
最终伤害 = max(0, round( (((((基础 + Σ直接加算) × 叠加乘区) × Σ直接乘算) × 暴击倍率 + Σ最终前加算) × Σ最终乘算 + Σ最终加算) ))
```

**计算阶段说明：**

| 阶段 | Modifier 类型 | 示例 | 叠加方式 |
|------|--------------|------|----------|
| 1 | `FLAT_ADDITIVE`（直接加算） | 天赋+10伤害 | 累加 |
| 2 | `STACK_MULTIPLICATIVE`（叠加乘区） | 两个+50%叠加 | 百分比累加后一次乘算 |
| 3 | `DIRECT_MULTIPLICATIVE`（直接乘算） | 弱点攻击×2 | **依次乘算** |
| 3 | 暴击倍率 | 独立于 modifier 列表 | 单独处理 |
| 4 | `PRE_FINAL_ADDITIVE`（最终前加算） | 处决固定追加 | 累加 |
| 5 | `FINAL_MULTIPLICATIVE`（最终乘算） | 最终伤害×1.2 | 累乘 |
| 6 | `FINAL_ADDITIVE`（最终加算） | 固定追加+50 | 累加 |

**叠加乘区 vs 直接乘算（重要区别）：**

| 场景 | 计算 | 结果 |
|------|------|------|
| 两个 `addStackMultModifier(1.5f, ...)` | `1 + 0.5 + 0.5` | `×2.0` |
| 两个 `addDirectMultModifier(1.5f, ...)` | `1.5 × 1.5` | `×2.25` |

- 叠加乘区适合「百分比加成」类效果（多个来源互相稀释），避免无限膨胀。
- 直接乘算适合「必须依次叠乘」的场景。

**注意**：`PRE_FINAL_ADDITIVE` 是正式阶段，不得删除或并入 `FINAL_ADDITIVE`。暴击独立于 modifier 列表，便于 UI 与旧 source 兼容。

## 三、核心类

### 3.1 DamageType 枚举

**路径**：`damage/DamageType.java`

```java
public enum DamageType {
    PHYSICAL, PHYSICAL_NO_ARMOR, MAGICAL,
    FIRE, FROST, LIGHTNING, TOXIC, CORROSIVE,     // 元素
    BLEEDING, POISON, OOZE, BURNING_STATUS, CHILL, // 状态伤害（DoT）
    HUNGER, FALL, CHASM, DEFERRED, CORRUPTION, PICK, WATER, AMULET,
    TRUE,     // 真实伤害，无视一切减免
    MIXED,    // 混合伤害（哨兵，由 MixedDamage 携带成分）
    UNKNOWN;
}
```

常用属性方法：`isMagical()`、`ignoresArmor()`、`ignoresShields()`（TRUE/HUNGER 无视护盾）、`isTrueDamage()`、`isPhysical()`、`isElemental()`、`isDoT()`。

> 兼容方法 `fromSource(Object)` 存在，用于旧调用迁移，待全部调用点显式化后删除。

### 3.2 DamageModifier

**路径**：`damage/DamageModifier.java`

表示单个伤害修正项。类型 `Type`：`FLAT_ADDITIVE`、`STACK_MULTIPLICATIVE`、`DIRECT_MULTIPLICATIVE`、`PRE_FINAL_ADDITIVE`、`FINAL_MULTIPLICATIVE`、`FINAL_ADDITIVE`。

携带 `source`（来源描述，用于调试/日志）与可选 `sourceObject`。

### 3.3 DamageInfo（核心）

**路径**：`damage/DamageInfo.java`

`DamageInfo` 是伤害计算单元。关键 modifier 管理（链式调用，均可选传 `Object sourceObject`）：

```java
addFlatModifier(value, source)         // 阶段1 直接加算
addStackMultModifier(value, source)    // 阶段2 叠加乘区
addDirectMultModifier(value, source)   // 阶段3 依次乘算
addPreFinalAddModifier(value, source)  // 阶段4 最终前加算
addFinalMultModifier(value, source)    // 阶段5 最终乘算
addFinalAddModifier(value, source)     // 阶段6 最终加算
setCritical(boolean) / setCritical(boolean, float mult)  // 暴击
```

**工厂方法**：`physical(...)`、`magical(...)`、`fire(...)`、`lightning(...)`、`frost(...)`、`poison(...)`、`bleeding(...)`、`ooze(...)`、`corrosive(...)`、`burningStatus(...)`、`trueDamage(...)`、`hunger(...)`、`fall(...)`、`chasm(...)` 等。

**调试工具**：`getCalculationTrace()`、`copy()`、`withBaseDamage(...)`、`withCritical(...)`。

### 3.4 DamagePipeline

**路径**：`damage/DamagePipeline.java`

伤害管线，`apply(Char target, DamageInfo info)` 调用 `Char.applyDamage(DamageInfo)` 作为唯一权威实现，返回 `DamageResult`。

### 3.5 DamageResult

**路径**：`damage/DamageResult.java`

```java
public final class DamageResult {
    public final int baseDamage;
    public final int modifiedDamage;
    public final int armorBlocked;
    public final int resistanceBlocked;
    public final int shieldBlocked;
    public final int hpDamage;
    public final boolean immune;
}
```

### 3.6 MixedDamage（混合伤害）

**路径**：`damage/MixedDamage.java`。两个平行列表 `types`（成分类型，禁含 MIXED/UNKNOWN/TRUE）+ `percentages`（占比）。`add(type, pct)`、`validate()`（**占比和必须为 1，否则抛异常**）。`DamageInfo.setMixedDamage(md)` 自动 validate 并置 `type = MIXED`。

混合伤害在 `Char.applyDamage` 中按各成分分别判定免疫/抗性后加权（`Σ pct × resistanceTo(comp) × resist(src)`），免疫成分被挡、全免则 `immuneHit`。

### 3.7 DamageResistance

**路径**：`damage/DamageResistance.java`。按 `DamageType` 管理抗性/免疫：`setResistance(type, value)`、`setImmunity(type, bool)`、`getResistance(type)`、`isImmune(type)`。真实伤害无视抗性。`merge(other)` 合并（取最大值）。

## 四、使用示例

### 4.1 基本物理伤害

```java
DamageInfo info = DamageInfo.physical(10, hero, sword);
info.addFlatModifier(5, "天赋：猎杀直觉")
    .addDirectMultModifier(1.2f, "弱点攻击");
int finalDamage = info.getDamage();  // (10+5) × 1.2 = 18
enemy.damage(info);
```

### 4.2 叠加乘区 vs 直接乘算

```java
// 叠加乘区：两个 +50% → ×2.0
info.addStackMultModifier(1.5f, "A").addStackMultModifier(1.5f, "B");

// 直接乘算：两个 ×1.5 → ×2.25
info.addDirectMultModifier(1.5f, "A").addDirectMultModifier(1.5f, "B");
```

### 4.3 完整六阶段

```java
DamageInfo info = DamageInfo.physical(20, hero, legendarySword);
info.addFlatModifier(10, "砥砺锋芒")           // 阶段1 +10
    .addStackMultModifier(1.3f, "增伤")        // 阶段2 ×(1+0.3)
    .addDirectMultModifier(1.3f, "弱点攻击")   // 阶段3 ×1.3
    .setCritical(true, 2.0f)                    // 阶段3 暴击 ×2.0
    .addPreFinalAddModifier(15, "处决加成")     // 阶段4 +15
    .addFinalMultModifier(1.25f, "最终加成")    // 阶段5 ×1.25
    .addFinalAddModifier(50, "固定追加");        // 阶段6 +50
int damage = info.getDamage();
```

### 4.4 混合伤害

```java
MixedDamage md = new MixedDamage();
md.add(DamageType.FIRE, 0.5f).add(DamageType.PHYSICAL, 0.5f).validate();
DamageInfo dmg = new DamageInfo(x, DamageType.MIXED, ...);
dmg.setMixedDamage(md);   // 自动 validate + 置 MIXED
target.damage(dmg);
```

### 4.5 持续伤害（DoT）

```java
public void act() {
    int base = damageRoll();
    DamageInfo info = DamageInfo.burningStatus(base, this);
    if (target.buff(Vulnerable.class) != null) {
        info.addDirectMultModifier(1.33f, "易伤");
    }
    target.damage(info);
}
```

## 五、Char.java 集成

```java
// 新入口：使用 DamageInfo
public void damage(DamageInfo info) {
    DamagePipeline.apply(this, info);
}

// 兼容入口：旧式调用，包装成 DamageInfo.fromSource 走管线
public void damage(int dmg, Object src) {
    DamageInfo active = DamagePipeline.activeInfo();
    if (active != null) {
        damage(dmg, src, active.getType());
    } else {
        DamageInfo info = new DamageInfo(dmg, DamageType.fromSource(src));
        DamagePipeline.apply(this, info);
    }
}
```

## 六、与事件系统的配合

伤害在 `Char.damage(int, Object, DamageType)` 收口处发布两套事件：
- **`CharUnprocedDamageEvent`**：减免前，携带原始伤害 `dmg`。
- **`CharFinalDamageEvent`**：减免后，携带实际 HP 扣减 `dealt`，`dealt>0` 才发。

订阅方据此在合适的阶段修改或响应伤害（详见 `event-system-design.md`）。

## 七、文件结构

```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/damage/
├── DamageType.java               # 伤害类型枚举
├── DamageInfo.java               # 伤害计算单元（核心）
├── DamageModifier.java           # Modifier 表示类
├── DamageResistance.java         # 抗性/免疫管理
├── DamagePipeline.java           # 伤害管线
├── DamageResult.java             # 伤害结果
├── DamageSource.java             # 兼容工具类
├── DamageCauseFormatter.java     # 伤害来源链格式化
├── MixedDamage.java              # 混合伤害
└── OrdinaryAttackDamage.java     # 普通攻击构建器
```

## 八、实现状态与迁移

| 组件 | 状态 |
|------|------|
| DamageType | ✅ 已实现 |
| DamageInfo 六阶段 modifier | ✅ 已实现（含 STACK_MULTIPLICATIVE） |
| DamageModifier | ✅ 已实现 |
| DamageResistance / 类型化抗性层 | ✅ 已实现 |
| DamagePipeline → Char.applyDamage | ✅ 已实现（权威路径） |
| DamageResult | ✅ 已实现 |
| MixedDamage / 来源链 | ✅ 已实现 |
| `fromSource` 全调用点显式化 | ⚠️ 进行中（大量调用点待迁移） |

**迁移建议顺序**：先骰子法术 → 怪物技能 → 武器物品；全部显式化后删除 `fromSource`、`isNoArmorPhysicalSource`、`NO_ARMOR_PHYSICAL_SOURCES`，管线切换纯类型判定。

> 详细重构过程与测试清单见 `damage-system-refactor-test-log.md`。
