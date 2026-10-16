# Char 伤害管线内联逻辑迁移计划（剩余项）

> **状态：批次 C1 / C2 / C3 已完成（见「三·补」）；「三」表剩余项只剩 ImmortalShield 与 Grim（均为「保持」）。**
> 「三」行号已刷新为当前代码（`Char.java` 共 1681 行）；「四 / 五」的行号仍是迁移前的旧值，未刷新。

## 一、背景与目标

`Char.java` 里仍堆积着装备 / 天赋 / 职业专属逻辑。根因有三条：

1. **管线阶段依赖**：效果只在特定阶段成立（护甲前后、抗性前后、护盾前后），顺序即语义；
2. **无 Buff 实例的物品效果**：护甲刻印（如 AntiMagic）本身不是 Buff，没有对象可挂钩子；
3. **build 期钩子覆盖不全**：`Buff.modify*Damage` 只在 `OrdinaryAttackDamage.build` 里被调用，法杖、陷阱、泥浆、饥饿等不经过该路径。

**目标**：修改型效果走管线钩子（`IDamageModifier`）；反应型走事件；`Char` 只留框架与发布点。

## 二、迁移机制（后续迁移的落点）

- `damage.IDamageModifier`：实现者（通常是 Buff）对承伤值施加"原始"（抗性前）或"最终"（抗性后、护盾前）修正，每类分**固定加算 / 百分比加算 / 乘算**，方法签名统一为 `(int dmg, DamageInfo info)`。聚合规则见 `Char.applyRawDamageModifiers` / `applyFinalDamageModifiers`（后者额外接收伤害类型抗性系数，见「三·补」C2）。
- `Buff.onDamageTaken(int dealt, DamageInfo info)`：受击之后的反应 / 副作用（拿到类型、来源、攻击者、来源链）。
- `Buff.onPreShieldDamage(int dmg, int hpBefore, DamageInfo info)`：护盾吸收前的受击反应（最终修正之后、护盾与 HP 扣减之前）。携带护盾前伤害与受击前 HP，只读；改伤仍走 `IDamageModifier`。
- 进管线原始伤害（全来源）经 `CharUnprocedDamageEvent` 获取；实际扣血经 `CharFinalDamageEvent`；护盾由正值归零经 `ShieldBreakEvent`。
- 吸收层固定两层，顺序即语义：**层 1 护盾（`ShieldBuff`）→ 层 2 维塔临时生命（`VitaeBuff`）**。本作不会再有更多吸收层，故不做成可插拔集合。
- 武器侧钩子：`Weapon.modifyDamageInfo`（普通攻击构建完 `DamageInfo` 后改写来源 / 类型）、`Weapon.ignoresDefenseRoll`、`Weapon.Enchantment.releasesConservedDamage`。
- 分类原则：纯数值修正 → `IDamageModifier`；带控制流 / 副作用且位置敏感的 → 按阶段保留，或为其新增相位。

## 三、applyDamage 内尚未迁移

| 逻辑                                                          | 行号        | 来源            | 类型       | 建议                                            |
| ----------------------------------------------------------- | --------- | ------------- | -------- | --------------------------------------------- |
| src 改写为 `DM100.LightningBolt`                               | 930-936   | 天球仪（武器）       | 来源归一化    | **已迁移 → `Weapon.modifyDamageInfo`**（见 C2，有差异）  |
| 护甲 DR                                                       | 949-959   | —             | 减免框架     | **已迁移 → 原始桶的固定项**（早于本批）                       |
| AntiMagic 平坦减伤                                              | 959-964   | 护甲刻印          | 修改型      | **已迁移 → `AntiMagic.rawAddFlat`**（早于本批）        |
| LifeLink 分摊                                                 | 963-966   | buff          | 控制流      | **已迁移 → `LifeLink.shareDamage`**（保留相位，见 C3）    |
| Sickle 转流血 + 早退                                             | 990-1004  | 武器            | 控制流      | **已删除**（入口 `duelistAbility` 已被注释，不可达）           |
| 伤害类型抗性 / 免疫                                                 | 968-1007  | —             | 减免框架     | **已迁移 → 最终乘算乘区**（见 C2，有差异）                    |
| Paralysis `processDamage`                                   | —         | buff          | 反应（读最终值） | **已迁移 → `Buff.onPreShieldDamage`**（等价）        |
| 护盾吸收 + PROVOKED_ANGER                                       | 1014-1029 | buff / 天赋     | 框架 + 反应  | **已迁移 → `ShieldBreakEvent`**（等价，见 C1）          |
| VitaeBuff 吸收                                                | 1031-1036 | buff          | 修改型（护盾后） | **已迁移 → 吸收层 2**（逻辑在 `VitaeBuff` 内，见 C3）        |
| VITAE_BOOST 平坦 -2                                           | —         | 天赋            | 修改型      | **已迁移 → 并入 `VitaeBuff.absorbDamage`**（等价）       |
| ImmortalShield 免死                                           | 1039-1041 | testmode buff | 控制流      | 保持                                            |
| Grim 低血追伤 + 成就                                              | 1043-1058 | 武器附魔          | 修改型（扣血后） | **本轮保持**（追伤在扣血之后且直接改 HP，需新的「扣血后追伤」钩子）            |
| Kinetic 蓄能                                                  | —         | 武器附魔          | 反应（攻击者侧） | **已迁移**：蓄能订阅 `CharFinalDamageEvent`；释放由 `ConservedDamage` 固定加算（见 C1，有差异） |
| 图标特例（SNIPER / WhiteKingGodSword / Deminion / AntiMagic src） | 1072-1080 | 职业 / 武器       | 表现层      | **已迁移 → `DamageInfo` 图标覆盖 / 伤害类型**（见 C2）      |

