# 伤害系统重构改动日志 + 回家测试清单

> 状态：📋 记录（重构已完成，本文为过程日志与测试清单，供回归参考）。
> 用途：本次对 DamageType/DamagePipeline 的大型重构改动记录，供你回家逐个编译验证。
> 所有改动**未跑 Gradle**，以下按功能域归类，每项都给了「改了什么」和「回家怎么测」。

---

## 一、核心管线反转（DamageInfo 权威）

### 改了什么
- `damage/DamageResult.java`：补全字段 `armorBlocked`、`resistanceBlocked`（现为 baseDamage / modifiedDamage / armorBlocked / resistanceBlocked / shieldBlocked / hpDamage / immune）。
- `damage/DamagePipeline.java`：`apply()` 改为调用 `Char.applyDamage(DamageInfo)` 作为**唯一权威**实现，返回 `DamageResult`；保留 `ACTIVE` ThreadLocal 供嵌套伤害继承类型。
- `actors/Char.java`：
  - 新增 `applyDamage(DamageInfo)`（public，返回 `DamageResult`）：旧 `damage(int,Object,DamageType)` 的完整逻辑搬入，改为从 DamageInfo 读 `type/source/damage`。
  - `damage(int,Object)` 改为包装成 `DamageInfo.fromSource` → 走管线（**兼容入口**，未删除）。
  - 删除 `applyDamageLegacy`、`CritClass`、`NoArmorCritClass`。

### 回家怎么测
- 普通攻击、暴击、陷阱、DoT、毒/火/冰/雷伤害是否正常生效。
- 浮字图标是否正常（跳字已改为按 DamageType，见第四节）。

---

## 二、护甲 DR 进入管线（armorBlocked 变真实）

### 改了什么
- `actors/Char.java` `applyDamage`：新增「应用护甲」阶段，护甲 DR 作为**直接加算**（在承伤倍率乘算之前）扣除，记入 `armorBlocked`。
  - 仅对「有攻击者 + 物理 + 不无视防御」生效；陷阱/DoT（攻击者为空）不套护甲。
- `damage/OrdinaryAttackDamage.java`：`foldPostProcessing` 移除 `-dr`；`ignoresDefenseRoll` 改为 public。
- 20 个 `RadishEnemy` mob + `Deminion`：移除各自内联的护甲扣减（护甲统一由管线在防守方算）。

### ⚠️ 数值顺序变化（重要）
护甲现在在管线里，位于承伤倍率（lunar ×2、doom ×1.67、champion、抗性）**之后**做平坦扣减。即「先乘后减」。与旧逻辑「先减后乘」数值不同，**平衡会变**。

### 回家怎么测
- 普通物理攻击的护甲吸收量是否符合预期（护甲值 × 减伤）。
- 天球仪（CelestialSphere）、狙击手远程、Torturer 等「无视护甲」的来源是否仍无视护甲。
- 陷阱/毒等非攻击伤害**不应**套护甲。
- 各 RadishEnemy 战斗是否正常。

---

## 三、双乘区（叠加 + 依次）

### 改了什么
- `damage/DamageModifier.java`：新增 `STACK_MULTIPLICATIVE` 类型 + `stackMult()` 工厂。
- `damage/DamageInfo.java`：
  - 新增 `stackMultiplicatives` 列表 + `addStackMultModifier(value, source)`。
  - `getDamage()` 计算顺序：直接加算 → **叠加乘区**（`1 + Σ(value-1)`）→ 依次乘算（累乘）→ 暴击 → 最终加算。
  - 例：两个 `addStackMultModifier(1.5f,...)` = `1+0.5+0.5 = ×2.0`；两个 `addDirectMultModifier(1.5f,...)` = `×1.5×1.5 = ×2.25`。
  - 删除死代码 `invalidateCache()`/`recalculate()`。
- 现有承伤倍率（lunar/doom/champion/抗性）**维持原版依次乘算**，未迁移（新增叠加乘区能力保留备用）。

