# Char.java 事件系统迁移计划

## 一、概述

Char.java（1748行）是游戏中最复杂的单文件之一。大量物品、天赋、Buff 的特殊效果逻辑直接硬编码在 `attack()`、`hit()`、`damage()` 等核心方法中，导致：
- 每添加新物品/天赋都需要修改 Char.java
- 逻辑分散，难以追踪某个效果的完整流程
- 不同模块之间高度耦合

**目标**：将硬编码的物品/天赋/Buff 逻辑迁移到事件订阅者中，使 Char.java 只保留核心框架和事件发布点。

---

## 二、需要创建的新事件

| 事件名 | 发布位置 | 用途 |
|--------|---------|------|
| `CharAttackEvent` | `attack()` 方法开头 | 攻击判定前，可修改命中/伤害/暴击 |
| `CharDamageCalcEvent` | `attack()` 伤害计算阶段 | 伤害乘算/加算阶段，订阅者可添加 modifier |
| `CharDamageEvent` | `damage()` 方法开头 | 受到伤害时，可修改伤害值或取消 |
| `CharHitCalcEvent` | `hit()` 方法中 | 命中/闪避计算阶段，订阅者可修改命中结果 |
| `CharDeathEvent` | `die()` 方法 | 死亡时触发，可取消死亡（已有 HeroDeathEvent，需扩展为通用） |
| `CharSpeedCalcEvent` | `speed()` 方法 | 速度计算阶段，订阅者可修改速度 |
| `CharResistCalcEvent` | `resist()` 方法 | 抗性计算阶段，订阅者可修改抗性 |
| `CharBuffAddEvent` | `add(Buff)` 方法 | Buff 添加时，可阻止添加 |

---

## 三、迁移清单（按优先级排序）

### 优先级 1：attack() 方法（约370行可迁移）

#### 1.1 暴击计算逻辑（行514-577）
**当前问题**：武器特殊暴击逻辑硬编码在 Char.attack() 中

| 物品/效果 | 行号 | 迁移目标 |
|-----------|------|---------|
| LongStick（长棍）暴击加成 | 518-519 | `LongStick.java` 订阅 `CharAttackEvent` |
| Bloodblade（血刃）暴击加成 | 520-522 | `Bloodblade.java` 订阅 `CharAttackEvent` |
| GiantKiller（巨人杀手）必暴 | 523-525 | `GiantKiller.java` 订阅 `CharAttackEvent` |
| Seekingspear（寻踪矛）暴击伤害+突袭暴击 | 526-531 | `Seekingspear.java` 订阅 `CharAttackEvent` |
| MissileWeapon + HoldBreath 暴击 | 533-538 | `MissileWeapon.java` 或天赋处理器 |
| Radish 全局暴击率 | 541-548 | `Radish.java` 订阅 `CharAttackEvent` |
| DEATHBLOW 天赋暴击 | 543-545, 559-565 | `Talent.java` 中的订阅者 |
| Scythe 镰刀 buff 暴击 | 554-557 | `Sickle.java` 订阅 `CharAttackEvent` |
| RingOfTenacity 取消暴击 | 568-570 | `RingOfTenacity.java` 订阅 `CharAttackEvent` |

**迁移后 Char.java 变化**：
```java
// 旧代码（删除）：
if (hero.belongings.weapon() instanceof LongStick) { ... }
else if (hero.belongings.weapon() instanceof Bloodblade) { ... }

// 新代码（替换为事件发布）：
CharAttackEvent attackEvent = new CharAttackEvent(this, enemy, dmg, critSkill, critDamage);
EventManager.emit(attackEvent);
// 从事件中获取修改后的值
dmg = attackEvent.getDamage();
current_crit = attackEvent.getCritChance();
current_critdamage = attackEvent.getCritDamage();
```

