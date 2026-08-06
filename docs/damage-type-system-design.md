# 伤害类型系统设计文档（v2.0）

## 一、概述

本文档描述伤害类型系统的完整设计，包括核心类、伤害计算流程以及使用指南。

### 设计目标

1. **伤害信息完整封装** - DamageInfo包含伤害值、类型、来源、modifier等所有信息
2. **暴击是属性而非类型** - 保留原始伤害类型，暴击作为独立属性
3. **Modifier系统** - 支持多层伤害修正（加算、乘算），便于天赋/Buff系统扩展
4. **调试友好** - 可追踪从基础伤害到最终伤害的完整计算过程

---

## 二、核心架构

### 2.1 DamageInfo = 伤害计算单元

DamageInfo不再只是数据容器，而是完整的伤害计算单元：

```
基础伤害 → 应用Modifiers → 最终伤害 → 传递给目标
```

**核心职责**：
- 存储**基础伤害值**（baseDamage）
- 管理**伤害Modifier列表**
- 提供`getDamage()`计算并返回最终伤害
- 包装伤害类型、来源、暴击等元信息

### 2.2 Modifier计算顺序

采用RPG标准的四阶段计算：

```
最终伤害 = floor(((基础伤害 + Σ直接加算) × Σ直接乘算) × Σ最终乘算 + Σ最终加算)
```

**计算阶段说明**：

| 阶段 | Modifier类型 | 示例 | 叠加方式 |
|------|-------------|------|----------|
| 1 | 直接加算 (FlatAdditive) | 天赋+10伤害、武器附魔+5 | 累加 |
| 2 | 直接乘算 (DirectMultiplicative) | 暴击×1.5、弱点攻击×2 | **累乘** |
| 3 | 最终乘算 (FinalMultiplicative) | 最终伤害×1.2 | 累乘 |
| 4 | 最终加算 (FinalAdditive) | 固定追加+50 | 累加 |

**注意**：乘算采用**累乘**而非累加
- 累乘：`×1.5 × ×1.2 × ×1.1 = ×1.98`
- 优点：多个乘算效果叠加更合理，避免无限膨胀

---

## 三、核心类设计

### 3.1 DamageType 枚举

**文件路径**: `damage/DamageType.java`

```java
public enum DamageType {
    // 物理伤害
    PHYSICAL("physical", FloatingText.PHYS_DMG, false, false),
    PHYSICAL_NO_ARMOR("physical_no_armor", FloatingText.PHYS_DMG_NO_BLOCK, false, true),

    // 魔法伤害
    MAGICAL("magical", FloatingText.MAGIC_DMG, true, false),

    // 元素伤害
    FIRE("fire", FloatingText.BURNING, true, false),
    FROST("frost", FloatingText.FROST, true, false),
    LIGHTNING("lightning", FloatingText.SHOCKING, true, false),
    TOXIC("toxic", FloatingText.TOXIC, true, false),
    CORROSIVE("corrosive", FloatingText.CORROSION, true, false),

    // 状态伤害（DoT）
    BLEEDING("bleeding", FloatingText.BLEEDING, false, false),
    POISON("poison", FloatingText.POISON, true, false),
    OOZE("ooze", FloatingText.OOZE, true, false),
    BURNING_STATUS("burning_status", FloatingText.BURNING, true, false),
    CHILL("chill", FloatingText.FROST, true, false),

    // 特殊伤害
    HUNGER("hunger", FloatingText.HUNGER, true, true),
    FALL("fall", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
    CHASM("chasm", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
    DEFERRED("deferred", FloatingText.DEFERRED, false, false),
    CORRUPTION("corruption", FloatingText.CORRUPTION, true, true),
    PICK("pick", FloatingText.PICK_DMG, false, false),
    WATER("water", FloatingText.WATER, true, false),
    AMULET("amulet", FloatingText.AMULET, true, true),

    // 真实伤害（无视一切减免）
    TRUE("true", FloatingText.PHYS_DMG, true, true),

    UNKNOWN("unknown", FloatingText.PHYS_DMG, false, false);

    // 属性方法
    public boolean isMagical();
    public boolean ignoresArmor();
    public boolean isTrueDamage();
    public boolean isPhysical();
    public boolean isElemental();
    public boolean isDoT();
}
```