## 三·补、已迁移项（2026-10-16）

### 本批之前

- **Paralysis**：删除 `processDamage`，改为实现 `Buff.onPreShieldDamage`；`Char.applyDamage` 的直调改为相位循环。行为等价。
- **护甲 DR、AntiMagic**：前者折入原始桶的固定项（与其它固定加算同桶相消），后者实现 `IDamageModifier.rawAddFlat`。行为等价。
- **Endure（苦痛坚忍）**：减伤由两处硬编码（`EndureTracker.modifyIncomingAttackDamage` 的 build 期 final 乘区 + `Hero.damage` 的管线前分支）统一为 `EndureTracker.finalMultiply`（`IDamageModifier`，抗性后、护盾前）；蓄力改为订阅 `CharUnprocedDamageEvent`（进管线原始伤害）。

#### Endure 已确认的行为差异

1. 非 Char 来源减伤相位由「管线前」移到「最终修正后（护盾前）」，与护甲 / 抗性先后顺序改变。
2. Char 来源的非普通攻击（法杖 / 能力等）此前完全绕过 Endure，现会被减伤。
3. 被 `AttackEvent` 取消或未落地的攻击不再蓄力（旧 build 期蓄力早于取消判定）。
4. 蓄力基准统一为进管线原始伤害；对 Hero 目标因 `Hero.damage` 预处理可能略有差异。
5. 减伤倍率公式与 `enduring` 判定不变。

### C1（恒动 / 受衅怒火 / 维塔加成 / Sickle 死代码）

- **VITAE_BOOST 平坦 -2**：由 `Char.applyDamage` 的两段判断（`VitaeBuff` 存在 + 天赋 4 点 + 非饥愤）移入 `VitaeBuff.absorbDamage(dmg, info)`；vitae 清空即 detach 的分支直接返回，与旧实现"buff 还在才减伤"一致。行为等价。
- **Kinetic 蓄能**：结算改为订阅 `CharFinalDamageEvent`，条件（`HP < 0`、目标为敌方阵营、来源为 `Char`）与旧实现一致。
- **Kinetic 释放**：储存伤害的加算由 `Kinetic.ConservedDamage.modifyOutgoingAttackDamage` 在构建期以固定加算注入（原为 `OrdinaryAttackDamage.foldPostProcessing` 的最终加算），释放与消耗绑定在同一次攻击上；`Unstable` 不再重复结算已储存的恒动伤害。
- **PROVOKED_ANGER**：新增 `ShieldBreakEvent`（护盾由正值归零时在护盾相位内发布），`Talent` 改为静态订阅；保留"护盾全额吸收（dealt = 0）也触发"的语义。行为等价。
- **Sickle**：删除 `Char.applyDamage` 中不可达的 `HarvestBleedTracker` 分支（唯一入口 `MeleeWeapon.execute` 里的 `duelistAbility` 调用已被注释）。

#### C1 已确认的行为差异