#### 1.2 伤害乘算逻辑（行579-617）
**当前问题**：各种 Buff/天赋的伤害乘数硬编码

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| Berserk 伤害因子 | 581-582 | `Berserk.java` 订阅 `CharDamageCalcEvent` |
| Fury 1.5x | 584-586 | `Fury.java` 订阅 `CharDamageCalcEvent` |
| RingOfTenacity 攻击乘数 | 587-589 | `RingOfTenacity.java` 订阅 `CharDamageCalcEvent` |
| ChampionEnemy 伤害因子 | 590-592 | `ChampionEnemy.java` 订阅 `CharDamageCalcEvent` |
| ChampionHero 伤害因子 | 593-595 | `ChampionHero.java` 订阅 `CharDamageCalcEvent` |
| AscensionChallenge 修正 | 596 | `AscensionChallenge.java` 订阅 `CharDamageCalcEvent` |
| Endure 伤害因子（攻/防） | 602-609 | `Endure.java` 订阅 `CharDamageCalcEvent` |
| ScrollOfChallenge 减伤 | 611-613 | `ScrollOfChallenge.java` 订阅 `CharDamageCalcEvent` |
| Weakness 减伤 | 615-617 | `Weakness.java` 订阅 `CharDamageCalcEvent` |
| PlateArmor 减伤 | 620-622 | `PlateArmor.java` 订阅 `CharDamageCalcEvent` |

#### 1.3 攻击时的天赋/物品触发（行402-513, 682-700）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| AfterImage.Blur 闪避 | 403-405 | `AfterImage.java` 订阅 `CharAttackEvent` |
| FatedDraw 防御/攻击判定 | 406-478, 494-500 | `FatedDraw.java` 订阅多个事件 |
| Preparation 暗杀伤害 | 483-491 | `Preparation.java` 订阅 `CharAttackEvent` |
| BOUNTY_HUNTER 天赋 | 487-488 | `Talent.java` 订阅者 |
| POWER_RECYCLE 天赋 | 489-491, 507-512 | `Talent.java` 订阅者 |
| FireImbue / FrostImbue | 682-683 | 已有 proc()，保持不变或订阅事件 |
| ArrowBuff 即杀 | 685-688 | `ArrowBuff.java` 订阅 `CharAttackEvent` |
| Preparation KO | 690-700 | `Preparation.java` 订阅 |
| DeathMark 恐惧收割 | 697 | `DeathMark.java` 订阅 |

---

### 优先级 2：damage() 方法（约270行可迁移）

#### 2.1 伤害来源特殊处理（行1081-1116）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| CelestialSphere 转魔法伤害 | 1081-1086 | `CelestialSphere.java` 订阅 `CharDamageEvent` |
| LunarCorona 月相伤害修正 | 1093-1100 | `LunarCorona.java` 订阅 `CharDamageEvent` |
| Turtleir 护甲吸收 | 1103-1108 | `Turtleir.java` 订阅 `CharDamageEvent` |
| Sunless 护甲吸收 | 1111-1116 | `Sunless.java` 订阅 `CharDamageEvent` |

#### 2.2 受伤时的状态处理（行1123-1183）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| LifeLink 伤害分摊 | 1123-1141 | `LifeLink.java` 订阅 `CharDamageEvent` |
| Terror/Dread/Charm 恢复 | 1143-1154 | 各 Buff 订阅 `CharDamageEvent` |
| Frost/MagicalSleep 打断 | 1155-1160 | 各 Buff 订阅 `CharDamageEvent` |
| Doom 1.67x 增伤 | 1161-1163 | `Doom.java` 订阅 `CharDamageEvent` |
| DeathMark 1.25x 增伤 | 1165-1167 | `DeathMark.java` 订阅 `CharDamageEvent` |
| Sickle 收割流血 | 1169-1183 | `Sickle.java` 订阅 `CharDamageEvent` |

