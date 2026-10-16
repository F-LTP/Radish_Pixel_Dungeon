# 武器标签分类与附魔门控计划（设计中·未开始）

> 状态：**设计中，未开始实施**。本文件先确定武器分类（多标签）与附魔/诅咒门控方向，作为后续实现 `WeaponTag` / `allowsEnchantment()` 的依据。

## 一、目标

- 给武器引入**多维度、可多值**的标签（`EnumSet<WeaponTag>`），而非单一枚举。
- 用标签驱动附魔/诅咒的适用性判定：附魔声明所需标签，武器声明自身标签，二者匹配才可附魔。
- 支持"通用附魔"（所有武器可用）与"分类专属附魔"（仅近战 / 仅投掷 / 仅法杖等）。
- 限制定义在基类上，子类自动继承并可继续叠加。

## 二、标签体系

一种武器可同时拥有多个标签，按维度分组。**默认标签从表中省略**（见第三节）。

### 2.1 攻击方式 AttackMode
| 标签 | 含义 |
|---|---|
| `MELEE` | 近战 |
| `THROWN` | 投掷 |
| `LAUNCHER` | 发射器（弓 / 弩） |
| `AMMO` | 弹药（飞镖 / 箭） |
| `CAST` | 施法（法杖） |

### 2.2 触发通道 ProcChannel（门控核心）
| 标签 | 含义 |
|---|---|
| `PROC_MELEE` | 近战命中可触发 |
| `PROC_RANGED` | 远程 / 投掷命中可触发 |
| `PROC_KILL` | 击杀时触发 |
| `PROC_SWING` | 需要挥击动作（如 Combos / Blocking / Striking） |
| `PROC_PROJECTILE` | 需要弹道（如 Seeking） |

### 2.3 攻速 Tempo（正常攻速不加标签）
| 标签 | 判定 |
|---|---|
| `VERY_FAST` | `DLY ≤ 0.5`（约 2 倍速及以上） |
| `FAST` | `0.5 < DLY < 1.0` |
| `SLOW` | `1.0 < DLY < 1.5` |
| `VERY_SLOW` | `DLY ≥ 1.5` |

### 2.4 形态 Form
`SWORD` `DAGGER` `AXE` `BLUNT` `POLEARM` `SCYTHE` `WHIP` `FIST` `STAFF` `SHIELD` `CROSSBOW` `BOW` `THROWABLE` `SPIKE` `BOOK` `ORB` `TOOL` `FLAG` `CANNON` `SPECIAL`

### 2.5 材质 Material
| 标签 | 含义 |
|---|---|
| `WOOD` | 木 |
| `IRON` | 铁 / 钢（金属） |
| `STONE` | 石 |
| `BONE` | 骨 |
| `GLASS` | 玻璃 / 水晶 |
| `CLOTH` | 布 / 皮革 |
| `GOLD` | 金 |
| `SILVER` | 银 |
| `ARCANE` | 秘法 / 魔法材质 |
| `ORGANIC` | 有机 / 血肉 |

### 2.6 物理伤害类型 DamageType
| 标签 | 含义 |
|---|---|
| `DMG_BLUNT` | 钝击 |
| `DMG_SLASH` | 劈砍 |
| `DMG_PIERCE` | 穿刺 |
| `DMG_MAGIC` | 法术伤害（非物理，仅用于纯法术武器） |

### 2.7 射程 Range
`REACH`（`RCH > 1`，有额外攻击距离）

### 2.8 特殊机制 Mechanic
| 标签 | 含义 |
|---|---|
| `ABILITY` | 有主动技能 / 决斗家技能 |
| `BLOCK` | 提供格挡 / 防御 |
| `UNARMED` | 徒手 |
| `USES_AMMO` | 需要消耗弹药 |
| `WAND` | 法杖（绑 wand） |
| `BOOMERANG` | 回旋（已包含返回语义，不再单列返回标签） |
| `NOT_STICKY` | 命中后不钉住（投掷默认会钉住） |
| `MOBILE` | 移动后可立即行动（如手里剑） |
| `AREA` | 范围效果 |
| `SUMMON` | 召唤 |
| `SACRIFICE` | 献祭 / 消耗生命 |
| `CHARGE` | 充能 / 蓄力 |
| `UTILITY` | 非战斗功能（修理 / 抓取 / 读书等） |
| `FRAGILE` | 易碎（如玻璃剑） |

