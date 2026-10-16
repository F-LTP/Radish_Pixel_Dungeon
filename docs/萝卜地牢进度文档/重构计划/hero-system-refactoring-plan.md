# 英雄系统重构方案

## 背景

当前项目中 `HeroClass.java`、`HeroSubClass.java`、`Talent.java` 都采用**枚举+switch**的模式，存在以下问题：

1. **枚举膨胀** - Talent枚举有100+天赋全部堆在一个文件里
2. **Switch地狱** - `initClassTalents`、`initSubclassTalents`、`initHero`等方法都有大量switch
3. **高耦合** - 添加新职业需要修改多个核心文件
4. **难以维护** - 天赋ID数字无规律，逻辑分散

## 重要说明

**萝卜地牢游戏进程较短，不需要保证存档向下兼容。**

这意味着：
- 可以彻底重构，不保留旧的枚举结构
- 可以使用更现代的设计模式
- 代码美观性优先于兼容性

## 目标

将所有职业数据从枚举中**完全提取到独立的定义类**，实现**零switch**的优雅架构。

---

## 新架构目录结构

```
actors/hero/
├── Hero.java                   # 英雄类（保持不变）
├── HeroClass.java              # 简化类/接口（替代枚举）
├── HeroSubClass.java           # 简化类/接口（替代枚举）
├── Talent.java                 # 简化类/接口（替代枚举）
├── definition/                 # 职业定义模块
│   ├── HeroDefinition.java     # 职业定义基类
│   ├── SubClassDefinition.java # 子职业定义基类
│   ├── TalentDefinition.java   # 天赋定义类（替代枚举）
│   ├── ArmorAbilityDef.java    # 护甲技能定义
│   ├── TalentSet.java          # 天赋集合类
│   ├── HeroRegistry.java       # 注册中心
│   └── heroes/                 # 各职业定义实现
│       ├── Warrior.java
│       ├── Mage.java
│       ├── Rogue.java
│       ├── Huntress.java
│       ├── Rector.java
│       └── Moonlight.java      # 月华完整定义
│       └── subclasses/         # 子职业定义
│           ├── Berserker.java
│           ├── Gladiator.java
│           ├── ...
│           ├── LittleKnight.java
│           ├── DiceMage.java
│           └── JutteChampion.java
│   └── talents/                # 天赋定义（按职业分类）
│       ├── warrior/
│       ├── mage/
│       ├── rogue/
│       ├── huntress/
│       ├── rector/
│       └── moonlight/          # 月华天赋
│           ├── HuntingIntuition.java
│           ├── SharpeningEdge.java
│           ├── WeaponMastery.java
│           ├── ...
```

---

## 核心接口设计

### HeroClass.java - 职业类（替代枚举）

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

/**
 * 职业类 - 通过注册中心获取具体定义
 * 不使用枚举，直接作为访问入口
 */
public class HeroClass {

    // 预定义职业实例（替代枚举常量）
    public static final HeroClass WARRIOR   = new HeroClass("WARRIOR");
    public static final HeroClass MAGE      = new HeroClass("MAGE");
    public static final HeroClass ROGUE     = new HeroClass("ROGUE");
    public static final HeroClass HUNTRESS  = new HeroClass("HUNTRESS");
    public static final HeroClass RECTOR    = new HeroClass("RECTOR");
    public static final HeroClass MOONLIGHT = new HeroClass("MOONLIGHT");

    // 所有职业集合
    public static final Collection<HeroClass> ALL = Arrays.asList(
        WARRIOR, MAGE, ROGUE, HUNTRESS, RECTOR, MOONLIGHT
    );

    private final String name;
    private HeroDefinition definition;

    private HeroClass(String name) { this.name = name; }

    public void bindDefinition(HeroDefinition def) { this.definition = def; }
    public HeroDefinition definition() { return definition; }
    public String name() { return name; }

    // 代理方法
    public String spritesheet() { return definition.spritesheet(); }
    public String splashArt() { return definition.splashArt(); }
    public boolean isUnlocked() { return definition.isUnlocked(); }
    public HeroSubClass[] subClasses() { return definition.subClasses(); }
    public ArmorAbility[] armorAbilities() { return definition.armorAbilities(); }
    public void initHero(Hero hero) { definition.initHero(hero); }
    public TalentSet talents() { return definition.talents(); }

