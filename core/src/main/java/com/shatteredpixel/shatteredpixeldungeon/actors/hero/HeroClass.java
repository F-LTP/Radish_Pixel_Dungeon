/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpectralBlades;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.AshKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.FatedDraw;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.ToyBackpack;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector.GodsPossesion;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector.LastPrayer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector.ShadowHymn;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.ChallengeBag;
import com.shatteredpixel.shatteredpixeldungeon.custom.dict.DictBook;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.BackpackCleaner;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.CustomPlayer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.CustomWeapon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.EnemyAttributeModifier;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.ImmortalShieldAffecter;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.LevelTeleporter;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.MobAttributeViewer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.MobPlacer;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.SnakeBiteToggle;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.SnDItemBox;
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
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.alive.SeedOfCard;
import com.shatteredpixel.shatteredpixeldungeon.items.alive.StoneOfCard;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MagneticCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Wheelchair;
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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.SmallWoodenCross;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CelestialSphere;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EchoplexHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnemyFlag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MetalCross;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PneumFistGloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShadowBooks;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Image;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.HashSet;

public enum HeroClass {

	WARRIOR( HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR ),
	MAGE( HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK ),
	ROGUE( HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER ),
	HUNTRESS( HeroSubClass.SNIPER, HeroSubClass.WARDEN ),

	//New Hero-Radish Pixel Dungeon
	RECTOR( HeroSubClass.BATTLEPREIST, HeroSubClass.REDCARDINAL, HeroSubClass.DEAD_KNIGHT),
	MOONLIGHT( HeroSubClass.LITTLE_KNIGHT, HeroSubClass.DICE_MAGE, HeroSubClass.JUTTE_CHAMPION);

//	DUELIST( HeroSubClass.CHAMPION, HeroSubClass.MONK );

	private HeroSubClass[] subClasses;