> 已删除的冗余标签：`THROWN_RETURN`（由 `BOOMERANG` 涵盖）、`STICKY`（投掷默认，仅保留 `NOT_STICKY`）、`STACKABLE` / `DURABLE`（投掷默认，见默认表）。

## 三、默认标签

| 类别 | 默认标签（表中省略） |
|---|---|
| 近战基类 | `MELEE`, `PROC_MELEE` |
| 投掷基类 | `THROWN`, `PROC_RANGED`, `STACKABLE`, `DURABLE`, `STICKY` |
| 飞镖基类 | `AMMO`, `THROWN`, `USES_AMMO`, `PROC_RANGED`, `STACKABLE` |
| 弓 | `BOW`, `LAUNCHER`, `USES_AMMO`, `PROC_PROJECTILE` |

## 四、武器标签表

> 表中只列**非默认标签**。正常攻速不写攻速标签。

### 4.1 近战 T1
| 武器 | Tier | 标签 |
|---|---|---|
| WornShortsword | 1 | SWORD, IRON, DMG_SLASH, ABILITY |
| MagesStaff | 1 | STAFF, WOOD, ARCANE, DMG_BLUNT, CAST, WAND, ABILITY |
| Dagger | 1 | DAGGER, IRON, WOOD, DMG_PIERCE, ABILITY |
| Gloves | 1 | FIST, CLOTH, IRON, DMG_BLUNT, UNARMED, VERY_FAST, ABILITY |

### 4.2 近战 T2
| 武器 | Tier | 标签 |
|---|---|---|
| Spanner | 2 | TOOL, IRON, DMG_BLUNT, UTILITY |
| Rlyeh | 2 | BOOK, ARCANE, DMG_BLUNT, UTILITY |
| RuneSlade | 2 | SWORD, IRON, ARCANE, DMG_SLASH |
| DarkShadowSword | 2 | SWORD, IRON, ARCANE, DMG_SLASH |
| Glasssword | 2 | SWORD, GLASS, DMG_SLASH, FRAGILE |
| Katar | 2 | FIST, IRON, DMG_PIERCE, VERY_FAST |
| BladeShield | 2 | SHIELD, IRON, WOOD, DMG_SLASH, BLOCK |
| SilverSting | 2 | DAGGER, IRON, SILVER, DMG_PIERCE |
| BoneClaw | 2 | FIST, BONE, DMG_SLASH |
| RottenLance | 2 | POLEARM, WOOD, DMG_PIERCE, REACH, VERY_SLOW, BLOCK, ABILITY |

### 4.3 近战 T3
| 武器 | Tier | 标签 |
|---|---|---|
| EnemyFlag | 3 | FLAG, POLEARM, CLOTH, WOOD, DMG_BLUNT, REACH, UTILITY |
| LockChain | 3 | WHIP, IRON, DMG_BLUNT, REACH |
| LongStick | 3 | STAFF, WOOD, DMG_BLUNT, UTILITY |
| WhiteKingGodSword | 3 | SWORD, IRON, ARCANE, DMG_SLASH, UTILITY |
| Beecomb | 3 | SPECIAL, ORGANIC, DMG_BLUNT, SUMMON, ABILITY |
| Waterwheel | 3 | BLUNT, WOOD, DMG_BLUNT |
| WingSword | 3 | SWORD, IRON, ARCANE, DMG_SLASH, ABILITY |
| SnakeSpear | 3 | POLEARM, IRON, WOOD, DMG_PIERCE, REACH, SLOW |
| SufferingDagger | 3 | DAGGER, IRON, DMG_PIERCE |
| PneumFistGloves | 3 | FIST, IRON, CLOTH, DMG_BLUNT, FAST, ABILITY |
| GrapplingHook | 3 | TOOL, IRON, DMG_BLUNT, UTILITY, ABILITY |