### 回家怎么测
- 现有伤害数值是否与之前基本一致（叠加乘区能力默认未用，不改变现有战斗）。
- 新增需求若用到叠加乘区，用 `addStackMultModifier` 验证是否按 `1+Σ` 计算。

---

## 四、跳字图标改为 DamageType 驱动

### 改了什么
- `actors/Char.java` 跳字段：删掉 17 个 `src instanceof ...` 长链，改为以 `info.getFloatingTextIcon()`（按 DamageType + 暴击）为基准。
- 仅保留少数特例：`NO_ARMOR_PHYSICAL_SOURCES`（只对 PHYSICAL）、`AntiMagic.RESISTS`、狙击手远程、`OnlyOneEyeAttack`、`DeminionCritClass`。

### 回家怎么测
- 毒/火/冰/闪电/流血/腐蚀/粘液/延迟/腐化/护符/镐子/魔法的浮字图标是否仍正确。
- 暴击图标（CRIT / CRIT_NO_BLOCK）是否正确。
- 无护甲物理伤害（Spike/Rockfall/Chasm 等）图标是否仍显示「无视护甲」。

---

## 五、类型化抗性层 + 抗性注册迁移

### 改了什么
- `actors/Char.java` / `actors/buffs/Buff.java`：新增按 DamageType 的抗性层 `typeResistances` / `typeImmunities`，接口 `resistanceTo(DamageType)` / `isImmuneTo(DamageType)`。
- `Property` 枚举：元素抗性改为按类型注册（FIERY→FIRE、ICY→FROST、ACIDIC→CORROSIVE、ELECTRIC→LIGHTNING）。
- **纯伤害类**注册从 `immunities/resistances`（基于类）**移动**到 `typeImmunities/typeResistances`：
  - ToxicGas→TOXIC、CorrosiveGas→CORROSIVE、Electricity→LIGHTNING、Fire/Inferno/WandOfFireblast/EternalFire/FireBlob→FIRE、WandOfDisintegration/ScrollOfRetribution/ScrollOfPsionicBlast/DeathGaze/DisintegrationTrap→MAGICAL、StormCloud→LIGHTNING、GeyserTrap→WATER。
- **双用途类**（Frost/Chill/Poison/Burning）：因同时驱动 debuff 时长，**免疫 ADD（保留 class + 加 type）**、**减免保留 class**（避免 `0.5×0.5` 双计数）。
- 同步更新直接检查点（blob / MagicalFireRoom / Tengu 的 `isImmune(Fire.class)` 等 → `isImmuneTo(type)`）。
- 管线抗性判定保持「双轨」：`isImmuneTo(type) || isImmune(srcClass)`、`resistanceTo(type) * resist(srcClass)`。

### ⚠️ 需重点回归
- 各元素/DoT 的**免疫**和**抗性**是否仍生效（火、冰、毒、腐蚀、闪电、毒气）。
- **双用途类的 debuff 时长**（冰冻/中毒/灼烧/麻痹时长）是否仍受抗性影响。
- 元素属性怪（FIERY/ICY/ACIDIC/ELECTRIC）对对应元素的减免是否仍 50%。

### 涉及文件
`actors/blobs/*`(ToxicGas/CorrosiveGas/Fire/Electricity/StormCloud)、`actors/buffs/*`(FireImbue/FrostImbue/ToxicImbue/ChampionEnemy/ChampionHero/AnkhInvulnerability/BlobImmunity/Frost)、`actors/mobs/*`(Eye/DM300/Spinner/YogFist/FungalSentry/Piranha/RotHeart/RotLasher/Deviloon/Tengu/MirrorImage/PrismaticImage/RadishEnemy 若干)、`levels/rooms/special/MagicalFireRoom`。

---

## 六、fromSource 调用点迁移（部分完成）

