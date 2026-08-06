package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlessAWP;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeToyEffects;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight.SharpeningEdgeTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.ToyBackpack;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.AntiEntropy;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Bulk;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Displacement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Metabolism;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Multiplicity;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Overgrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Stench;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Affection;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Entanglement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Repulsion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.SkyWalker;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Muramasa;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.toys.ClumsyShoes;
import com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToy;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldRadish;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RiverCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CircleSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class Armor extends EquipableItem {

	public static final String AC_SHARPENING_EDGE = "SHARPENING_EDGE";
	public static final String AC_ATTACH = "ATTACH";
	public static final String AC_TOY = "TOY";
	protected Buff buff;
	protected static final String AC_DETACH       = "DETACH";

	// 玩具背包相关字段
	public int toyCharge = 0; // 玩具背包充能
	protected ArrayList<ItemArmorAttachable> attachedToys = new ArrayList<>();

	private static final Class<? extends ItemArmorAttachable>[][] TOY_CLASSES_BY_TIER = new Class[][]{
		{
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Scar.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.IronHeart.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Arrow.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.BarkskinToy.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Cloak.class,
			ClumsyShoes.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Poem.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Mercury.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Tincture.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Polearm.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.Whetstone.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.MagicWand.class,
			com.shatteredpixel.shatteredpixeldungeon.items.toys.ShieldToy.class,
		},
		{TieredToy.Buckler.class, TieredToy.TwinDaggers.class, TieredToy.BlessedWater.class, TieredToy.Spinach.class, TieredToy.Terrarium.class},
		{TieredToy.Harpoon.class, TieredToy.EnchantedShield.class, TieredToy.IronPendant.class, TieredToy.Shortsword.class, TieredToy.BloodChalice.class},
		{TieredToy.CrackedPlate.class, TieredToy.LifeBolt.class, TieredToy.SplittingArrows.class, TieredToy.HissingRing.class, TieredToy.Antivenom.class},
		{TieredToy.Ambrosia.class, TieredToy.Nunchaku.class, TieredToy.Sponge.class, TieredToy.MiniCrossbow.class, TieredToy.Doomblade.class, TieredToy.BagOfHolding.class},
		{TieredToy.BlindingBolt.class, TieredToy.Hourglass.class, TieredToy.GhostShield.class, TieredToy.ToothNecklace.class, TieredToy.Determination.class}
	};

	public enum Augment {
		EVASION (2f , -1f),
		DEFENSE (-2f, 1f),
		NONE	(0f   ,  0f);

		private float evasionFactor;
		private float defenceFactor;

		Augment(float eva, float df){
			evasionFactor = eva;
			defenceFactor = df;
		}

		public int evasionFactor(int level){
			return Math.round((2 + level) * evasionFactor);
		}

		public int defenseFactor(int level){
			return Math.round((2 + level) * defenceFactor);
		}
	}

	public Augment augment = Augment.NONE;

	public Glyph glyph;
	//public boolean curseInfusionBonus = false;

	public boolean glyphHardened = false;

	public boolean masteryPotionBonus = false;

	public int tier;

	private static final int USES_TO_ID = 10;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;

	public Armor( int tier ) {
		this.tier = tier;
	}

	private static final String USES_LEFT_TO_ID = "uses_left_to_id";
	private static final String AVAILABLE_USES  = "available_uses";
	private static final String GLYPH			= "glyph";
	private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
	private static final String MASTERY_POTION_BONUS = "mastery_potion_bonus";
	private static final String AUGMENT			= "augment";
	private static final String TOY_CHARGE      = "toy_charge";
	private static final String ATTACHED_TOYS   = "attached_toys";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( GLYPH, glyph );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( MASTERY_POTION_BONUS, masteryPotionBonus );
		bundle.put( AUGMENT, augment);
		bundle.put( TOY_CHARGE, toyCharge);
		Bundle toyBundle = new Bundle();
		for (int i = 0; i < attachedToys.size(); i++) {
			toyBundle.put("toy_" + i, attachedToys.get(i));
		}
		toyBundle.put("toy_count", attachedToys.size());
		bundle.put(ATTACHED_TOYS, toyBundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		usesLeftToID = bundle.getInt( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getInt( AVAILABLE_USES );
		inscribe((Glyph) bundle.get(GLYPH));
		curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
		masteryPotionBonus = bundle.getBoolean( MASTERY_POTION_BONUS );

		augment = bundle.getEnum(AUGMENT, Augment.class);

		// 恢复玩具背包数据
		if (bundle.contains(TOY_CHARGE)) {
			toyCharge = bundle.getInt(TOY_CHARGE);
		}
		attachedToys = new ArrayList<>();
		if (bundle.contains(ATTACHED_TOYS)) {
			Bundle toyBundle = bundle.getBundle(ATTACHED_TOYS);
			if (toyBundle.contains("toy_count")) {
				int count = toyBundle.getInt("toy_count");
				for (int i = 0; i < count; i++) {
					ItemArmorAttachable toy = (ItemArmorAttachable) toyBundle.get("toy_" + i);
					if (toy != null) {
						attachedToys.add(toy);
						toy.attachToArmor(this);
					}
				}
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
		// armor can be kept in bones between runs, attachments cannot.
		toyCharge = 0;
		attachedToys.clear();
	}

	public boolean readyToIdentify(){
		return !isIdentified() && usesLeftToID <= 0;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);

		// 有附着物时显示卸下选项
		boolean hasAttachments = !attachedToys.isEmpty();
		if (hasAttachments) {
			actions.add(AC_DETACH);
		}

		// 砥砺锋芒天赋：只有月华英雄且有天赋时显示
		if (SharpeningEdgeTalent.canUse(hero, this)) {
			actions.add(AC_SHARPENING_EDGE);
		}

		// 玩具背包护甲技能：只有选择了玩具背包护甲技能时显示
		if (hero.armorAbility instanceof ToyBackpack) {
			actions.add(AC_TOY); // 查看玩具背包
			if (this instanceof ClassArmor
					&& ((ClassArmor)this).charge >= hero.armorAbility.chargeUse(hero)) {
				actions.add(AC_ATTACH); // 生成新玩具
			}
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_SHARPENING_EDGE)) {
			return Messages.get(Armor.class, "ac_" + action);
		}
		if (action.equals(AC_TOY) || action.equals(AC_ATTACH)) {
			return Messages.get(Armor.class, "ac_" + action);
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_SHARPENING_EDGE)) {
			SharpeningEdgeTalent.showTargetSelectionWindow(hero, this);

		} else if (action.equals(AC_DETACH)) {
			// 打开窗口选择要卸下的物品
			GameScene.show(new WndDetachItems(hero, this));

		} else if (action.equals(AC_ATTACH)) {
			if (this instanceof ClassArmor
					&& hero.armorAbility instanceof ToyBackpack
					&& ((ClassArmor)this).charge >= hero.armorAbility.chargeUse(hero)) {
				((ToyBackpack) hero.armorAbility).requestGeneration((ClassArmor) this, hero);
			}

		} else if (action.equals(AC_TOY)) {
			// 查看玩具背包信息
			GameScene.show(new WndToyBackpackInfo(this));
		}
	}

	@Override
	public boolean doEquip( Hero hero ) {

		// func 4 Muramasa mania
		// DoggingDog on 20250419
		if(Dungeon.hero.buff(Muramasa.MuramasaMania.class)!=null && Dungeon.hero!=null){
			GLog.n(Messages.get(Muramasa.MuramasaMania.class,"mania"));
			return false;
		}
		//


		detach(hero.belongings.backpack);

		if (hero.belongings.armor == null || hero.belongings.armor.doUnequip( hero, true, false )) {

			hero.belongings.armor = this;

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(Armor.class, "equip_cursed") );
			}

			((HeroSprite)hero.sprite).updateArmor();
			activate(hero);
			Talent.onItemEquipped(hero, this);
			hero.spendAndNext( time2equip( hero ) );
			return true;

		} else {

			collect( hero.belongings.backpack );
			return false;

		}
	}

	@Override
	public void activate(Char ch) {
		// BrokenSeal 的 WarriorShield 通过 applyEffect() 自动应用
		if (buff != null){
			buff.detach();
			buff = null;
		}
		buff = buff();
		if (buff!=null)
			buff.attachTo( ch );
		if (ch instanceof Hero) {
			for (ItemArmorAttachable toy : attachedToys) {
				toy.applyEffect((Hero)ch);
			}
		}
	}

	public void affixSeal(BrokenSeal seal){
		// 升级传递逻辑：纹章等级传递给护甲，但卸下时会返还
		if (seal.level() > 0){
			//doesn't trigger upgrading logic such as affecting curses/glyphs
			int newLevel = trueLevel()+1;
			level(newLevel);
			Badges.validateItemLevelAquired(this);
		}
		attachToy(seal);
	}

	public BrokenSeal checkSeal(){
		return getToy(BrokenSeal.class);
	}

	// ========== 玩具背包方法 ==========

	/**
	 * 生成随机玩具
	 */
	public ItemArmorAttachable generateRandomToy() {
		int talent = Dungeon.hero == null ? 0 : Dungeon.hero.pointsInTalent(Talent.BETTER_ITEM);
		return generateRandomToy( Random.IntRange(1 + talent, 2 + talent) );
	}

	public ItemArmorAttachable generateRandomToy( int tier ) {
		try {
			if (tier < 1 || tier > TOY_CLASSES_BY_TIER.length) return null;
			Class<? extends ItemArmorAttachable> cls = Random.oneOf(TOY_CLASSES_BY_TIER[tier - 1]);
			ItemArmorAttachable toy = cls.getDeclaredConstructor().newInstance();
			if (toy != null) {
				toy.identify();
			}
			return toy;
		} catch (Exception e) {
			return null;
		}
	}

	public static int toyTierCount() {
		return TOY_CLASSES_BY_TIER.length;
	}

	public static Class<? extends ItemArmorAttachable>[] toyClassesForTier(int tier) {
		if (tier < 1 || tier > TOY_CLASSES_BY_TIER.length) {
			throw new IllegalArgumentException("Invalid toy tier: " + tier);
		}
		return TOY_CLASSES_BY_TIER[tier - 1].clone();
	}

	/**
	 * 将玩具附着到护甲上
	 */
	public void attachToy(ItemArmorAttachable toy) {
		attachedToys.add(toy);
		toy.attachToArmor(this);
		if (Dungeon.hero != null && isEquipped(Dungeon.hero)) {
			toy.applyEffect(Dungeon.hero);
		}
	}

	public int toyCapacity() {
		int base = Dungeon.hero != null && Dungeon.hero.armorAbility instanceof ToyBackpack
				? ToyBackpack.attachedToyCapacity(Dungeon.hero) : 2;
		boolean temporaryBag = Dungeon.hero != null && isEquipped(Dungeon.hero)
				&& ChallengeToyEffects.hasEffect(Dungeon.hero, TieredToy.BagOfHolding.class);
		return hasToy(TieredToy.BagOfHolding.class) || temporaryBag ? base + 2 : base;
	}

	public static class ToyRef {
		public final ItemArmorAttachable toy;
		private final Armor armor;
		private ToyRef(ItemArmorAttachable toy, Armor armor) { this.toy = toy; this.armor = armor; }
		public void destroy(Hero hero) {
			if (armor != null) {
				int index = armor.attachedToys.indexOf(toy);
				if (index >= 0) armor.detachToy(index);
				armor.dropExcessToys(hero);
			} else {
				toy.detachAll(hero.belongings.backpack);
			}
		}
	}

	public static ArrayList<ToyRef> ownedToys(Hero hero) {
		ArrayList<ToyRef> result = new ArrayList<>();
		collectToyRefs(hero.belongings.backpack, result);
		if (hero.belongings.armor != null) addArmorToyRefs(hero.belongings.armor, result);
		return result;
	}

	private static void collectToyRefs(Bag bag, ArrayList<ToyRef> result) {
		for (Item item : bag.items) {
			if (item instanceof ItemArmorAttachable) result.add(new ToyRef((ItemArmorAttachable) item, null));
			if (item instanceof Armor) addArmorToyRefs((Armor) item, result);
			if (item instanceof Bag) collectToyRefs((Bag) item, result);
		}
	}

	private static void addArmorToyRefs(Armor armor, ArrayList<ToyRef> result) {
		for (ItemArmorAttachable toy : armor.attachedToys) {
			boolean present = false;
			for (ToyRef ref : result) if (ref.toy == toy) { present = true; break; }
			if (!present) result.add(new ToyRef(toy, armor));
		}
	}

	public void requestAttachToy(Hero hero, ItemArmorAttachable toy) {
		if (attachedToys.size() < toyCapacity()) {
			completeToyAttachment(hero, toy);
		} else {
			GameScene.show(new WndReplaceToy(hero, this, toy));
		}
	}

	private void completeToyAttachment(Hero hero, ItemArmorAttachable toy) {
		attachToy(toy);
		toy.detach(hero.belongings.backpack);
		GLog.p(Messages.get(ItemArmorAttachable.class, "attached", toy.name(), name()));
	}

	private void returnToy(Hero hero, ItemArmorAttachable toy) {
		if (!toy.collect(hero.belongings.backpack)) {
			if (toy instanceof BrokenSeal) Dungeon.level.drop(toy, hero.pos).sprite.drop();
			else toy.vanishOnGround(false, hero.pos);
		}
	}

	private void replaceToy(Hero hero, int index, ItemArmorAttachable replacement) {
		ItemArmorAttachable removed = attachedToys.get(index);
		detachToy(index);
		returnToy(hero, removed);
		completeToyAttachment(hero, replacement);
		dropExcessToys(hero);
	}

	private void dropExcessToys(Hero hero) {
		while (attachedToys.size() > toyCapacity()) {
			int overflowIndex = toyCapacity();
			ItemArmorAttachable removed = attachedToys.get(overflowIndex);
			detachToy(overflowIndex);
			if (removed instanceof BrokenSeal) Dungeon.level.drop(removed, hero.pos).sprite.drop();
			else removed.vanishOnGround(true, hero.pos);
		}
	}

	public void dropExcessToysAfterCapacityChange(Hero hero) {
		if (hero != null) dropExcessToys(hero);
	}

	/**
	 * 从护甲卸下玩具
	 */
	public void detachToy(int index) {
		if (index >= 0 && index < attachedToys.size()) {
			ItemArmorAttachable toy = attachedToys.remove(index);
			if (Dungeon.hero != null && isEquipped(Dungeon.hero)) {
				ChallengeToyEffects.removePermanentEffect(Dungeon.hero, toy);
			}
			toy.attachedTo = null;
		}
	}

	/**
	 * 卸下破损纹章（向后兼容，现在统一通过 detachToy 处理）
	 * 卸下时：护甲等级-1返还给纹章，如果有符文传递天赋则携带护甲附魔/诅咒
	 */
	public void detachSeal(Hero hero) {
		BrokenSeal seal = getToy(BrokenSeal.class);
		if (seal == null) return;

		// 如果纹章有等级，返还给护甲-1
		if (seal.level() > 0) {
			int newLevel = Math.max(0, trueLevel() - 1);
			level(newLevel);
		}

		// 如果有符文传递天赋，携带护甲附魔/诅咒
		if (hero != null && hero.hasTalent(Talent.RUNIC_TRANSFERENCE)) {
			if (glyph != null) {
				seal.inscribe(glyph);
			}
		}

		int idx = attachedToys.indexOf(seal);
		detachToy(idx);
	}

	/**
	 * 获取指定类型的已附着玩具
	 */
	@SuppressWarnings("unchecked")
	public <T extends ItemArmorAttachable> T getToy(Class<T> toyClass) {
		for (ItemArmorAttachable toy : attachedToys) {
			if (toy.getClass() == toyClass) {
				return (T) toy;
			}
		}
		return null;
	}

	/**
	 * 获取所有附着玩具
	 */
	public ArrayList<ItemArmorAttachable> getToys() {
		return new ArrayList<>(attachedToys);
	}

	/**
	 * 检查是否附着了指定类型的玩具
	 */
	public boolean hasToy(Class<? extends ItemArmorAttachable> toyClass) {
		return getToy(toyClass) != null;
	}

	/**
	 * 附着玩具数量
	 */
	public int toyCount() {
		return attachedToys.size();
	}

	// ========== 窗口类 ==========

	public class WndReplaceToy extends WndOptions {
		private final Hero hero;
		private final Armor armor;
		private final ItemArmorAttachable replacement;

		public WndReplaceToy(Hero hero, Armor armor, ItemArmorAttachable replacement) {
			super(Messages.get(Armor.class, "replace_toy_title"),
					Messages.get(Armor.class, "replace_toy_message", replacement.name()),
					armor.attachedToys.stream().map(ItemArmorAttachable::name).toArray(String[]::new));
			this.hero = hero;
			this.armor = armor;
			this.replacement = replacement;
		}

		@Override protected void onSelect(int index) {
			if (index >= 0 && index < armor.attachedToys.size()) armor.replaceToy(hero, index, replacement);
		}
	}

	/**
	 * 卸下附着物品窗口
	 */
	public class WndDetachItems extends WndOptions {

		private Hero hero;
		private Armor armor;

		public WndDetachItems(Hero hero, Armor armor) {
			super(
				Messages.get(Armor.class, "detach_title"),
				Messages.get(Armor.class, "detach_message"),
				armor.attachedToys.stream().map(ItemArmorAttachable::name).toArray(String[]::new)
			);
			this.hero = hero;
			this.armor = armor;
		}

		@Override
		protected void onSelect(int index) {
			if (index < 0 || index >= armor.attachedToys.size()) return;
			ItemArmorAttachable item = armor.attachedToys.get(index);

			// 破损纹章有特殊卸下逻辑
			if (item instanceof BrokenSeal) {
				armor.detachSeal(hero);
			} else {
				armor.detachToy(index);
			}
			armor.dropExcessToys(hero);

			GLog.i(Messages.get(Armor.class, "detached_toy", item.name()));
			hero.sprite.operate(hero.pos);
			if (!item.collect()) {
				if (item instanceof BrokenSeal) Dungeon.level.drop(item, hero.pos).sprite.drop();
				else item.vanishOnGround(false, hero.pos);
			}
		}
	}

	/**
	 * 玩具背包信息窗口
	 */
	public class WndToyBackpackInfo extends WndTitledMessage {

		public WndToyBackpackInfo(Armor armor) {
			super(
				new ItemSprite(armor.image(), null),
				Messages.get(Armor.class, "toy_backpack_title"),
				buildToyBackpackMessage(armor)
			);
		}
	}

	// 静态辅助方法：构建玩具背包信息文本
	private static String buildToyBackpackMessage(Armor armor) {
		StringBuilder sb = new StringBuilder();
		int currentCharge = armor instanceof ClassArmor ? (int)Math.floor(((ClassArmor)armor).charge) : armor.toyCharge;
		float chargeCost = Dungeon.hero != null && Dungeon.hero.armorAbility instanceof ToyBackpack
				? Dungeon.hero.armorAbility.chargeUse(Dungeon.hero)
				: ToyBackpack.BASE_CHARGE_COST;
		int ownedToyCount = Dungeon.hero == null ? armor.toyCount() : Armor.ownedToys(Dungeon.hero).size();
		int totalToyCapacity = Dungeon.hero != null && Dungeon.hero.armorAbility instanceof ToyBackpack
				? ToyBackpack.totalToyCapacity(Dungeon.hero) : armor.toyCapacity();
		sb.append(Messages.get(Armor.class, "toy_backpack_charge", currentCharge, (int)Math.ceil(chargeCost)));
		sb.append("\n").append(Messages.get(Armor.class, "toy_backpack_slots", armor.toyCount(), armor.toyCapacity()));
		sb.append("\n").append(Messages.get(Armor.class, "toy_backpack_total",
				ownedToyCount, totalToyCapacity));
		sb.append("\n\n");
		if (armor.attachedToys.isEmpty()) {
			sb.append(Messages.get(Armor.class, "toy_backpack_empty"));
		} else {
			sb.append(Messages.get(Armor.class, "toy_backpack_attached"));
			for (ItemArmorAttachable toy : armor.attachedToys) {
				sb.append("\n\n• ").append(toy.name()).append("\n  ").append(toy.desc());
			}
		}
		ChallengeToyEffects challengeEffects = Dungeon.hero == null
				? null : Dungeon.hero.buff(ChallengeToyEffects.class);
		if (challengeEffects != null && !challengeEffects.effects().isEmpty()) {
			sb.append("\n\n").append(Messages.get(Armor.class, "toy_backpack_challenge_effects"));
			ArrayList<ItemArmorAttachable> effects = challengeEffects.effects();
			for (int i = 0; i < effects.size(); i++) {
				ItemArmorAttachable toy = effects.get(i);
				sb.append("\n\n").append(Messages.get(Armor.class, "toy_backpack_challenge_effect",
						toy.name(), challengeEffects.remainingTurns(i)));
				sb.append("\n  ").append(toy.desc());
			}
		}
		return sb.toString();
	}

	@Override
	protected float time2equip( Hero hero ) {
		return 2 / hero.speed();
	}

	@Override
	public boolean collect(Bag container) {
		if(super.collect(container)){
			if (Dungeon.hero != null && Dungeon.hero.isAlive() && isIdentified() && glyph != null){
				Catalog.setSeen(glyph.getClass());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item identify(boolean byHero) {
		if (glyph != null && byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(glyph.getClass());
		}
		return super.identify(byHero);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			hero.belongings.armor = null;
			for (ItemArmorAttachable toy : attachedToys) {
				ChallengeToyEffects.removePermanentEffect(hero, toy);
			}

			if (buff != null) {
				buff.detach();
				buff = null;
			}
			((HeroSprite)hero.sprite).updateArmor();

			BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
			if (sealBuff != null) sealBuff.setArmor(null);

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.armor() == this;
	}

	public final int DRMax(){
		return DRMax(buffedLvl());
	}

	public int DRMax(int lvl){
		int max = tier * (2 + lvl) + augment.defenseFactor(lvl);
		if (lvl > max){
			return ((lvl - max)+1)/2;
		} else {
			return max;
		}
	}

	public final int DRMin(){
		return DRMin(buffedLvl());
	}

	public int DRMin(int lvl){
		int max = DRMax(lvl);
		int min;
		if (lvl >= max){
			min = (lvl - max);
		} else {
			min = lvl;
		}

		// 剑盾骑士天赋：月华护甲最小值至少为武器伤害最小值的倍数
		if (Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.MOONLIGHT) {
			int points = Dungeon.hero.pointsInTalent(Talent.SWORD_SHIELD_KNIGHT);
			if (points > 0 && Dungeon.hero.belongings.weapon instanceof MeleeWeapon) {
				// 获取武器伤害最小值
				int weaponMinDamage = Dungeon.hero.belongings.weapon.min();
				// 计算加成：+1=100%, +2=125%, +3=150%
				float multiplier = 1.0f + (points - 1) * 0.25f;
				int talentMin = Math.round(weaponMinDamage * multiplier);
				// 不能超过护甲最大值
				min = Math.min(Math.max(min, talentMin), max);
			}
		}

		return min;
	}

	public float evasionFactor( Char owner, float evasion ){

		if (hasGlyph(Stone.class, owner) ){
			BrokenSeal s = checkSeal();
			if((glyph instanceof Stone && !((Stone)glyph).testingEvasion())
					|| (s != null && s.getGlyph() != null && s.getGlyph() instanceof Stone))
				return 0;
		}

		if (owner instanceof Hero){
			int aEnc = STRReq() - ((Hero) owner).STR();
			if (aEnc > 0) evasion /= Math.pow(1.5, aEnc);

			Momentum momentum = owner.buff(Momentum.class);
			if (momentum != null){
				evasion += momentum.evasionBonus(((Hero) owner).lvl, Math.max(0, -aEnc));
			}
		}

		return evasion + augment.evasionFactor(buffedLvl());
	}

	public float speedFactor( Char owner, float speed ){

		if (owner instanceof Hero) {
			int aEnc = STRReq() - ((Hero) owner).STR();
			if (aEnc > 0) speed /= Math.pow(1.2, aEnc);
		}

		if (hasGlyph(Swiftness.class, owner)) {
			boolean enemyNear = false;
			PathFinder.buildDistanceMap(owner.pos, Dungeon.level.passable, 2);
			for (Char ch : Actor.chars()){
				if ( PathFinder.distance[ch.pos] != Integer.MAX_VALUE && owner.alignment != ch.alignment){
					enemyNear = true;
					break;
				}
			}
			if (!enemyNear) speed *= (1.2f + 0.04f * procLvl()) * RingOfArcana.enchantPowerMultiplier(owner)*owner.talentProc();
		}
		if (hasGlyph(Flow.class, owner) && Dungeon.level.water[owner.pos]){
			speed *= (2f + 0.25f*procLvl()) * RingOfArcana.enchantPowerMultiplier(owner)*owner.talentProc();
		}

		if (hasGlyph(Bulk.class, owner) &&
				(Dungeon.level.map[owner.pos] == Terrain.DOOR
						|| Dungeon.level.map[owner.pos] == Terrain.OPEN_DOOR )) {
			speed /= 3f * RingOfArcana.enchantPowerMultiplier(owner);
		}

		return speed;

	}

	public float stealthFactor( Char owner, float stealth ){

		if (hasGlyph(Obfuscation.class, owner)){
			stealth += (1 + procLvl()/3f) * RingOfArcana.enchantPowerMultiplier(owner)*owner.talentProc();
		}

		return stealth;
	}

	@Override
	public int level() {
		int level = super.level();
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}

	//other things can equip these, for now we assume only the hero can be affected by levelling debuffs
	@Override
	public int buffedLvl() {
		if (hero != null && hero.belongings.armor == this) {
			GoldRadish goldRadish = hero.belongings.getItem(GoldRadish.class);
			if(goldRadish != null){
				return goldRadish.fixedLevel(goldRadish.buffedLvl());
			}

			RiverCrystal riverGlass = hero.belongings.getItem(RiverCrystal.class);
			// 塑形玻璃的虚拟等级需要与国王之戒的虚拟等级叠加
			if(hero.buff(BlessAWP.ArmorGetReady.class)!=null && hero.belongings.armor() == this && riverGlass != null){
				return super.buffedLvl()+1 + riverGlass.level() + 1 + RingOfKing.updateMultiplier(Dungeon.hero);
			} else if(hero.buff(BlessAWP.ArmorGetReady.class)!=null && hero.belongings.armor() == this) {
				return super.buffedLvl()+1 + RingOfKing.updateMultiplier(Dungeon.hero);
			} else if(riverGlass != null){
				return super.buffedLvl() + riverGlass.level() + 1 + RingOfKing.updateMultiplier(Dungeon.hero);
			}


			if (hero.pointsInTalent(Talent.GIFT) > 0) {
				// 获取天赋等级（1-4）
				int giftLevel = hero.pointsInTalent(Talent.GIFT);

				// 根据天赋等级计算基础要求的最小等级（+1对应2，+2对应3，以此类推）
				int baseRequiredLevel = giftLevel + 1;

				// 计算基础值：取当前基础等级和要求的最小等级中的较大值
				int baseValue = Math.max(super.buffedLvl(), baseRequiredLevel);

				// 计算最终值：基础值加上戒指加成，如果有祝福则额外+2
				int finalValue = baseValue + RingOfKing.updateMultiplier(Dungeon.hero);
				if (hero.buff(Bless.class) != null) {
					finalValue += 2;
				}

				return finalValue;
			}


			if(Dungeon.hero.buff( Degrade.class ) != null){
						return super.buffedLvl();
					} else {
						return hero.belongings.armor.level() + RingOfKing.updateMultiplier(Dungeon.hero);
					}
		}

		if (hero != null && (isEquipped(hero) || hero.belongings.contains(this))){
			return super.buffedLvl();
		} else {
			return level();
		}
	}

	@Override
	public Item upgrade() {
		return upgrade( false );
	}

	public Item upgrade( boolean inscribe ) {

		if (inscribe){
			if (glyph == null){
				inscribe( Glyph.random() );
			}
		} else {
			if (hasCurseGlyph()){
				if (Random.Int(3) == 0) inscribe(null);
			} else if (level() >= 4 && Random.Float(10) < Math.pow(2, level()-4)){
				inscribe(null);
			}
		}

		cursed = false;

		BrokenSeal s = checkSeal();
		if (s != null && s.level() == 0)
			s.upgrade();

		return super.upgrade();
	}

	public int proc( Char attacker, Char defender, int damage ) {

		if (glyph != null && defender.buff(MagicImmune.class) == null) {
			damage = glyph.proc( this, attacker, defender, damage );
		}

		if (!levelKnown && defender == Dungeon.hero) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0) {
				if (ShardOfOblivion.passiveIDDisabled()){
					if (usesLeftToID > -1){
						GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), name());
					}
					usesLeftToID = -1;
				} else {
					identify();
					GLog.p(Messages.get(Armor.class, "identify"));
					Badges.validateItemLevelAquired(this);
				}
			}
		}

		return damage;
	}

	@Override
	public void onHeroGainExp(float levelPercent, Hero hero) {
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!levelKnown && isEquipped(hero) && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 0.5 levels
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID);
		}
	}

	@Override
	public String name() {
		return glyph != null && (cursedKnown || !glyph.curse()) ? glyph.name( super.name() ) : super.name();
	}

	@Override
	public String info() {
		String info = desc();

		if (levelKnown) {
			if(hero.belongings.weapon() instanceof CircleSword && hero.belongings.armor() == this){
				info += "\n\n" + Messages.get(Armor.class, "curr_absorb", 0, 0, STRReq());
			} else {
				info += "\n\n" + Messages.get(Armor.class, "curr_absorb", DRMin(), DRMax(), STRReq());
			}

			if (Dungeon.hero != null && STRReq() > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "too_heavy");
			}
		} else {
			info += "\n\n" + Messages.get(Armor.class, "avg_absorb", DRMin(0), DRMax(0), STRReq(0));

			if (Dungeon.hero != null && STRReq(0) > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "probably_too_heavy");
			}
		}

		String statsInfo = statsInfo();
		if (!statsInfo.equals("")) info += "\n\n" + statsInfo;

		switch (augment) {
			case EVASION:
				info += " " + Messages.get(Armor.class, "evasion");
				break;
			case DEFENSE:
				info += " " + Messages.get(Armor.class, "defense");
				break;
			case NONE:
		}

		if (glyph != null  && (cursedKnown || !glyph.curse())) {
			info += "\n\n" +  Messages.capitalize(Messages.get(Armor.class, "inscribed", glyph.name()));
			info += " " + glyph.desc();
		}

		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Armor.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Armor.class, "cursed");
		} else {
			BrokenSeal s = checkSeal();
			if (s != null) {
				info += "\n\n" + Messages.get(Armor.class, "seal_attached", s.maxShield(tier, level()));
				Glyph g = s.getGlyph();
				if (g != null) {
					info += "\n\n" + Messages.capitalize(Messages.get(Armor.class, "inscribed", g.name()));
					info += " " + g.desc();
				}
			}
		}

		if (!isIdentified() && cursedKnown) {
			if (glyph != null && glyph.curse()) {
				info += "\n\n" + Messages.get(Armor.class, "weak_cursed");
			} else {
				info += "\n\n" + Messages.get(Armor.class, "not_cursed");
			}
		}

		return info;
	}

	public String statsInfo(){
		return Messages.get(this, "stats_desc");
	}
	@Override
	public Emitter emitter() {
		BrokenSeal s = checkSeal();
		if (s == null) return super.emitter();
		Emitter emitter = new Emitter();
		emitter.pos(ItemSprite.atlasFrame(image).width/2f + 2f, ItemSprite.atlasFrame(image).height/3f);
		emitter.fillTarget = false;
		emitter.pour(Speck.factory( Speck.RED_LIGHT ), 0.6f);
		return emitter;
	}

	@Override
	public Item random() {
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		level(n);

		//30% chance to be cursed
		//15% chance to be inscribed
		float effectRoll = Random.Float();
		if (effectRoll < 0.3f * ParchmentScrap.curseChanceMultiplier()) {
			inscribe(Glyph.randomCurse());
			cursed = true;
		} else if (effectRoll >= 1f - (0.15f * ParchmentScrap.enchantChanceMultiplier())){
			inscribe();
		}

		return this;
	}

	public int STRReq(){
		int req2 = STRReq(buffedLvl());
		int req = STRReq(level());
		if(req2 != 0 ){
			req = req2;
		}
		int multi = RingOfKing.updateMultiplier(Dungeon.hero);
		if( RingOfKing.curItem != null && RingOfKing.curItem.cursed )
			multi = 1;
		// 暂时这样吧，先把问题修了
		// bug fix 20240727
		if(hero != null){
			RingOfKing ringOfKing = hero.belongings.getItem(RingOfKing.class);
			if(ringOfKing != null){
				if(hero.belongings.armor == this && (hero.belongings.misc instanceof RingOfKing || hero.belongings.ring instanceof RingOfKing)) req = req + multi;
			}
		}
		if (masteryPotionBonus){
			req -= 2;
		}
		return req;
	}

	public int STRReq(int lvl){
		return STRReq(tier, lvl);
	}

	protected static int STRReq(int tier, int lvl){
		lvl = Math.max(0, lvl);

		//strength req decreases at +1,+3,+6,+10,etc.
		return (8 + Math.round(tier * 2)) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}

	@Override
	public int value() {
		if (checkSeal() != null) return 0;

		int price = 20 * tier;
		if (hasGoodGlyph()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseGlyph())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	protected ArmorBuff buff() {
		return null;
	}

	public Armor inscribe( Glyph glyph ) {
		if (glyph == null || !glyph.curse()) curseInfusionBonus = false;
		this.glyph = glyph;
		updateQuickslot();
		//the hero needs runic transference to actually transfer, but we still attach the glyph here
		// in case they take that talent in the future
		BrokenSeal s = checkSeal();
		if (s != null){
			s.setGlyph(glyph);
		}
		if (glyph != null && isIdentified() && Dungeon.hero != null
				&& Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(this)){
			Catalog.setSeen(glyph.getClass());
		}
		return this;
	}

	public Armor inscribe() {

		Class<? extends Glyph> oldGlyphClass = glyph != null ? glyph.getClass() : null;
		Glyph gl = Glyph.random( oldGlyphClass );

		return inscribe( gl );
	}

	public boolean hasGlyph(Class<?extends Glyph> type, Char owner) {
		boolean armorHasGlyph=glyph != null && glyph.getClass() == type && owner.buff(MagicImmune.class) == null;
		BrokenSeal s = checkSeal();
		boolean sealHasGlyph = s != null && s.getGlyph() != null && s.getGlyph().getClass() == type && owner.buff(MagicImmune.class) == null;
		return armorHasGlyph||sealHasGlyph;
	}

	//these are not used to process specific glyph effects, so magic immune doesn't affect them
	public boolean hasGoodGlyph(){
		return glyph != null && !glyph.curse();
	}

	public boolean hasCurseGlyph(){
		return glyph != null && glyph.curse();
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return glyph != null && (cursedKnown || !glyph.curse()) ? glyph.glowing() : null;
	}

	public static abstract class Glyph implements Bundlable {

		public static float genericProcChanceMultiplier( Char defender ){
			return RingOfArcana.enchantPowerMultiplier(defender);
		}



		public static final Class<?>[] common = new Class<?>[]{
				Obfuscation.class, Swiftness.class, Viscosity.class, Potential.class , SkyWalker.class};

		public static final Class<?>[] uncommon = new Class<?>[]{
				Brimstone.class, Stone.class, Entanglement.class,
				Repulsion.class, Camouflage.class, Flow.class };

		public static final Class<?>[] rare = new Class<?>[]{
				Affection.class, AntiMagic.class, Thorns.class };

		private static final float[] typeChances = new float[]{
				50, //12.5% each
				40, //6.67% each
				10  //3.33% each
		};
		public boolean onSeal=false;

		public static final Class<?>[] curses = new Class<?>[]{
				AntiEntropy.class, Corrosion.class, Displacement.class, Metabolism.class,
				Multiplicity.class, Stench.class, Overgrowth.class, Bulk.class
		};

		public abstract int proc( Armor armor, Char attacker, Char defender, int damage);

		protected float procChanceMultiplier( Char defender ){
			return RingOfArcana.enchantPowerMultiplier(defender);
		}

		public String name() {
			if (!curse())
				return name( Messages.get(this, "glyph") );
			else
				return name( Messages.get(Item.class, "curse"));
		}

		public String name( String armorName ) {
			return Messages.get(this, "name", armorName);
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			if (bundle.contains("onseal")){
				onSeal=bundle.getBoolean("onseal");
			}
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put("onseal",onSeal);
		}

		public abstract ItemSprite.Glowing glowing();

		@SuppressWarnings("unchecked")
		public static Glyph random( Class<? extends Glyph> ... toIgnore ) {
			switch(Random.chances(typeChances)){
				case 0: default:
					return randomCommon( toIgnore );
				case 1:
					return randomUncommon( toIgnore );
				case 2:
					return randomRare( toIgnore );
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph randomCommon( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(common));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph randomUncommon( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(uncommon));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph randomRare( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(rare));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph randomCurse( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(curses));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

	}
	public class ArmorBuff extends Buff {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (now() == 0 && cooldown() == 0 && target.cooldown() > 0) spend(TICK);
				return true;
			}
			return false;
		}

		@Override
		public boolean act() {
			spend( TICK );
			return true;
		}

		public int level(){
			return Armor.this.level();
		}

		public int buffedLvl(){
			return Armor.this.buffedLvl();
		}

	}
	@Override
	public void getCurse(boolean extraEffect) {
		if(extraEffect){
			if(glyph != null){
				inscribe(Glyph.randomCurse(glyph.getClass()));
			}else {
				inscribe(Glyph.randomCurse());
			}
		}
		super.getCurse(extraEffect);
	}
	public int procLvl(){
		BrokenSeal s = checkSeal();
		if (glyph != null && s != null && s.getGlyph() != null && s.getGlyph() == glyph) {
			return buffedLvl() + 1;
		}
		return buffedLvl();
	}
}
