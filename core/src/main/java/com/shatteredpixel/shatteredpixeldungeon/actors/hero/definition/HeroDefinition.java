package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.ChallengeBag;
import com.shatteredpixel.shatteredpixeldungeon.custom.dict.DictBook;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.BackpackCleaner;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.CustomPlayer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.CustomWeapon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.AnimationTweaker;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.EnemyAttributeModifier;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.ImmortalShieldAffecter;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.LevelTeleporter;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.MobAttributeViewer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.MobPlacer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.SnakeBiteToggle;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.SnDFunctions;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TalentSetter;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TerrainPlacer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestBag;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TimeReverser;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TrapPlacer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.LazyTest;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.SpawnMisc;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.SpawnWeapon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestArmor;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestArtifact;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestMissile;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestRing;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestTalentOFTerminalBook;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.alive.SeedOfCard;
import com.shatteredpixel.shatteredpixeldungeon.items.alive.StoneOfCard;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MagneticCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Aberforth;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Chibayari;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.DualDuelDaggers;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.InversionBeta;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.LunarCorona;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Showdarker;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Starlight;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Sunless;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Turtleir;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Wastelandew;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CelestialSphere;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EchoplexHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnemyFlag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PneumFistGloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShadowBooks;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.utils.DeviceCompat;

/**
 * 职业定义基类 - 所有职业继承此类，集中声明该职业的静态外观、结构、初始装备与天赋层级。
 */
public abstract class HeroDefinition {

	//职业标识
	public abstract HeroClass heroClass();

	//静态外观
	public abstract String spritesheet();
	public abstract String splashArt();
	public abstract String avatarSkin();

	//结构
	public abstract HeroSubClass[] subClasses();
	public abstract ArmorAbility[] armorAbilities();

	//天赋层级（职业通用 T1/T2/T3/T4）
	public abstract TalentSet talents();

	/**
	 * 该职业/皮肤变体的显示名称。基础职业默认返回职业标题，皮肤可覆盖以拥有独立名称。
	 */
	public String heroName() { return Messages.get(HeroClass.class, heroClass().name()); }

	/**
	 * 该职业/皮肤变体的长描述。默认读取职业描述键，皮肤可覆盖以拥有独立描述。
	 */
	public String heroDesc() {
		return Messages.get(HeroClass.class, heroClass().name() + "_desc");
	}

	/**
	 * 该职业/皮肤变体的短描述。默认读取职业短描述键，皮肤可覆盖。
	 */
	public String heroShortDesc() {
		return Messages.get(HeroClass.class, heroClass().name() + "_desc_short");
	}

	/**
	 * 该职业/皮肤变体的解锁提示。默认拼接职业解锁键，皮肤可覆盖。
	 */
	public String heroUnlockMsg() {
		return heroShortDesc() + "\n\n" + Messages.get(HeroClass.class, heroClass().name() + "_unlock");
	}

	/**
	 * 该职业可用的皮肤变体列表（不含基础职业本身）。默认无皮肤。
	 * 若职业有皮肤，在此返回全部 {@link SkinDefinition}。
	 */
	public SkinDefinition[] skins() { return new SkinDefinition[0]; }

	// ---- 精灵渲染参数（选人界面预览 & 游戏内精灵）----

	/** 精灵类。基础职业默认 {@link HeroSprite}，独立皮肤可覆盖。 */
	public Class<? extends HeroSprite> spriteClass() { return HeroSprite.class; }

	/** 是否为独立贴图皮肤（true=专属 asset，false=职业 spritesheet）。 */
	public boolean customSprite() { return false; }

	/** 独立贴图 asset（仅 customSprite 时有效）。 */
	public String asset() { return null; }

	public int frameW() { return 12; }
	public int frameH() { return 15; }

	public float scale() { return 3f; }

	public boolean showGrass() { return true; }

	/** 预览待机动画帧序（TextureFilm 索引）。默认复用标准英雄小人待机帧。 */
	public int[] idleFrames() { return HeroSprite.IDLE_FRAMES; }