    // 存档序列化
    public String saveName() { return name; }
    public static HeroClass fromSaveName(String name) {
        for (HeroClass cls : ALL) {
            if (cls.name.equals(name)) return cls;
        }
        return WARRIOR;
    }
}
```

### Talent.java - 天赋基类（替代枚举）

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

/**
 * 天赋基类 - 所有天赋继承此类
 * 每个天赋是独立的类，而非枚举值
 */
public abstract class Talent {

    protected final String name;
    protected final int iconId;
    protected final int maxPoints;
    protected final int tier;

    protected Talent(String name, int iconId, int maxPoints, int tier) {
        this.name = name;
        this.iconId = iconId;
        this.maxPoints = maxPoints;
        this.tier = tier;
    }

    public String name() { return name; }
    public int iconId() { return iconId; }
    public int maxPoints() { return maxPoints; }
    public int tier() { return tier; }

    // 天赋效果回调
    public void onUpgrade(Hero hero, int newLevel) { }
    public void onDowngrade(Hero hero, int newLevel) { }
    public boolean canUnlock(Hero hero) { return true; }

    // 存档序列化
    public String saveName() { return this.getClass().getSimpleName(); }
}
```

### HeroDefinition.java - 职业定义基类

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

/**
 * 职业定义基类 - 所有职业继承此类
 */
public abstract class HeroDefinition {

    // 必须实现
    public abstract HeroClass heroClass();
    public abstract String spritesheet();
    public abstract String splashArt();
    public abstract HeroSubClass[] subClasses();
    public abstract ArmorAbility[] armorAbilities();
    public abstract TalentSet talents();
    public abstract void initHero(Hero hero);

    // 可选重写
    public boolean isUnlocked() { return true; }
    public void adjustHeroStats(Hero hero) { }

    // 注册
    public void register() {
        heroClass().bindDefinition(this);
    }
}
```

### TalentSet.java - 天赋集合类

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

/**
 * 天赋集合 - 管理各层级天赋
 */
public class TalentSet {

    private Talent[] tier1, tier2, tier3, tier4;
    private Map<HeroSubClass, Talent[]> subclassT3 = new HashMap<>();
    private Map<HeroSubClass, Talent[]> subclassT4 = new HashMap<>();
    private Map<ArmorAbility, Talent[]> abilityTalents = new HashMap<>();

    // Getter/Setter
    public Talent[] getTier1() { return tier1; }
    public void setTier1(Talent[] t) { tier1 = t; }

    public Talent[] getTier2() { return tier2; }
    public void setTier2(Talent[] t) { tier2 = t; }

    public Talent[] getTier3() { return tier3; }
    public void setTier3(Talent[] t) { tier3 = t; }

    public Talent[] getTier4() { return tier4; }
    public void setTier4(Talent[] t) { tier4 = t; }

    public void setSubclassT3(HeroSubClass sub, Talent[] t) { subclassT3.put(sub, t); }
    public void setAbilityTalents(ArmorAbility abil, Talent[] t) { abilityTalents.put(abil, t); }
}
```

---

## 月华职业定义示例

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

/**
 * 月华职业定义
 */
public class Moonlight extends HeroDefinition {

    @Override
    public HeroClass heroClass() { return HeroClass.MOONLIGHT; }

    @Override
    public String spritesheet() { return Assets.Sprites.MOONLIGHT; }

    @Override
    public String splashArt() { return Assets.Splashes.MOONLIGHT; }

    @Override
    public HeroSubClass[] subClasses() {
        return new HeroSubClass[]{
            HeroSubClass.LITTLE_KNIGHT,
            HeroSubClass.DICE_MAGE,
            HeroSubClass.JUTTE_CHAMPION
        };
    }

    @Override
    public ArmorAbility[] armorAbilities() {
        return new ArmorAbility[]{
            new FatedDraw(),      // 注定一抽
            new ToyBag(),         // 玩具背包
            new FireKingAvatar()  // 薪王化身
        };
    }

    @Override
    public TalentSet talents() {
        TalentSet set = new TalentSet();

        // T1
        set.setTier1(new Talent[]{
            HuntingIntuition.INSTANCE,
            SharpeningEdge.INSTANCE,
            WeaponMastery.INSTANCE,
            WarTrample.INSTANCE
        });

        // T2
        set.setTier2(new Talent[]{
            MealUtilization.INSTANCE,
            StrongBody.INSTANCE,
            HolySpring.INSTANCE,
            TripleInsurance.INSTANCE,
            CatapultStart.INSTANCE
        });

        // T3 通用
        set.setTier3(new Talent[]{
            SwordShieldKnight.INSTANCE,
            WheelchairCrash.INSTANCE
        });

        // 子职业T3
        set.setSubclassT3(HeroSubClass.LITTLE_KNIGHT, new Talent[]{
            WontLose.INSTANCE, WetEnchant.INSTANCE, LeftBowRapid.INSTANCE
        });

        return set;
    }

