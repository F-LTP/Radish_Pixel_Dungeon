# 杂散皮肤与变身机制（Jumble Skin & Shift System）

> 状态：**已实现**。

## 概述

杂散（Jumble）是**所有职业共享**的英雄皮肤变体，基于独立贴图 `jumble.png`，并通过一个特殊的永久 Buff（`JumbleChangeBuff`）驱动**周期性变身**：每隔 180-220 回合，杂散会在 6 组形态间自动切换，同时随机化天赋、并变换已装备的神器/戒指/武器/护甲。

与赌徒/流浪者等"单职业皮肤"不同，杂散被注册到全部 6 个职业的 `skins()` 中，使用同一个皮肤索引。

---

## 1. 贴图与帧布局（`jumble.png`）

贴图尺寸 **336x192**，按 **12x16** 帧格划分，共 **28 列 × 12 行**。

### 分组结构

共 **6 组**贴图，每组占据 **2 行**，与职业无关（变身随机切到任意一组）：

| 组 | 行号（0 基） |
|----|-------------|
| 第 1 组 | 0-1 |
| 第 2 组 | 2-3 |
| 第 3 组 | 4-5 |
| 第 4 组 | 6-7 |
| 第 5 组 | 8-9 |
| 第 6 组 | 10-11 |

每组行首索引 = `行号 × 28`（行内列偏移 0-27）。

### 第一行（基础动画，列 0 起）

| 动画 | 前 4 组（组 0-3） | 第 5 组（组 4） | 第 6 组（组 5） |
|------|------------------|----------------|----------------|
| 站立 idle | 0-1 | 0-1 | 0-1 |
| 行走 run | 2-7 | 2-7 | 2-7 |
| 死亡 die | 8-11 | 8-11 | 8-11 |
| 攻击 attack | 12-16 | 12-20 | 12-21 |
| 使用 operate | 17-18 | 21-22 | 22-23 |
| 阅读 read | 19-21 | 23-25 | 24-26 |

> 第 5/6 组攻击帧更多，导致 使用/阅读 帧号整体后移（由 `JumbleSprite` 内根据 `group` 动态计算）。

### 第二行（消失/出现动画）

- 列 0-9：**消失（进入隐身 / 变身）**动画
- 列 10-19：**出现（退出隐身 / 变身完成）**动画

---

## 2. 精灵类 `JumbleSprite`

继承 `HeroSprite`，与赌徒/流浪者一样采用自足怪物贴图模板，通过覆盖 `updateArmor()` 定义全部动画。

关键方法：

```java
public void setGroup(int g);              // 切换到指定组（0-5），重建动画并回待机
public int group();                       // 当前组索引

public void playChange(Callback cb);      // 播放当前组的"消失"动画，完成回调
public void playAppear(int newGroup, Callback cb); // 切到新组并播放"出现"动画，完成回调
```

### 倒计时 Buff 的附加

`updateArmor()` 末尾会调用：

```java
if (ch == Dungeon.hero) {
    JumbleChangeBuff.resetCountdownIfMissing();
}
```

- 由于 `HeroSprite` 构造时 `link(hero)` 设置 `ch`，而 `GameScene` 在 `place()` 后会再次调用 `updateArmor()`，此时 `ch == Dungeon.hero` 成立，Buff 被可靠附加（`Buff.affect` 幂等，不重复）。

---

## 3. 变身机制 `JumbleChangeBuff`

位于 `actors/buffs/JumbleChangeBuff.java`，继承 `Buff`。

### Buff 属性

| 属性 | 实现 |
|------|------|
| 类型 | `buffType.POSITIVE`（正面） |
| 永久 | `act()` 中 `spend(TICK)` 每回合保持激活 |
| 不可驱散 | 重写 `detach()` 为 **no-op**（净化药水只清 NEGATIVE，天然免疫；任何显式移除也无效） |
| 持久化 | 存档 `turnsRemaining` / `changing` |

### 触发流程

`act()` 每回合递减 `turnsRemaining`，归零时：

1. `hero.busy()` —— 阻塞玩家行动（`Hero.act()` 在 `!ready` 时不行动），**不消耗回合**。
2. `sprite.playChange(cb)` —— 播放**当前组**的消失（变身）动画。
3. 动画完成回调内依次：
   - `doTalentMetamorph(hero)` —— 随机化天赋；
   - `doEquipmentTransmute(hero)` —— 变换装备；
   - `Random.Int(6)` 随机选 1 个目标组，`sprite.playAppear(newGroup, cb)` 切换到新组并播放出现动画。