### 已迁移（改成显式 DamageInfo，`fromSource` 会判错类型、影响抗性/图标）
| 文件 | 类型 |
|---|---|
| `WarpBeacon`（法师传送粉碎，2 处） | MAGICAL |
| `DisintegrationTrap`（死亡射线） | MAGICAL |
| `HolyDart`（神圣伤害） | MAGICAL |
| `PoisonDartTrap`（物理镖，2 处） | PHYSICAL |
| `WornDartTrap`（物理镖，2 处） | PHYSICAL |
| `GrimTrap`（即死，2 处） | TRUE |
| `ArcaneBomb` / `HolyBomb` | 本就显式 MAGICAL（无需改） |

### ⚠️ 未完成（剩余约 80+ 文件、~260 处 `damage(int,Object)` 调用）
- `ElementalStrike`：伤害类型随附魔分支变化（物理/闪电/即死），**未迁移**。
- 其余 mob 技能、部分法杖/法术、怪物能力仍走 `damage(int,Object)` + `fromSource`。
- **`fromSource` / `isNoArmorPhysicalSource` / `NO_ARMOR_PHYSICAL_SOURCES` 尚未删除**（要等全部调用点显式化后才能删）。

### 回家怎么测（针对已迁移项）
- 法师 WarpBeacon 传送粉碎对魔法抗性/魔免怪是否生效。
- DisintegrationTrap 射线是否被魔免。
- HolyDart 对亡灵/恶魔是否造成魔法伤害。
- GrimTrap 是否无视护甲/护盾/抗性直接扣血（即死）。
- 毒镖/锈镖陷阱的物理伤害是否正常。

---

## 七、总体测试清单（优先级从高到低）

1. **编译**：全工程能否编译通过（改动面大，最可能卡编译）。
2. **普通战斗回归**：普攻、暴击、护甲吸收、护盾、毒/火/冰/雷 DoT、死亡流程、浮字。
3. **元素抗性回归**：火/冰/毒/腐蚀/闪电的免疫与 50% 减免；双用途类（冰冻/中毒）时长。
4. **无视护甲来源**：天球仪/狙击手/Torturer/Spike/Rockfall/Chasm。
5. **护甲数值**：确认「先乘后减」的新顺序下护甲吸收量符合预期。
6. **已迁移调用点**：第六节列出的 WarpBeacon/DisintegrationTrap/HolyDart/三种陷阱。

---

## 八、未迁移完成清单（剩余工作）

> 以下都是**本次尚未完成**、仍需后续处理的项。按「结构性」和「调用点」两类列出。

### A. 结构性未完成（删不掉/迁不动的）

| 项 | 说明 | 阻碍 |
|---|---|---|
| `fromSource`（`DamageType.fromSource`，~100 行类名猜测） | 删除 | 需先让全部 `damage(int,Object)` 调用点显式化 |
| `isNoArmorPhysicalSource` / `NO_ARMOR_PHYSICAL_SOURCES`（`Char` 类清单） | 删除 | 仍是 `OrdinaryAttackDamage.ignoresArmor` 判类型用 + 跳字用 |
| 双用途类（Frost/Chill/Poison/Burning）完整迁到类型层 | 未迁 | 它们同时驱动 debuff 时长（`Buff.java` 用 `resist(Class)`），需先把时长系统改为类型感知 |
| `ElementalStrike` 迁移 | 未迁 | 伤害类型随附魔分支变化（物理/闪电/即死），无法单一归型 |
| 管线改「纯类型判定」 | 未改（保持双轨） | 需等 A 前两项（fromSource/NO_ARMOR 清单）清完 |

### B. 调用点未迁移（仍走 `damage(int,Object)` + `fromSource`）

按类别列出**仍残留旧调用**的主要文件（共约 90 个，逐个都需改成显式 `DamageInfo`）：