### 4.4 近战 T4
| 武器 | Tier | 标签 |
|---|---|---|
| Morello | 4 | BOOK, ARCANE, DMG_BLUNT, CHARGE, UTILITY |
| CircleSword | 4 | SWORD, IRON, DMG_SLASH, REACH |
| Grimtooth | 4 | DAGGER, BONE, DMG_PIERCE |
| CelestialSphere | 4 | ORB, ARCANE, GLASS, DMG_MAGIC, UTILITY |
| EndGuard | 4 | SWORD, IRON, DMG_SLASH |
| ShadowBooks | 4 | BOOK, ARCANE, DMG_BLUNT, UTILITY |
| Bloodblade | 4 | SWORD, BONE, DMG_SLASH, SACRIFICE, ABILITY |
| CompositeCrossbow | 4 | CROSSBOW, WOOD, IRON, DMG_PIERCE, LAUNCHER, USES_AMMO, PROC_PROJECTILE, ABILITY |
| Darksword | 4 | SWORD, IRON, ARCANE, DMG_SLASH |
| HeadCleaver | 4 | AXE, IRON, DMG_SLASH, SLOW |
| Seekingspear | 4 | POLEARM, IRON, WOOD, DMG_PIERCE, REACH |
| ReplacePoint | 4 | SPECIAL, IRON, DMG_PIERCE, VERY_FAST |
| BoneSpear | 4 | POLEARM, BONE, DMG_PIERCE, ABILITY |
| HeavyCannon | 4 | CANNON, IRON, DMG_BLUNT, AREA, ABILITY |

### 4.5 近战 T5
| 武器 | Tier | 标签 |
|---|---|---|
| Taijutsu | 5 | FIST, ORGANIC, DMG_BLUNT, UNARMED, UTILITY |
| GiantKiller | 5 | SWORD, IRON, GOLD, DMG_SLASH |
| FogSword | 5 | SWORD, IRON, ARCANE, DMG_SLASH |
| EchoplexHammer | 5 | BLUNT, IRON, DMG_BLUNT, PROC_KILL |
| KillBoatSword | 5 | SWORD, IRON, DMG_SLASH, SPECIAL |
| Cutterhead | 5 | AXE, IRON, DMG_SLASH |
| Scythe | 5 | SCYTHE, IRON, WOOD, DMG_SLASH, ABILITY |
| Axe_D | 5 | AXE, IRON, WOOD, DMG_SLASH |
| ScorpionCrossbow | 5 | CROSSBOW, IRON, WOOD, DMG_PIERCE, LAUNCHER, USES_AMMO, PROC_PROJECTILE |
| Tonfa | 5 | BLUNT, WOOD, IRON, DMG_BLUNT, VERY_FAST, ABILITY |

### 4.6 投掷
| 武器 | Tier | 标签 |
|---|---|---|
| ThrowingStone | 1 | BLUNT, STONE, DMG_BLUNT, NOT_STICKY |
| ThrowingKnife | 1 | DAGGER, IRON, DMG_PIERCE |
| ThrowingSpike | 1 | SPIKE, IRON, DMG_PIERCE |
| FishingSpear | 2 | POLEARM, IRON, WOOD, DMG_PIERCE |
| ThrowingClub | 2 | BLUNT, WOOD, STONE, DMG_BLUNT, NOT_STICKY |
| Shuriken | 2 | SPECIAL, IRON, DMG_SLASH, MOBILE |
| ThrowingSpear | 3 | POLEARM, IRON, WOOD, DMG_PIERCE |
| Kunai | 3 | DAGGER, IRON, DMG_PIERCE |
| Bolas | 3 | SPECIAL, IRON, CLOTH, DMG_BLUNT, UTILITY |
| Javelin | 4 | POLEARM, IRON, WOOD, DMG_PIERCE |
| Tomahawk | 4 | AXE, IRON, WOOD, DMG_SLASH |
| HeavyBoomerang | 4 | BOOMERANG, WOOD, DMG_BLUNT, NOT_STICKY |
| Trident | 5 | POLEARM, IRON, DMG_PIERCE |
| ThrowingHammer | 5 | BLUNT, IRON, DMG_BLUNT, NOT_STICKY |
| ForceCube | 5 | SPECIAL, STONE, ARCANE, DMG_BLUNT, AREA, NOT_STICKY |