### 3.2 DamageModifier 类

**文件路径**: `damage/DamageModifier.java`

用于表示单个伤害修正项：

```java
public class DamageModifier {
    public enum Type {
        FLAT_ADDITIVE,           // 直接加算
        DIRECT_MULTIPLICATIVE,   // 直接乘算
        FINAL_MULTIPLICATIVE,    // 最终乘算
        FINAL_ADDITIVE           // 最终加算
    }

    private Type type;
    private float value;
    private String source;       // 来源描述（用于调试/日志）
    private Object sourceObject; // 来源对象（可选）

    // 构造
    public DamageModifier(Type type, float value, String source);
    
    // 静态工厂
    public static DamageModifier flatAdd(float value, String source);
    public static DamageModifier directMult(float value, String source);
    public static DamageModifier finalMult(float value, String source);
    public static DamageModifier finalAdd(float value, String source);
}
```

### 3.3 DamageInfo 类（核心）

**文件路径**: `damage/DamageInfo.java`

```java
public class DamageInfo {
    
    // ========== 伤害值相关 ==========
    private int baseDamage;              // 基础伤害值
    private int cachedFinalDamage;       // 计算后的最终值（缓存）
    private boolean calculated = false;  // 是否已计算

    // ========== Modifier列表 ==========
    private List<DamageModifier> flatAdditives;           // 直接加算
    private List<DamageModifier> directMultiplicatives;   // 直接乘算
    private List<DamageModifier> finalMultiplicatives;    // 最终乘算
    private List<DamageModifier> finalAdditives;          // 最终加算

    // ========== 元信息 ==========
    private DamageType type;
    private boolean critical;
    private float criticalMultiplier = 1.5f;  // 默认暴击倍率
    private Char attacker;
    private Item sourceItem;
    private Object source;

    // ========== 伤害计算 ==========
    
    /**
     * 获取最终伤害值（应用所有modifier）
     */
    public int getDamage() {
        if (!calculated) {
            cachedFinalDamage = calculateFinalDamage();
            calculated = true;
        }
        return cachedFinalDamage;
    }

    /**
     * 获取基础伤害值（不含modifier）
     */
    public int getBaseDamage() {
        return baseDamage;
    }

    /**
     * 核心计算方法
     */
    private int calculateFinalDamage() {
        float result = baseDamage;

        // 阶段1：直接加算
        for (DamageModifier m : flatAdditives) {
            result += m.getValue();
        }

        // 阶段2：直接乘算（累乘）
        for (DamageModifier m : directMultiplicatives) {
            result *= m.getValue();
        }

        // 阶段3：最终乘算（累乘）
        for (DamageModifier m : finalMultiplicatives) {
            result *= m.getValue();
        }

        // 阶段4：最终加算
        for (DamageModifier m : finalAdditives) {
            result += m.getValue();
        }

        return Math.round(result);
    }

    // ========== Modifier管理 ==========

    /**
     * 添加直接加算modifier
     */
    public DamageInfo addFlatModifier(float value, String source) {
        flatAdditives.add(DamageModifier.flatAdd(value, source));
        invalidateCache();
        return this;  // 支持链式调用
    }

    /**
     * 添加直接乘算modifier
     */
    public DamageInfo addDirectMultModifier(float value, String source) {
        directMultiplicatives.add(DamageModifier.directMult(value, source));
        invalidateCache();
        return this;
    }

    /**
     * 添加最终乘算modifier
     */
    public DamageInfo addFinalMultModifier(float value, String source) {
        finalMultiplicatives.add(DamageModifier.finalMult(value, source));
        invalidateCache();
        return this;
    }

    /**
     * 添加最终加算modifier
     */
    public DamageInfo addFinalAddModifier(float value, String source) {
        finalAdditives.add(DamageModifier.finalAdd(value, source));
        invalidateCache();
        return this;
    }

    /**
     * 设置暴击（自动添加暴击乘算modifier）
     */
    public DamageInfo setCritical(boolean critical) {
        this.critical = critical;
        if (critical) {
            // 暴击作为直接乘算modifier
            addDirectMultModifier(criticalMultiplier, "暴击");
        }
        return this;
    }

    /**
     * 设置暴击并指定倍率
     */
    public DamageInfo setCritical(boolean critical, float multiplier) {
        this.criticalMultiplier = multiplier;
        return setCritical(critical);
    }

    /**
     * 清除缓存（modifier变化时调用）
     */
    private void invalidateCache() {
        calculated = false;
    }

    // ========== 工厂方法 ==========

    public static DamageInfo physical(int baseDamage, Char attacker) {
        return new DamageInfo(baseDamage, DamageType.PHYSICAL, attacker);
    }

    public static DamageInfo physical(int baseDamage, Char attacker, Item weapon) {
        DamageInfo info = new DamageInfo(baseDamage, DamageType.PHYSICAL, attacker);
        info.setSourceItem(weapon);
        return info;
    }

    public static DamageInfo magical(int baseDamage, Object source) {
        return new DamageInfo(baseDamage, DamageType.MAGICAL, null, null, source);
    }

    public static DamageInfo lightning(int baseDamage, Object source) {
        return new DamageInfo(baseDamage, DamageType.LIGHTNING, null, null, source);
    }

    public static DamageInfo fire(int baseDamage, Object source) {
        return new DamageInfo(baseDamage, DamageType.FIRE, null, null, source);
    }

    public static DamageInfo trueDamage(int baseDamage) {
        return new DamageInfo(baseDamage, DamageType.TRUE);
    }

    // ========== 调试工具 ==========

    /**
     * 获取伤害计算过程描述
     */
    public String getCalculationTrace() {
        StringBuilder sb = new StringBuilder();
        sb.append("伤害计算过程:\n");
        sb.append("  基础伤害: ").append(baseDamage).append("\n");
        
        if (!flatAdditives.isEmpty()) {
            sb.append("  + 直接加算:\n");
            for (DamageModifier m : flatAdditives) {
                sb.append("    +").append(m.getValue()).append(" (").append(m.getSource()).append(")\n");
            }
        }
        
        if (!directMultiplicatives.isEmpty()) {
            sb.append("  × 直接乘算:\n");
            for (DamageModifier m : directMultiplicatives) {
                sb.append("    ×").append(m.getValue()).append(" (").append(m.getSource()).append(")\n");
            }
        }
        
        sb.append("  = 最终伤害: ").append(getDamage());
        return sb.toString();
    }
}
```