- **骰子法师法术**：BladeRain/Blaze/Burst/Combustion/Cut/Flick/Hemlock/Laceration/LightBeam/LightPoke/Mark/Miasma/Scald/Scorch/Shine/Vine Spell
- **英雄能力**：ElementalBlast、ShadowHymn、HeroicLeap、Shockwave、StormAttackArrow
- **怪物技能**：CrystalSpire、CrystalWisp、DM100/DM100H、DwarfKing、Elemental、Eye、Mob、Necromancer/SpectralNecromancer、Shaman、Skeleton、Statue、Tengu、Warlock、YogFist
- **RadishEnemy / RadishBoss**：Artillerist、ClusteredSkeleton、Deminion、DemonLord、Deviloon、DM175、Dog、Drake、GiantWorm、GnollZealot、Goblin、Gorgon、Grudge、Jailer、Mayfly、Prisoner、RoyalGuard、ShieldMage、StoneSpirit、Torturer、BigSnake_Zikk、GnollKing、GnollShamanKing
- **Buff**：Berserk、Burning、Combo、HalomethaneBurning、HolyLowBurinng、KickTracker、Belief、Soulstaker
- **武器/物品**：EchoplexHammer、Tonfa、WhiteKingGodSword、MakeshiftSlingshot、Masamune、Turtleir、RingOfDestruction、CursedWand、DamageWand、WandOfDisintegration、WandOfNewStar、WandOfWarding、Heap
- **其他**：CavesBossLevel、WandOfReflectDisintegration、WandOfScanningBeam、DamageResistance

> 说明：上面部分文件（如 Eye、YogFist、Tengu、DisintegrationTrap）已做过**抗性迁移**或部分调用迁移，但**仍可能有其他 `damage(int,Object)` 调用**未显式化，仍需逐处核对。

### C. 未完成的测试 / 校准

- 单元测试（DamageInfo/Pipeline/Result）——项目无 JUnit 依赖，需搭建。
- 护甲数值平衡校准（因「先乘后减」顺序变化）。

### D. 建议的后续顺序

1. 编译通过（先解决编译问题）。
2. 按类别批量显式化调用点（建议先骰子法术 → 再怪物技能 → 再武器物品）。
3. 全部显式化后：删 `fromSource`、`isNoArmorPhysicalSource`、`NO_ARMOR_PHYSICAL_SOURCES`。
4. 管线切纯类型判定。
5. 双用途类时长系统改造 + 完整迁到类型层。
6. 补单元测试。

---

## 九、新增特性：伤害来源链（Damage Cause Chain）

> 本次新增的底层系统。**不改变现有伤害数值**，只增加来源追踪能力。需回归确保无副作用。

### 改了什么
- `damage/DamageInfo.java`：新增 `List<Object> causeChain` 有序来源链 + `addCause(Object)` / `addCauses(Collection)` / `setCauseChain(Collection)` / `getCauseChain()` / `hasCauseChain()`；`copy()` 携带链。
- `actors/Char.java`：新增 `public List<Object> lastDamageCauseChain`，`applyDamage` 时记录最近一次伤害的来源链（与 `lastDamageType` 并列）。
- `damage/DamageCauseFormatter.java`（新增）：`nameOf(Object)` 把 Char/Item/Buff/普通对象转可读名；`describeChain(List)` 把整条链连成一句（如「玩家 → 玩家的武器 → 烈焰附魔 → …」）。消息 key：`damage.damagecauseformatter.unknown/join`（中英已补）。
- 传导端接入（示例）：
  - `actors/buffs/Burning.java`：新增 `causeChain` + `reignite(Char, float, List<?> chain)`；`act()` 造伤害时把链注入 `DamageInfo`。
  - `items/weapon/enchantments/Blazing.java`（烈焰附魔）：点燃/灼烧时传链 `[攻击者, 武器, Blazing]`。
  - `actors/buffs/Bleeding.java`：新增 `causeChain` + `set(float, Class, List<?> chain)`；`act()`/`finishAllBleedingDamage` 注入链。
  - `items/armor/glyphs/Thorns.java`（荆棘）：反伤时传链 `[防御者, Thorns]`。