1. 储存伤害的加算相位由「最终加算」变为「基础值加算」，会吃暴击 / 直接乘区 / 最终乘区，**释放出来的伤害变高**（平衡改动）。
2. 释放时机提前到命中判定（构建 `DamageInfo`）时：被 `AttackEvent` 取消的攻击会消耗掉储存量（旧实现不消耗）。仅影响反弹类（如 Rlyeh）等极少数路径。
3. 只有「会把恒动伤害释放出去的武器」（附魔声明 `releasesConservedDamage()`，即 Kinetic / Unstable）才会释放，因此矮人王「30 点恒动」在非恒动武器下仍只作为挂着的 buff（与旧实现一致）。
4. `Unstable` 命中时不再自行消耗储存量（改由 `ConservedDamage` 自行消耗），因此现在的行为是：附魔为 `Unstable` 时「首次蓄能」依旧不会发生（与旧实现一致），储存量一旦存在则会被下一次普通攻击释放。
5. 蓄能结算由「`onDamageTaken` 之后」提前到 `CharFinalDamageEvent` 发布时（同一次结算内），事件订阅者与受击钩子之间不再有 Kinetic 结算介入，实际无影响。

### C2（抗性乘区 / 天球仪来源 / 伤害图标）

- **伤害类型抗性**：抗性系数并入最终乘算乘区（`Char.applyFinalDamageModifiers` 增加 `resistanceMultiplier` 参数）；免疫仍是控制流，保留独立前置判定（伤害归零 + 抑制跳字），`DamageResult.resistanceBlocked` 计算式不变。
- **天球仪来源归一化**：新增 `Weapon.modifyDamageInfo` 钩子（`OrdinaryAttackDamage.build` 末尾调用），`CelestialSphere` 覆写为把来源改写成 `DM100.LightningBolt`；另新增 `Weapon.ignoresDefenseRoll` 承接原「天球仪无视防御掷骰」特判。`Char` 不再识别武器 / 雕像类型。
- **伤害图标**：`DamageInfo` 新增图标覆盖字段，推导顺序为「显式覆盖 > 抗魔法来源（`AntiMagic.isMagicSource`）> 暴击 / 伤害类型」；`Char` 不再读 SNIPER / WhiteKingGodSword / Deminion / AntiMagic 的具体类。狙击手的无视护甲跳字由 `PHYSICAL_NO_ARMOR` 类型自然表达（判定条件与 `OrdinaryAttackDamage.ignoresArmor` 完全一致）；`OnlyOneEyeAttack` 与 Deminion 的跳字改由来源在构建 `DamageInfo` 时显式设置。

#### C2 已确认的行为差异

1. 抗性作为乘算项并入最终乘区后，`ArcaneArmor` 的固定减伤相位由「抗性后加算」变为「乘算前加算」：旧 `dmg * 抗性 - flat`，新 `(dmg - flat) * 抗性`；取整由两次 `round` 变为一次。全仓只有 `ArcaneArmor` 使用 `finalAddFlat`，`finalAddPercent` / `rawAddPercent` 目前无实现者。
2. 天球仪来源归一化只作用于**走普通攻击构建**的伤害；旧实现对任何「以天球仪为攻击武器」的来源（含自带 `DamageInfo` 的能力伤害）都会改写。源码里 `src instanceof CelestialSphere` 一支已无调用方，随特判一并移除。
3. 伤害图标改为按 `DamageInfo` 派生：`AntiMagic.RESISTS` 命中的来源类现在在 `DamageInfo` 内判定（同一份名单，新增 `AntiMagic.isMagicSource`），判定范围与优先级与旧 `Char` 内联代码一致。

### C3（LifeLink 分摊 / 吸收层）

- **LifeLink 分摊**：分摊相位保留在管线中，份额计算与伙伴承伤封装为 `LifeLink.shareDamage(victim, dmg, type)`，`Char` 只调一句。行为等价。
- **维塔临时生命**：确认为**吸收层 2**，固定排在护盾（层 1）之后、扣血之前；吸收与 VITAE_BOOST 的固定减伤都在 `VitaeBuff` 内结算，`Char` 只做分派。按「不会再有更多吸收层」的结论，不引入可插拔接口。
- **Grim**：本轮保持。

## 四、hit() / attack() / 其它方法尚未迁移

> 行号未刷新（沿迁移前记录）。