### 3.4 DamageResistance 类

**文件路径**: `damage/DamageResistance.java`

```java
public class DamageResistance {
    private Map<DamageType, Float> resistances;
    private Set<DamageType> immunities;

    public void setResistance(DamageType type, float value);
    public void setImmunity(DamageType type, boolean immune);
    public float getResistance(DamageType type);
    public boolean isImmune(DamageType type);

    /**
     * 应用抗性减免伤害
     */
    public int applyResistance(int damage, DamageInfo info) {
        if (info.isTrueDamage() || isImmune(info.getType())) {
            return damage;
        }
        float resist = getResistance(info.getType());
        return Math.round(damage * (1f - resist));
    }
}
```

---

## 四、使用示例

### 4.1 基本物理伤害

```java
// 创建基础伤害
DamageInfo info = DamageInfo.physical(10, hero, sword);

// 添加modifier（链式调用）
info.addFlatModifier(5, "天赋：猎杀直觉")
    .addDirectMultModifier(1.2f, "弱点攻击");

// 获取最终值
int finalDamage = info.getDamage();  // (10+5) × 1.2 = 18

// 应用伤害
enemy.damage(info);
```

### 4.2 暴击伤害

```java
DamageInfo info = DamageInfo.physical(15, hero, sword);

// 设置暴击（自动添加×1.5乘算）
info.setCritical(true);

// 查看计算过程
System.out.println(info.getCalculationTrace());
// 输出:
//   基础伤害: 15
//   × 直接乘算:
//     ×1.5 (暴击)
//   = 最终伤害: 22

enemy.damage(info);
```

### 4.3 复杂modifier组合