4. 出现动画完成回调 → `finishChange()`：
   - `changing = false`、`resetCountdown()`（重置 180-220 倒计时）；
   - `hero.sprite.idle()` 回到待机；
   - `hero.ready()` 恢复行动。

> 守卫：`changing` 标志防止动画播放期间重入；变身期间 `act()` 不递减倒计时。

### 天赋随机化（`doTalentMetamorph`）

- 遍历 `hero.talents` 每个 tier，对**每个有投入点数**的天赋：
  - 从全部 6 职业的天赋池（`Talent.initClassTalents` 对每个 `HeroClass` 生成）中，随机挑一个**不同**天赋替换；
  - 保留原有点数；
  - 写入 `hero.metamorphedTalents`（复用蜕变密卷的 a→b→c 链简化逻辑，与 `TalentButton` 的 `METAMORPH_REPLACE` 一致）。
- 已作为替换目标的天赋会从池中排除，避免重复。

### 装备变换（`doEquipmentTransmute`）

| 槽位 | 方式 | 说明 |
|------|------|------|
| 神器 | `transmuteArtifact` | 从 `Generator.Category.ARTIFACT.classes` 全池随机，**忽略唯一性 probs，允许重复**（满足"变身时可重复神器"）；保留诅咒/等级 |
| 戒指 | `ScrollOfTransmutation.changeItem` | 复用原版嬗变卷轴逻辑 |
| 武器 | `ScrollOfTransmutation.changeItem` | 复用原版嬗变卷轴逻辑 |
| 护甲 | `transmuteArmor` | **单独实现**（原版 `changeItem` 不处理护甲）：按同 tier 随机，保留强化等级/诅咒 |

统一由 `replaceEquipped` 处理：卸下旧物（清诅咒）→ 装备/收集新物 → 恢复快捷栏。

---

## 4. 皮肤注册（`JumbleSkin`）

- 位于 `actors/hero/definition/skins/JumbleSkin.java`，继承 `SkinDefinition`。
- 皮肤名 `"JUMBLE"`，`skinIndex()` 返回 `HeroClasses.JUMBLE`（**6**）。
- `customSprite() = true`，`asset() = Assets.Sprites.JUMBLE`，帧格 12x16。
- 注册方式：在 **全部 6 个职业**的 `HeroDefinition.skins()` 中加入 `new JumbleSkin(this)`：
  - 战士（+流浪者）、法师、盗贼（+赌徒）、猎人、牧师、月华（+圆球）均含杂散。

---

## 5. 关键类 / 文件

| 类 / 文件 | 作用 |
|-----------|------|
| `sprites/JumbleSprite.java` | 杂散精灵（6 组贴图 + 变身/出现动画方法） |
| `actors/buffs/JumbleChangeBuff.java` | 变身倒计时 Buff（触发、阻塞、天赋/装备替换） |
| `actors/hero/definition/skins/JumbleSkin.java` | 全职业共享皮肤定义 |
| `assets/sprites/RadishSnDSprite/jumble.png` | 6 组 × 2 行 贴图（12x16 帧格） |
| `assets/messages/actors/actors[ _zh].properties` | 皮肤与 Buff 文字键 |

### 消息键

```properties
# 皮肤
actors.hero.definition.skins.jumbleskin.name/desc/desc_short/unlock
# Buff
actors.buffs.jumblechangebuff.name/desc/countdown
```

---

## 6. 设计约定与坑

- **Buff 不可移除**：`detach()` 覆盖为 no-op，务必在类内自行管理状态，任何外部移除都不会生效。
- **阻塞不消耗回合**：用 `hero.busy()` + 动画回调驱动，期间 `spend(TICK)` 保持 Buff 激活但不变身逻辑重入。
- **护甲变换**：原版 `ScrollOfTransmutation.changeItem()` 不处理 `Armor`，需单独实现按 tier 随机。
- **神器重复**：直接访问 `Generator.Category.ARTIFACT.classes` 全池随机，绕过 `probs` 唯一性，满足杂散"变身时可重复神器"的需求。
- **线程安全**：变身逻辑在动画回调（渲染线程）中执行，与项目内其它变身/卷轴实现一致，属可接受模式；务必保证 `hero` 存活判断后再 `ready()`。