| 逻辑                                                    | 行号                | 来源        | 建议                             |
| ----------------------------------------------------- | ----------------- | --------- | ------------------------------ |
| 召唤物击杀成就（EarthGuardian / MirrorImage / PrismaticImage） | 474-479           | 召唤物       | 可迁 `AttackHitEvent`            |
| `RingOfAccuracy` / `RingOfEvasion`（仅 Mob 分支）          | 528 / 533         | 戒指        | 后续实现AttributeModifier 专门实现这种功能 |
| `FatedDraw` 取最大命中 / 伤害                                | 584 / 620 / 654   | 月华能力      | 通过事件实现该效果                      |
| `RingOfBenediction` 命中 / 闪避 / 法强                      | 594-599 / 829-831 | 戒指        | 后续实现AttributeModifier 专门实现这种功能 |
| `RingOfConcentration` 暴击率 / 暴伤                        | 125 / 129         | 戒指        | 后续实现AttributeModifier 专门实现这种功能 |
| `canInteract` ALLY_WARP                               | 208 / 233         | 天赋        | 保持                             |
| `defenseVerb` FREERUNNER                              | 263               | 职业        | 保持                             |
| `attackProc` 内 LightKing 出伤                           | 713-745           | 物品        | 可并入出伤修正链                       |
| `resistanceTo` IRON_MUSCLE                            | 1554-1556         | 天赋        | 保持                             |
| `resist` RingOfElements                               | 1568              | 戒指        | 后续实现AttributeModifier 专门实现这种功能 |
| `die` 清理各类 Tracker                                    | 1304 起            | buff / 天赋 | 保持（或 `onOwnerDeath` 钩子）        |

## 五、Hero.act() 尚未迁移

> 行号未刷新（沿迁移前记录）。

| 逻辑 | 行号 | 来源 | 建议 |
| --- | --- | --- | --- |
| Radish.GlobalCritChance | 1231-1234 | 饰品 | 保持，或 buff 化 |
| Frog.PoulWater 冷却 | 1239-1248 | 物品 | 保持，或 buff 化 |
| SmallWoodenCross 一次性赐福 | 1252-1259 | 物品 | 保持 |
| HIDE_IN_CROWD | 1262-1270 | 天赋 | 保持 |

## 六、其它可提升点（非迁移项）

1. **管线遍历次数**：`applyRaw/Final`、护盾、Vitae、`onDamageTaken` 各自遍历 `buffs()`；可合并为一次遍历、按相位分派。
2. **applyDamage 过长**：已在方法头写明阶段顺序，并把分摊 / 吸收层拆成独立调用；护甲、抗性段落仍可继续外提。
3. ~~**天球仪归一化特判**~~：已由 `Weapon.modifyDamageInfo` 承接。
4. ~~**表现层与数据混合**~~：图标改由 `DamageInfo` 的图标字段 / 伤害类型承载。
5. **纯查询式数值计算**（戒指 / 天赋的乘子）散落在 `Char`，buff 化后可进一步解耦。
6. **`DamageResult` 构造参数偏多**，可考虑对象化 / Builder，减少调用点噪声。

## 七、分批计划与结果

| 批次 | 内容 | 风险 | 结果 |
| --- | --- | --- | --- |
| **C1** | Kinetic、PROVOKED_ANGER → 事件；VITAE_BOOST 并入 `VitaeBuff`；Sickle 死代码 | 低 | 完成（差异见「三·补」C1） |
| **C2** | 天球仪来源归一化；抗性折入最终乘区；图标特例 | 中 | 完成（差异见「三·补」C2） |
| **C3** | LifeLink 分摊封装；维塔临时生命确认为吸收层 2；Grim 保持 | 中 | 完成 |
| **C4**（新） | `Hero.act` 两项 buff 化（原计划排在 C3，属「五」，未在本轮范围内） | 中 | 未开始 |

每批独立编译（`sh gradlew :core:compileJava`）并独立提交，提交信息注明行为差异。

## 八、验收标准

- [ ] `Char.java` 不再 import 具体的武器 / 护甲 / 戒指 / 附魔类（已消除：天球仪 / 雕像 / 狙击手 / Deminion / AntiMagic / Kinetic / Sickle / MissileWeapon；**仍余**：`Grim`、`Blazing`、`Brimstone`、`LightKing`、`Masamune`、`ScrollOfRetribution` 等，对应「四 / 五」未迁移项）
- [x] 新装备效果只需修改自身类，不再改 `Char.java`（武器侧新增 `modifyDamageInfo` / `ignoresDefenseRoll` / `releasesConservedDamage` 钩子）
- [x] 每批次编译通过，且逐项声明"行为等价"或"已确认的差异"（C1、C2 的差异见「三·补」）

---

*文档版本：0.5*
*更新日期：2026年10月16日*