### 4.7 飞镖
| 武器 | Tier | 标签 |
|---|---|---|
| Dart | 1 | — |
| TippedDart | 2 | — |
| AdrenalineDart | 2 | 肾上腺素 |
| BlindingDart | 2 | 致盲 |
| ChillingDart | 2 | 冰缓 |
| CleansingDart | 2 | 净化 |
| DisplacingDart | 2 | 传送 |
| HealingDart | 2 | 治疗 |
| HolyDart | 2 | 神圣伤害 |
| IncendiaryDart | 2 | 燃烧 |
| ParalyticDart | 2 | 麻痹 |
| PoisonDart | 2 | 中毒 |
| RotDart | 2 | 腐蚀 |
| ShockingDart | 2 | 感电 |

### 4.8 弓
| 武器 | Tier | 标签 |
|---|---|---|
| SpiritBow | — | WOOD, CLOTH, DMG_PIERCE |

## 五、附魔 / 诅咒门控规则（示例）

- 附魔声明"所需标签集合"，武器需**全部满足**才可附着。
- 无额外需求（`GENERIC`）对所有武器开放。
- 示例映射：

| 附魔 / 诅咒 | 所需标签 | 备注 |
|---|---|---|
| Blazing / Chilling / Shocking / Blooming / Corrupting / Lamprey | 无（GENERIC） | 命中即可，近战 / 投掷通用 |
| Combos / Striking / Blocking | `PROC_SWING` | 仅近战挥击 |
| Seeking | `PROC_PROJECTILE` | 仅远程弹道 |
| Projecting | `PROC_MELEE` 或 `PROC_RANGED` | 近战加距离 / 投掷改瞄准 |
| Euphoria / Lucky | `PROC_KILL` | 击杀触发 |
| Grim / Vampiric | 无，但对 `FAST` / `VERY_FAST` 降权或禁用 | 防高频超模 |
| WetEnchantment | `MELEE` | 小骑士专属 |
| （预留）火焰类 | 非 `WOOD` 或对 `WOOD` 降权 | 材质门控示例 |
| （预留）闪电类 | 优先 `IRON` | 材质门控示例 |

- 基类白名单 / 黑名单作为标签之外的例外，例如某投掷武器全局禁 `Blazing`。
- 具体平衡数值与禁用名单待实现阶段逐条确认。

## 六、不参与生成的武器

以下 **Shattered 传统武器**当前不在本牢生成池中，暂不纳入标签表：

`Shortsword` `Sword` `Longsword` `Greatsword` `BattleAxe` `Flail` `Gauntlet` `Glaive` `Greataxe` `Greatshield` `Mace` `Quarterstaff` `Rapier` `Scimitar` `Sickle` `Spear` `WarHammer` `WarScythe` `Whip` `Katana` `AssassinsBlade` `RoundShield` `RunicBlade` `HandAxe` `Dirk` `Crossbow` `Crowbar`

其余 Boss / 专属 / 旧版遗留武器（`Yamato` `KnightGS` `Holyankh` `MetalCross` `DirkOfB` `JutteChampionWeapon` `DogLeg` 及 `legacyItem/*`）同样不在生成池，待有需要时再单独补标签。

## 七、实施建议

1. 先落 `WeaponTag` 枚举与 `Weapon.weaponTags()`（默认空集，子类在初始化块填充）。
2. 基类按第三节填充默认标签；各武器只补差异标签。
3. 附魔侧加 `requiredTags()`，`Weapon.allowsEnchantment()` 默认做"需求 ⊆ 武器标签"判定，再叠加基类白 / 黑名单。
4. 随机选择改为武器感知（`Enchantment.randomFor(weapon, ...)`），收口 `Weapon.random()` / `enchant()` / 卷轴三选一 / 灌注等入口。
5. 材质与伤害类型标签先作为数据记录，门控规则待平衡阶段再启用。