#### 2.3 受伤时的天赋/Buff效果（行1185-1282）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| ChampionEnemy/Hero 受伤因子 | 1185-1191 | 各自订阅 `CharDamageEvent` |
| AntiMagic/ArcaneArmor 减伤 | 1201-1204 | `AntiMagic.java` / `ArcaneArmor.java` |
| PROVOKED_ANGER 天赋 | 1214-1220 | `Talent.java` 订阅者 |
| ShieldBuff 吸收 | 1222-1228 | 已有 absorbDamage()，可订阅事件 |
| VitaeBuff 吸收 | 1232-1241 | 已有 absorbDamage()，可订阅事件 |
| VITAE_BOOST 天赋 | 1237-1241 | `Talent.java` 订阅者 |
| ImmortalShield | 1243 | `ImmortalShieldAffecter.java` 订阅 |
| BLOODY_VITAE 天赋 | 1247-1250 | `Talent.java` 订阅者 |
| Grim 附魔触发 | 1255-1270 | `Grim.java` 订阅 `CharDamageEvent` |
| Kinetic 动能储存 | 1272-1282 | `Kinetic.java` 订阅 `CharDamageEvent` |

#### 2.4 伤害图标选择（行1285-1321）
**当前问题**：巨大的 if-else 链判断伤害图标

**迁移方案**：让 DamageType 或伤害来源自带图标信息，或使用 `CharDamageEvent` 让来源自行设置图标。

---

### 优先级 3：hit() 方法（约130行可迁移）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| AfterImage 绝对闪避 | 772-786 | `AfterImage.java` 订阅 `CharHitCalcEvent` |
| Axe_D 必中（敌HP<HT时） | 788-798 | `Axe_D.java` 订阅 `CharHitCalcEvent` |
| PneumFistGloves 必中 | 801-814 | `PneumFistGloves.java` 订阅 `CharHitCalcEvent` |
| FatedDraw 命中/闪避取最大值 | 836-844, 866-873 | `FatedDraw.java` 订阅 `CharHitCalcEvent` |
| RingOfBenediction 命中加成 | 847-853 | `RingOfBenediction.java` 订阅 |
| Bless/Hex/Daze 命中修正 | 854-876 | 各 Buff 订阅 `CharHitCalcEvent` |
| ChampionEnemy/Hero 命中修正 | 860-883 | 各自订阅 |

---

### 优先级 4：其他方法

#### 4.1 speed()（行980-1008）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| RingOfBenediction 速度修正 | 984-992 | `RingOfBenediction.java` 订阅 `CharSpeedCalcEvent` |
| HolyLand 减速 | 994-999 | `HolyLand.java` 订阅 `CharSpeedCalcEvent` |
| 各种 Buff 速度因子 | 1001-1007 | 各 Buff 订阅（部分已有 speedFactor()） |

#### 4.2 attackProc()（行934-968）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| LightKing 伤害修正 | 940-961 | `LightKing.java` 订阅 `CharDamageCalcEvent` |

#### 4.3 resist()（行1621-1652）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| IRON_MUSCLE 天赋抗性 | 1637-1648 | `Talent.java` 订阅 `CharResistCalcEvent` |

#### 4.4 add(Buff)（行1487-1528）

| 效果 | 行号 | 迁移目标 |
|------|------|---------|
| PotionOfCleansing 阻止负面 | 1489-1495 | `PotionOfCleansing.java` 订阅 `CharBuffAddEvent` |
| Masamune 阻止 Hex/Vertigo | 1497-1502 | `Masamune.java` 订阅 `CharBuffAddEvent` |

---

## 四、迁移策略

### 4.1 渐进式迁移步骤

对每个要迁移的效果：

1. **创建/使用已有事件** - 确定该效果应该在哪个事件阶段介入
2. **在物品/Buff类中创建订阅者** - 使用 `@SubscribeEvent` 注解
3. **在 Char.java 中发布事件** - 在合适的位置 emit 事件
4. **删除硬编码逻辑** - 确认新逻辑工作后删除旧代码
5. **测试验证** - 确保效果与迁移前一致

### 4.2 迁移顺序建议