	HeroClass( HeroSubClass...subClasses ) {
		this.subClasses = subClasses;
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

		new TestBag().collect();

		new TrapPlacer().collect();

		new TimeReverser().collect();

		new ImmortalShieldAffecter().collect();

		new BackpackCleaner().collect();

		new LevelTeleporter().collect();

		new LazyTest().collect();

		new TestArmor().collect();
		new TestArtifact().collect();
		new SpawnWeapon().collect();
		new TestMissile().collect();
		new TestRing().collect();
		new SnDItemBox().collect();
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

	public void initHero( Hero hero ) {

		//[TEST MODE]
		if (Dungeon.isChallenged(Challenges.TEST_MODE))
			doChallengeSpawn();

		hero.rectorDeadKngithDeadMode = false;

		new StoneOfCard().collect();
		new SeedOfCard().collect();

		hero.heroClass = this;
		Talent.initClassTalents(hero);

		// Snake Bite challenge: start with 11 strength
		if (Dungeon.isChallenged(Challenges.SNAKE_BITE)) {
			hero.STR = Hero.STARTING_STR + 1;
		}

		Item i = new ClothArmor().identify();
		if (!Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		i = new Food();
		if (!Challenges.isItemBlocked(i)) i.collect();

		// 牧师不初始携带绒布包，商人会售卖
		// TODO 这里应该重构成每个职业在init里面添加对应的背包
		if (this != RECTOR && this != MOONLIGHT) {
			new VelvetPouch().collect();
			Dungeon.LimitedDrops.VELVET_POUCH.drop();
		}

		Waterskin waterskin = new Waterskin();
		waterskin.collect();

		new ScrollOfIdentify().identify();

		switch (this) {
			case WARRIOR:
				initWarrior( hero );
				break;

			case MAGE:
				initMage( hero );
				break;

			case ROGUE:
				initRogue( hero );
				break;

			case HUNTRESS:
				initHuntress( hero );
				break;

			case RECTOR:
				initRector( hero );
				break;

			case MOONLIGHT:
				initMoonlight( hero );
				break;
		}

		if (SPDSettings.quickslotWaterskin()) {
			for (int s = 0; s < QuickSlot.SIZE; s++) {
				if (Dungeon.quickslot.getItem(s) == null) {
					Dungeon.quickslot.setSlot(s, waterskin);
					break;
				}
			}
		}
	}

	private static void initWarrior( Hero hero ) {
		(hero.belongings.weapon = new WornShortsword()).identify();
		ThrowingStone stones = new ThrowingStone();
		stones.quantity(3).collect();
		Dungeon.quickslot.setSlot(0, stones);

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
		}

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}

	private static void initMage( Hero hero ) {
		MagesStaff staff;

		staff = new MagesStaff(new WandOfMagicMissile());

		(hero.belongings.weapon = staff).identify();
		hero.belongings.weapon.activate(hero);

		Dungeon.quickslot.setSlot(0, staff);

		new ScrollOfUpgrade().identify();
		new PotionOfLiquidFlame().identify();
	}

	private static void initRogue( Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();

		CloakOfShadows cloak = new CloakOfShadows();
		(hero.belongings.artifact = cloak).identify();
		hero.belongings.artifact.activate( hero );

		ThrowingKnife knives = new ThrowingKnife();
		knives.quantity(3).collect();

		Dungeon.quickslot.setSlot(0, cloak);
		Dungeon.quickslot.setSlot(1, knives);

		new ScrollOfMagicMapping().identify();
		new PotionOfInvisibility().identify();
	}

	private static void initHuntress( Hero hero ) {

		(hero.belongings.weapon = new Gloves()).identify();
		SpiritBow bow = new SpiritBow();
		bow.identify().collect();

		Dungeon.quickslot.setSlot(0, bow);

		new PotionOfMindVision().identify();
		new ScrollOfLullaby().identify();
	}

	private static void initRector( Hero hero ) {

		(hero.belongings.weapon = new MetalCross()).identify();

		new SmallWoodenCross().identify().collect();
		new ScrollHolder().identify().collect();
		new Food().identify().collect();

		Buff.affect(hero, Belief.class);

		new ScrollOfIdentify().identify();
		new PotionOfExperience().identify();
		new ScrollOfRemoveCurse().identify();
	}

	private static void initMoonlight( Hero hero ) {
		Wheelchair wheelchair = new Wheelchair();
		(hero.belongings.artifact = wheelchair).identify();
		hero.belongings.artifact.activate( hero );
		// 初始武器：所有角色的初始武器
		(hero.belongings.weapon = new WornShortsword()).identify();
		new Dagger().identify().collect();
		new Gloves().identify().collect();
		new MagicalHolster().collect();
		new MagesStaff().identify().collect();
		Dart knives = new Dart();
		knives.quantity(3).collect();

		Dungeon.quickslot.setSlot(0, wheelchair);
		Dungeon.quickslot.setSlot(1, knives);
		// 初始物品：力量药剂、升级卷轴、鉴定卷轴已鉴定
		// 以及四种随机卷轴与药水
		new PotionOfStrength().identify();
		new ScrollOfUpgrade().identify();
		new ScrollOfIdentify().identify();

		int scrollCount = Random.Int(5); // 0-4
		int potionCount = 4 - scrollCount;

		HashSet<Class<? extends Scroll>> unknownScrolls = Scroll.getUnknown();
		for (int i = 0; i < scrollCount && !unknownScrolls.isEmpty(); i++) {
			Class<? extends Scroll> scrollClass = Random.element(unknownScrolls);
			Reflection.newInstance(scrollClass).identify();
			unknownScrolls.remove(scrollClass);
		}

		HashSet<Class<? extends Potion>> unknownPotions = Potion.getUnknown();
		for (int i = 0; i < potionCount && !unknownPotions.isEmpty(); i++) {
			Class<? extends Potion> potionClass = Random.element(unknownPotions);
			Reflection.newInstance(potionClass).identify();
			unknownPotions.remove(potionClass);
		}

		// 月华生命值调整：-2 最大生命值，-1 成长
		hero.HT = hero.HT - 2;
		hero.HP = hero.HT;
	}

	public String title() {
		return Messages.get(HeroClass.class, name());
	}

	public String desc(){
		return Messages.get(HeroClass.class, name()+"_desc");
	}

	public String shortDesc(){
		return Messages.get(HeroClass.class, name()+"_desc_short");
	}

	public HeroSubClass[] subClasses() {
		return subClasses;
	}

	public ArmorAbility[] armorAbilities(){
		switch (this) {
			case WARRIOR: default:
				return new ArmorAbility[]{new HeroicLeap(), new Shockwave(), new Endure()};
			case MAGE:
				return new ArmorAbility[]{new ElementalBlast(), new WildMagic(), new WarpBeacon()};
			case ROGUE:
				return new ArmorAbility[]{new SmokeBomb(), new DeathMark(), new ShadowClone()};
			case HUNTRESS:
				return new ArmorAbility[]{new SpectralBlades(), new NaturesPower(), new SpiritHawk()};
			case RECTOR:
				return new ArmorAbility[]{new LastPrayer(),new ShadowHymn(),new GodsPossesion()};
			case MOONLIGHT:
				return new ArmorAbility[]{new FatedDraw(), new ToyBackpack(), new AshKing()};
		}
	}

	public String spritesheet() {
		switch (this) {
			case WARRIOR: default:
				return Assets.Sprites.WARRIOR;
			case MAGE:
				return Assets.Sprites.MAGE;
			case ROGUE:
				return Assets.Sprites.ROGUE;
			case HUNTRESS:
				return Assets.Sprites.HUNTRESS;
			case RECTOR:
				return Assets.Sprites.RECTOR;
			case MOONLIGHT:
				return Assets.Sprites.MOONLIGHT;
		}
	}

	public String splashArt(){
		switch (this) {
			case WARRIOR: default:
				return Assets.Splashes.WARRIOR;
			case MAGE:
				return Assets.Splashes.MAGE;
			case ROGUE:
				return Assets.Splashes.ROGUE;
			case HUNTRESS:
				return Assets.Splashes.HUNTRESS;
			case RECTOR:
				return Assets.Splashes.RECTOR;
			case MOONLIGHT:
				return Assets.Splashes.MOONLIGHT;
		}
	}
	
	public boolean isUnlocked(){
		//always unlock on debug builds
		if (DeviceCompat.isDebug()) return true;

		switch (this){
			case MAGE:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_MAGE);
			case ROGUE:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_ROGUE);
			case HUNTRESS:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_HUNTRESS);
			case RECTOR:
				return true;
			case MOONLIGHT:
				return true; // TODO: 添加解锁条件
			case WARRIOR: default:
				return true;
		}
	}
	
	public String unlockMsg() {
		return shortDesc() + "\n\n" + Messages.get(HeroClass.class, name()+"_unlock");
	}

	public String GetSkinAssest(){
		switch (this) {
			case WARRIOR: default:
				return Assets.Sprites.AVATARS_WARRIOR;
			case MAGE:
				return Assets.Sprites.AVATARS_MAGE;
			case ROGUE:
				return Assets.Sprites.AVATARS_ROGUE;
			case HUNTRESS:
				return Assets.Sprites.AVATARS_HUNTRESS;
			case RECTOR:
				return Assets.Sprites.AVATARS_RECTOR;
			case MOONLIGHT:
				return Assets.Sprites.AVATARS_MOONLIGHT;
		}
	}

	private static boolean onlyMode = false;



	/**
	 *
	 * @param skinIndex 注意皮肤iNDEX与PNG索引有关
	 */
	public void SetSkin(int skinIndex){
		boolean isSkinUnlock = false;
		Image img = new Image(this.GetSkinAssest());
		int skinCount = img.texture.width/64;

		if(skinIndex==0){
			isSkinUnlock = true;
		}else {
			while ( skinIndex < skinCount ) {
				switch (this) {
					case WARRIOR:
					default:
						isSkinUnlock = SPDSettings.isItemUnlock("avatars_warrior_" + skinIndex);
						break;
					case MAGE:
						isSkinUnlock = SPDSettings.isItemUnlock("avatars_mage_" + skinIndex);
						break;
					case ROGUE:
						isSkinUnlock = SPDSettings.isItemUnlock("avatars_rogue_" + skinIndex);
						break;
					case HUNTRESS:
						isSkinUnlock = SPDSettings.isItemUnlock("avatars_huntress_" + skinIndex);
						break;
					case RECTOR:
						isSkinUnlock = SPDSettings.isItemUnlock("avatars_rector_" + skinIndex);
						break;
					case MOONLIGHT:
						isSkinUnlock = SPDSettings.isItemUnlock("avatars_moonlight_" + skinIndex);
						break;
				}
				if(!isSkinUnlock){
					skinIndex++;
				}else {
					break;
				}
			}
		}

		if(!isSkinUnlock){
			skinIndex=0;
			if(!onlyMode){
				ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndKeyBindings.class,"switch_skin")));
				onlyMode = true;
			}
		}


		SPDSettings.setHeroSkin(this.ordinal(),skinIndex);
	}

	public int GetSkin(){
		return SPDSettings.getHeroSkin(this.ordinal());
	}

}
