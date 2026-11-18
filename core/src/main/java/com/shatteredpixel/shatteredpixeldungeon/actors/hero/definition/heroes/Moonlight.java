package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.AshKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.FatedDraw;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.ToyBackpack;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.SphereSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Wheelchair;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.HashSet;

public class Moonlight extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.MOONLIGHT; }
	@Override public String spritesheet() { return Assets.Sprites.MOONLIGHT; }
	@Override public String splashArt() { return Assets.Splashes.MOONLIGHT; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_MOONLIGHT; }

	@Override protected boolean grantsVelvetPouch(){ return false; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.LITTLE_KNIGHT, HeroSubClasses.DICE_MAGE, HeroSubClasses.JUTTE_CHAMPION };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new FatedDraw(), new ToyBackpack(), new AshKing() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.HUNTING_INTUITION, Talent.SHARPENING_EDGE, Talent.WEAPON_MASTERY, Talent.WAR_TRAMPLE });
		set.setTier2(new Talent[]{ Talent.MEAL_UTILIZATION, Talent.STRONG_BODY, Talent.HOLY_SPRING, Talent.TRIPLE_INSURANCE, Talent.CATAPULT_START });
		set.setTier3(new Talent[]{ Talent.SWORD_SHIELD_KNIGHT, Talent.WHEELCHAIR_CRASH });
		set.setTier4(new Talent[]{ Talent.LIGHT_ETERNITY, Talent.MOON_GLORY });
		return set;
	}

	@Override public int[] baseHPGrowth() { return new int[]{ 18, 4 }; }

	@Override public SkinDefinition[] skins() {
		return new SkinDefinition[]{ new SphereSkin(this), new JumbleSkin(this) };
	}

	@Override protected void initClassLoadout(Hero hero) {
		Wheelchair wheelchair = new Wheelchair();
		(hero.belongings.artifact = wheelchair).identify();
		hero.belongings.artifact.activate( hero );
		// 初始武器：所有角色的初始武器
		(hero.belongings.weapon = new WornShortsword()).identify();
		new Dagger().identify().collect();
		new Gloves().identify().collect();
		new MagicalHolster().collect();
		Dungeon.LimitedDrops.MAGICAL_HOLSTER.drop();
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

		// 月华生命值调整：-2 最大生命值，-1 成长（已由 baseHPGrowth 处理）
		hero.HT = hero.HT - 2;
		hero.HP = hero.HT;
	}
}