    @Override
    public void initHero(Hero hero) {
        // 初始武器：所有角色初始武器
        (hero.belongings.weapon = new WornShortsword()).identify();
        new Dagger().identify().collect();
        new Gloves().identify().collect();
        new MagesStaff().identify().collect();

        // 轮椅神器
        Wheelchair wheelchair = new Wheelchair();
        (hero.belongings.artifact = wheelchair).identify();
        wheelchair.activate(hero);

        // 初始物品已鉴定
        new PotionOfExperience().identify();
        new ScrollOfUpgrade().identify();
        new ScrollOfIdentify().identify();

        // 生命值调整
        hero.HT -= 2;
        hero.HP = hero.HT;
    }
}
```

---

## 天赋类示例

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.talents.moonlight;

/**
 * 猎杀直觉 - T1天赋
 * 月华每获得20/15次经验，就获得一块神秘的肉
 */
public class HuntingIntuition extends Talent {

    public static final HuntingIntuition INSTANCE = new HuntingIntuition();

    private HuntingIntuition() {
        super("HUNTING_INTUITION", 440, 2, 1);
    }

    @Override
    public void onUpgrade(Hero hero, int newLevel) {
        hero.resetExpCounter(this);
    }

    public void onExpGain(Hero hero) {
        int threshold = hero.pointsInTalent(this) == 2 ? 15 : 20;
        // 检查并发放奖励...
    }
}
```

---

## 注册中心

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

/**
 * 职业注册中心 - 游戏启动时调用initAll()
 */
public class HeroRegistry {

    private static Map<HeroClass, HeroDefinition> heroes = new HashMap<>();

    public static void register(HeroDefinition def) {
        heroes.put(def.heroClass(), def);
        def.register();
    }

    public static HeroDefinition get(HeroClass cls) {
        return heroes.get(cls);
    }

    public static void initAll() {
        register(new Warrior());
        register(new Mage());
        register(new Rogue());
        register(new Huntress());
        register(new Rector());
        register(new Moonlight());
    }
}
```

---

## 实施步骤

### 第一阶段：创建基础框架

1. 创建 `definition/` 包
2. 实现 `HeroClass.java` 新结构
3. 实现 `Talent.java` 新结构
4. 实现 `TalentRegistry.java` 注册中心
5. 实现 `TalentSet.java` 天赋集合类

### 第二阶段：创建定义基类

1. 实现 `HeroDefinition.java` 抽象基类
2. 实现 `SubClassDefinition.java` 抽象基类

### 第三阶段：迁移现有职业

按顺序迁移：Warrior → Mage → Rogue → Huntress → Rector → Moonlight

### 第四阶段：全面重构核心类

1. 将 `HeroClass.java` 从枚举改为类
2. 将 `HeroSubClass.java` 从枚举改为类
3. 将 `Talent.java` 从枚举改为基类
4. 更新所有引用点

### 第五阶段：清理与测试

1. 删除所有switch语句
2. 删除旧的枚举残留代码
3. 测试各职业初始化
4. 测试天赋系统
5. 测试护甲技能

---

## 注意事项

### 无需存档兼容

萝卜地牢游戏进程较短，用户不会长期保存存档：
- **可以彻底重构** - 不保留旧枚举结构
- **代码美观优先** - 优先考虑可维护性
- **存档格式可以改变** - 使用简单的字符串序列化

### 游戏启动时调用

```java
// 在Game.java或Dungeon.java初始化阶段调用
HeroRegistry.initAll();
TalentRegistry.initAll();
```

---

## 优势总结

| 方面 | 当前架构 | 重构后 |
|------|---------|--------|
| 添加新职业 | 修改3+个核心文件 | 创建1个定义类 |
| 添加新天赋 | 修改枚举+多个switch | 创建1个天赋类 |
| 天赋管理 | 分散在多个switch | 集中在TalentSet |
| 代码可读性 | 枚举+switch混乱 | 类继承结构清晰 |
| 维护成本 | 高 | 低 |

---

## 文档版本

- 创建日期：2026年4月17日
- 作者：Qwen Code
- 状态：待实施
- 备注：不需要存档向下兼容，代码美观优先