**第一批（低风险，高收益）**：
- 伤害图标选择逻辑（纯展示，不影响逻辑）
- speed() 中的 Buff 速度因子（已有 speedFactor() 模式的统一迁移）

**第二批（中等风险）**：
- attack() 中的武器暴击逻辑（每个武器独立迁移）
- damage() 中的护甲特殊效果（LunarCorona/Turtleir/Sunless）

**第三批（高风险，需仔细测试）**：
- attack() 伤害乘算链（涉及战斗平衡）
- hit() 命中/闪避逻辑
- damage() 中的天赋触发逻辑

### 4.3 注意事项

1. **优先级设计**：订阅者的 priority 必须合理
   - 伤害计算修改器：priority = 50（核心逻辑）
   - 展示/日志：priority = -10（低优先级）
   - 拦截/取消：priority = 100（最高优先级）

2. **性能考虑**：
   - 事件发布有反射开销，但编译时索引已解决
   - 避免在高频事件（如每帧）中发布

3. **向后兼容**：
   - 迁移期间新旧代码可并存
   - 使用 `@Deprecated` 标记旧方法

4. **DamageInfo 整合**：
   - 新事件应优先使用 DamageInfo 传递伤害数据
   - 逐步淘汰 `damage(int, Object)` 旧接口

---

## 五、迁移后的 Char.java 预期结构

```java
// Char.java 迁移后的核心方法（示意）

public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
    // 1. 发布攻击事件（订阅者可修改参数）
    CharAttackEvent attackEvent = new CharAttackEvent(this, enemy, dmgMulti, dmgBonus, accMulti);
    EventManager.emit(attackEvent);
    if (attackEvent.isCancelled()) return false;
    
    // 2. 命中判定（发布事件，订阅者可修改命中结果）
    CharHitCalcEvent hitEvent = new CharHitCalcEvent(this, enemy, accMulti);
    EventManager.emit(hitEvent);
    if (!hitEvent.isHit()) { /* miss handling */ return false; }
    
    // 3. 伤害计算（发布事件，订阅者可添加 modifier）
    CharDamageCalcEvent calcEvent = new CharDamageCalcEvent(this, enemy, baseDamage);
    EventManager.emit(calcEvent);
    int finalDamage = calcEvent.getFinalDamage();
    
    // 4. 应用伤害
    CharDamageEvent damageEvent = new CharDamageEvent(enemy, finalDamage, this);
    EventManager.emit(damageEvent);
    enemy.damage(damageEvent.getDamage(), this);
    
    // 5. 后续处理（击杀、特效等）
    return true;
}
```

**预期行数减少**：从 1748 行减少到约 800-1000 行（减少 40-45%）

---

## 六、文件结构规划

迁移后的新文件：

```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/
├── events/
│   ├── CharAttackEvent.java          # 新增
│   ├── CharDamageCalcEvent.java      # 新增
│   ├── CharDamageEvent.java          # 新增
│   ├── CharHitCalcEvent.java         # 新增
│   ├── CharSpeedCalcEvent.java       # 新增
│   ├── CharResistCalcEvent.java      # 新增
│   └── CharBuffAddEvent.java         # 新增
├── events/handlers/                  # 新增目录：事件处理器
│   ├── WeaponCritHandler.java        # 武器暴击逻辑
│   ├── TalentCombatHandler.java      # 天赋战斗逻辑
│   ├── BuffDamageHandler.java        # Buff伤害修正
│   └── ArmorDamageHandler.java       # 护甲伤害修正
```

---

## 七、验收标准

- [ ] Char.java 中不再直接 import 具体武器/护甲/戒指类
- [ ] 所有物品/Buff/天赋的特殊效果通过事件订阅实现
- [ ] 战斗结果与迁移前完全一致（可通过自动化测试验证）
- [ ] 新增物品/天赋不再需要修改 Char.java
- [ ] 性能无明显下降（事件发布开销 < 0.1ms/次）

---

*文档版本：1.0*
*创建日期：2026年7月27日*
*项目：Radish Pixel Dungeon*