```java
DamageInfo info = DamageInfo.physical(20, hero, legendarySword);

// 多层modifier
info.addFlatModifier(10, "天赋：砥砺锋芒")      // +10
    .addFlatModifier(5, "武器附魔")              // +5
    .setCritical(true, 2.0f)                    // ×2.0 暴击
    .addFinalMultModifier(1.25f, "最终加成")     // ×1.25
    .addFinalAddModifier(50, "固定追加");        // +50

// 计算: ((20+10+5) × 2.0) × 1.25 + 50 = 137.5 → 137
int damage = info.getDamage();  // 137
```

### 4.4 魔法伤害

```java
// 法杖伤害
DamageInfo info = DamageInfo.lightning(25, wandOfLightning);
info.addDirectMultModifier(1.5f, "法杖等级加成");

enemy.damage(info);
```

### 4.5 持续伤害（DoT）

```java
// Burning.java
public void act() {
    int baseDamage = damageRoll();
    DamageInfo info = DamageInfo.burningStatus(baseDamage, this);
    
    // 火焰伤害可能有额外加成
    if (target.buff(FireImbue.class) != null) {
        info.addDirectMultModifier(0.5f, "火焰易伤");
    }
    
    target.damage(info);
}
```

---

## 五、Char.java 修改

```java
/**
 * 新的伤害方法：使用DamageInfo
 */
public void damage( DamageInfo info ) {
    if (info == null) return;

    // 获取计算后的最终伤害
    int dmg = info.getDamage();
    Object src = info.getSource();

    // 处理暴击显示
    if (info.isCritical()) {
        src = info.ignoresArmor() ? new NoArmorCritClass() : new CritClass();
    }

    // 调用现有方法（保持向后兼容）
    damage(dmg, src);
}
```

---

## 六、调试与日志

### 6.1 伤害计算追踪

```java
DamageInfo info = DamageInfo.physical(50, hero, sword);
info.addFlatModifier(10, "天赋A")
    .setCritical(true)
    .addFinalAddModifier(20, "最终加成");

// 打印计算过程
GLog.i(info.getCalculationTrace());
/*
伤害计算过程:
  基础伤害: 50
  + 直接加算:
    +10.0 (天赋A)
  × 直接乘算:
    ×1.5 (暴击)
  + 最终加算:
    +20.0 (最终加成)
  = 最终伤害: 95
*/
```

### 6.2 Modifier来源追踪

每个modifier携带来源描述，便于：
- 调试时查看伤害为何变化
- UI显示伤害加成来源
- 日志记录战斗过程

---

## 七、文件结构

```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/
├── damage/
│   ├── DamageType.java              # 伤害类型枚举
│   ├── DamageInfo.java              # 伤害计算单元（核心）
│   ├── DamageModifier.java          # Modifier表示类
│   ├── DamageResistance.java        # 抗性管理
│   └── DamageSource.java            # 兼容工具类
└── actors/
    └── Char.java                    # damage(DamageInfo) 方法
```

---

## 八、实现状态

| 组件 | 状态 | 备注 |
|------|------|------|
| DamageType | ✅ 已实现 | 需保持现状 |
| DamageInfo | 🔄 需重构 | 添加modifier系统 |
| DamageModifier | ❌ 待创建 | 新增类 |
| DamageResistance | ✅ 已实现 | 需保持现状 |
| DamageSource | ✅ 已实现 | 需保持现状 |
| Char.damage(DamageInfo) | ✅ 已实现 | 需适配新getDamage() |

---

## 九、迁移计划

### 阶段1：重构DamageInfo
- 添加baseDamage/finalDamage
- 实现modifier四列表
- 实现计算方法

### 阶段2：创建DamageModifier
- 定义Type枚举
- 实现工厂方法

### 阶段3：更新Char.damage()
- 使用info.getDamage()获取最终值

### 阶段4：迁移调用点
- Buff类 → 使用新API
- Item类 → 使用新API
- 核心战斗逻辑

---

## 十、设计优势

| 优势 | 说明 |
|------|------|
| **计算集中** | 所有伤害计算在DamageInfo内完成，不散落各处 |
| **调试友好** | 可追踪每一步计算过程 |
| **扩展性强** | 新增天赋/Buff只需添加modifier |
| **解耦** | Char只负责"应用伤害"，不负责"计算伤害" |
| **向后兼容** | 新方法内部调用旧方法，渐进迁移 |