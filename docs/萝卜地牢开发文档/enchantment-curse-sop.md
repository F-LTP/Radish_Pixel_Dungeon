# 创建附魔 / 诅咒 SOP

本文档总结在萝卜地牢中新增武器附魔、武器诅咒、护甲刻印、护甲诅咒的标准流程与注意事项。

---

## 一、武器附魔 / 诅咒

**目录**：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/enchantments/`（正面）、`items/weapon/curses/`（诅咒）

**基类**：`Weapon.Enchantment`

### 1. 新建类

```java
public class Xxx extends Weapon.Enchantment {

    private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing(0xRRGGBB);

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());   // 武器等级
        float power = procChanceMultiplier(attacker);  // 奥术倍率等
        // ... 效果，返回修改后的 damage
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return COLOR;
    }

    @Override
    public boolean curse() {   // 仅诅咒需要
        return true;
    }
}
```

### 2. 数值成长规范

- **等级**：`Math.max(0, weapon.buffedLvl())`
- **附魔强度（奥术戒指）**：`RingOfArcana.enchantPowerMultiplier(owner)`；触发概率用 `procChanceMultiplier(attacker)`
- 推荐形式：`procChance = f(level) * procChanceMultiplier(attacker)`

### 3. 注册（必做）

| 位置 | 操作 |
|---|---|
| `Weapon.Enchantment.common/uncommon/rare` | 正面附魔加入对应稀有度数组 |
| `Weapon.Enchantment.curses` | 诅咒加入数组 |
| `ElementalStrike.effectTypes` | 仅当该附魔参与元素打击效果映射时添加；新增前检查未映射类型的处理路径 |
| `CustomWeapon.enchPrio` | 测试自定义武器用，按测试需求维护 |
| `TestMelee.generateEnchant` | 测试生成器，可选 |

### 4. 本地化

`core/src/main/assets/messages/items/items.properties` 与 `items_zh.properties`：

```
items.weapon.enchantments.xxx.name=xxx %s
items.weapon.enchantments.xxx.desc=...
items.weapon.enchantments.xxx.elestrike_desc=...
```

诅咒使用 `items.weapon.curses.xxx.*`。key = 类全名去掉包名前缀后小写；内部类用 `$`，如 `items.weapon.curses.xxx$buff.name`。

### 5. 高级钩子

- 攻速：改 `Weapon.delayFactor()`
- 命中：改 `Weapon.accuracyFactor()`
- 持续状态：静态内部 `Buff`，实现 `storeInBundle/restoreFromBundle`
- 临时等级 / 力量需求：实现 `ITempLevelCurse`，在 `buffedLvl()` / `STRReq()` 中生效

---

## 二、护甲刻印 / 诅咒

**目录**：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/glyphs/`（正面）、`items/armor/curses/`（诅咒）

**基类**：`Armor.Glyph`

### 1. 新建类

```java
public class Xxx extends Armor.Glyph {

    private static ItemSprite.Glowing COLOR = new ItemSprite.Glowing(0xRRGGBB);

    @Override
    public int proc(Armor armor, Char attacker, Char defender, int damage) {
        int level = Math.max(0, armor.procLvl());       // 含纹章 +1
        float power = procChanceMultiplier(defender);
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return COLOR;
    }

    @Override
    public boolean curse() {   // 仅诅咒需要
        return true;
    }
}
```

### 2. 注册（必做）

| 位置 | 操作 |
|---|---|
| `Armor.Glyph.common/uncommon/rare` | 正面刻印加入数组 |
| `Armor.Glyph.curses` | 诅咒加入数组（自动进图鉴 / 随机池） |

### 3. 本地化

```
items.armor.glyphs.xxx.name=%s of xxx      # 中文：xxx%s
items.armor.glyphs.xxx.desc=...
```

诅咒使用 `items.armor.curses.xxx.*`；内部 Buff 用 `xxx$buffname.*`。

### 4. 高级钩子

- 移动 / 闪避 / 潜行：改 `Armor.speedFactor()` / `evasionFactor()` / `stealthFactor()`
- 持续状态：静态内部 `FlavourBuff`
- 踩踏等事件：静态方法加 `@SubscribeEvent(event = XxxEvent.class)`，注解处理器自动注册
- 燃烧时长等外部交互：改对应类（如 `Burning.reignite()`）
- 临时等级 / 力量需求：实现 `ITempLevelCurse`，注意同时检查护甲刻印与纹章刻印

---

## 三、通用检查清单

- [ ] `curse()` 诅咒返回 `true`，正面附魔不重写
- [ ] `glowing()` 返回颜色（诅咒一般 `0x000000`）
- [ ] 等级 + 奥术倍率都参与成长（本项目规范）
- [ ] 加入对应随机池数组
- [ ] 如需参与 ElementalStrike 效果映射，再核对并更新 `ElementalStrike`
- [ ] 中英文 key / desc 齐全
- [ ] 触发概率用 `procChanceMultiplier`；`MagicImmune` 由 `hasEnchant` / `hasGlyph` 统一处理
- [ ] 按项目配置的 JDK 编译：`./gradlew :core:compileJava --offline`

---

## 四、易踩的坑

1. **`ElementalStrike` 映射**：新增武器附魔时，检查 `effectTypes` 对该类型的处理；只有需要相应效果时才增加映射，不要假定所有未映射类型都会导致崩溃。
2. **`delayFactor` 一次攻击被多次调用**（Strongman 伤害计算），随机 roll 会多次触发；攻击时间以最后一次为准。
3. **`buffedLvl()` 分支多**：`MeleeWeapon` / `Armor` 有 GoldRadish、RiverCrystal、GIFT 等分支，改等级相关逻辑时要逐个覆盖。
4. **内部类消息 key** 用 `$`：`items.armor.curses.swamp$waterlogged.name`。
5. **力量需求修正**放在现有计算之后（`req += curseStrReqMod()`），避免打乱国王之戒等加成。
6. **存档兼容需逐项判断**：新增持久化字段、改动 Bundle key 或旧状态含义时，应检查现有存档读取兼容性；不可由通用规则推断无需迁移。

---

## 五、参考实现

| 类型 | 示例 |
|---|---|
| 武器诅咒（概率 proc） | `items/weapon/curses/Sacrificial.java`、`Annoying.java` |
| 武器诅咒（改攻速 + 等级/奥术成长） | `items/weapon/curses/Temporal.java` + `Weapon.delayFactor()` |
| 武器诅咒（改伤害 + 自伤） | `items/weapon/curses/DoubleEdged.java` |
| 武器 / 护甲诅咒（临时等级 + 力量需求） | `items/curses/ITempLevelCurse`、`CurseTempLevels`、`Heavy` / `Ultralight` |
| 护甲诅咒（速度 + 事件 buff + 燃烧交互） | `items/armor/curses/Swamp.java` |
| 护甲刻印（带内部 Buff） | `items/armor/glyphs/Viscosity.java` |
| 事件订阅 | `items/artifacts/Wheelchair.java`、`items/armor/curses/Swamp.java` |