	/**
	 * 英雄初始化。包含所有职业共用的初始化流程，并在 {@link #initClassLoadout(Hero)}
	 * 处插入各职业专属的初始装备/物品。
	 */
	public void initHero( Hero hero ) {

		//[TEST MODE]
		if (Dungeon.isChallenged(Challenges.TEST_MODE))
			doChallengeSpawn();

		hero.rectorDeadKngithDeadMode = false;

		new StoneOfCard().collect();
		new SeedOfCard().collect();

		hero.heroClass = heroClass();
		hero.skin = hero.heroClass.getGlobalSkin();
		Talent.initClassTalents(hero);

		// Snake Bite challenge: start with 11 strength
		if (Dungeon.isChallenged(Challenges.SNAKE_BITE)) {
			hero.STR = Hero.STARTING_STR + 1;
		}

		Item i = new ClothArmor().identify();
		if (!Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		i = new Food();
		if (!Challenges.isItemBlocked(i)) i.collect();

		// 部分职业（牧师、月华）不初始携带绒布包，商人会售卖
		if (grantsVelvetPouch()) {
			new VelvetPouch().collect();
			Dungeon.LimitedDrops.VELVET_POUCH.drop();
		}

		Waterskin waterskin = new Waterskin();
		waterskin.collect();

		new ScrollOfIdentify().identify();

		//各职业专属初始装备/物品
		initClassLoadout( hero );

		if (SPDSettings.quickslotWaterskin()) {
			for (int s = 0; s < QuickSlot.SIZE; s++) {
				if (Dungeon.quickslot.getItem(s) == null) {
					Dungeon.quickslot.setSlot(s, waterskin);
					break;
				}
			}
		}
	}

	/**
	 * 各职业专属的初始装备/物品。子类重写。
	 * 注意：本方法在共用流程末尾、水袋快捷栏填充之前调用。
	 */
	protected void initClassLoadout(Hero hero) { }

	/**
	 * 初始是否携带绒布包。默认 true；牧师、月华为 false。
	 */
	protected boolean grantsVelvetPouch(){ return true; }

	//可选重写
	/**
	 * 职业解锁条件。各职业可重写以提供各自的解锁规则
	 * （例如 {@code Badges.isUnlocked(Badges.Badge.UNLOCK_MAGE)}）。
	 * 调试构建下恒解锁。
	 */
	public boolean isUnlocked() {
		if (DeviceCompat.isDebug()) return true;
		return true;
	}

	/**
	 * 职业专属的基础生命成长。返回 null 表示使用默认成长。
	 * 返回值为 [初始HP, 每级成长]。
	 */
	public int[] baseHPGrowth() { return null; }

	/**
	 * 潜行搜索半径（盗贼为 2）。
	 */
	public int sneakRadius() { return 1; }

	/**
	 * 获得经验时触发的职业钩子。
	 */
	public void onExpGain(Hero hero, int exp) { }

	//注册到 HeroClass
	public final void register() {
		heroClass().bindDefinition(this);
	}

	private static void doChallengeSpawn() {
		new ChallengeBag().collect();

		//TODO
		new MagneticCrown().identify().collect();

		new DictBook().collect();
		new EchoplexHammer().collect();
		new EnemyFlag().collect();
		new ShadowBooks().collect();
		new CelestialSphere().collect();

		new Aberforth().identify().collect();
		new DualDuelDaggers().identify().collect();
		new Chibayari().identify().collect();
		new Wastelandew().identify().collect();
		new Turtleir().identify().collect();
		new Showdarker().identify().collect();
		new Sunless().identify().collect();
		new Starlight().identify().collect();
		new LunarCorona().identify().collect();

		new InversionBeta().identify().collect();

		new SpawnMisc().collect();
		new MobPlacer().collect();

		new PneumFistGloves().collect();

		CustomWeapon customWeapon = new CustomWeapon();
		customWeapon.adjustStatus();
		customWeapon.identify().collect();

		new CustomPlayer().collect();

		new SnakeBiteToggle().collect();

		new TalentSetter().collect();

		TestBag testBag = new TestBag();
		testBag.collect();
		TestBag testbag2 = new TestBag();
		testbag2.collect();

		new TrapPlacer().collect();

		new TimeReverser().collect();

		new ImmortalShieldAffecter().collect();

		new BackpackCleaner().collect();

		new AnimationTweaker().collect( testbag2 );

		new LevelTeleporter().collect();

		new LazyTest().collect();

		new TestArmor().collect();
		new TestArtifact().collect();
		new SpawnWeapon().collect();
		new TestMissile().collect();
		new TestRing().collect();
		new SnDFunctions().collect();
		//new TestPotion().collect();

		new TestTalentOFTerminalBook().collect();

		new ScrollHolder().collect();
		Dungeon.LimitedDrops.SCROLL_HOLDER.drop();

		new PotionBandolier().collect();
		Dungeon.LimitedDrops.POTION_BANDOLIER.drop();

		if (!Dungeon.LimitedDrops.VELVET_POUCH.dropped()) {
			new VelvetPouch().collect();
			Dungeon.LimitedDrops.VELVET_POUCH.drop();
		}

		new MagicalHolster().collect();
		Dungeon.LimitedDrops.MAGICAL_HOLSTER.drop();

		//	new WandOfReflectDisintegration().identify().collect();

		new EnemyAttributeModifier().collect();

		new MobAttributeViewer().collect();

		new TerrainPlacer().collect();

	}
}