### 回家怎么测
- **编译**：4 个改动文件 + 1 个新文件能否编译。
- **无副作用回归**：烈焰附魔点燃、灼烧、荆棘反伤、流血伤害数值与之前是否完全一致（本系统不改数值）。
- **API 可用性**（开发侧）：能否用
  ```java
  DamageInfo dmg = new DamageInfo(x, DamageType.FIRE, ...);
  dmg.addCause(attacker).addCause(weapon).addCause(this);
  target.damage(dmg);
  DamageCauseFormatter.describeChain(char.lastDamageCauseChain);
  ```
  正确生成来源描述。
- **死亡/日志**：若在死亡信息或日志中接入 `describeChain`，是否能读到你需要的完整来源链。
- **未实现**：Fire blob 逐格传导链（火 → 草 → 门 → 新目标）尚未做，仅底层备好。

---

## 十、新增特性：混合伤害系统（Mixed Damage）

> 本次新增的伤害类型机制。默认未被任何战斗使用，纯新增能力。需验证数值与 UI 是否符合设计。

### 改了什么
- `damage/DamageType.java`：新增 `MIXED` 哨兵枚举。
- `damage/MixedDamage.java`（新增）：两个平行列表 `types`（成分类型，禁含 MIXED/UNKNOWN/TRUE）+ `percentages`（占比）；`add(DamageType, float)`、`validate()`（**和必须为 1，否则抛异常**）。
- `damage/DamageInfo.java`：新增 `mixed` 字段 + `setMixedDamage(MixedDamage)`（自动 validate 并置 type=MIXED）、`getMixed()`、`isMixed()`；`copy()` 深拷贝；新增 `getFloatingTextIcons()`（混合时按占比从高到低返回图标数组，含暴击）。
- `actors/Char.java` `applyDamage`：混合伤害**按各成分分别判定免疫/抗性后加权**（`Σ pct × resistanceTo(comp) × resist(src)`），免疫成分被挡、全免则 `immuneHit`；非混合走原逻辑。
- 跳字多图标：
  - `sprites/CharSprite.java`：新增 `showStatusWithIcons(color, text, int[] icons)`。
  - `effects/FloatingText.java`：`icon` 字段改为 `ArrayList<Image> icons`，`reset`/`update`/`layout`/`width` 支持多图标，左侧从文字往左排、右侧往右排；新增 `show(..., int[] iconIdxs, boolean left)`。

### 使用方式（供参考，当前无战斗用到）
```java
MixedDamage md = new MixedDamage();
md.add(DamageType.FIRE, 0.5f).add(DamageType.PHYSICAL, 0.5f).validate();
DamageInfo dmg = new DamageInfo(x, DamageType.MIXED, ...);
dmg.setMixedDamage(md);   // 自动 validate + 置 MIXED
target.damage(dmg);
```

### 回家怎么测
- **编译**：2 个新文件（MixedDamage）+ 4 个改动文件能否编译。
- **单一伤害跳字回归**：普通物理/火/毒等跳字图标是否仍正常（FloatingText 重构是否影响单图标）。
- **`MixedDamage.validate()` 断言**：和 != 1 时是否抛异常；和 == 1 时正常。
- **混合减免**：对某成分免疫的怪（如对火焰免疫），混合 `[FIRE 50%, PHYSICAL 50%]` 时火焰部分被挡、物理部分照常，总伤害是否符合加权预期。
- **混合跳字**：`showStatusWithIcons` 是否在跳字左侧显示多个图标，且按占比从高到低排序；暴击时图标是否正确（CRIT/CRIT_NO_BLOCK）。
- **FloatingText 布局**：多图标在左对齐/右对齐、多个跳字堆叠时布局是否美观、无错位。

### 备注
- 若后续有战斗想用混合伤害，只需按上面「使用方式」构造即可，底层已备好。
