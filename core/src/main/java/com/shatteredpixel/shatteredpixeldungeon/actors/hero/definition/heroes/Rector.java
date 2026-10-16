package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector.GodsPossesion;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector.LastPrayer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector.ShadowHymn;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.SmallWoodenCross;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MetalCross;

public class Rector extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.RECTOR; }
	@Override public String spritesheet() { return Assets.Sprites.RECTOR; }
	@Override public String splashArt() { return Assets.Splashes.RECTOR; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_RECTOR; }

	@Override protected boolean grantsVelvetPouch(){ return false; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.BATTLEPREIST, HeroSubClasses.REDCARDINAL, HeroSubClasses.DEAD_KNIGHT };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new LastPrayer(), new ShadowHymn(), new GodsPossesion() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.PRAYER_BEFORE_MEALS, Talent.MENTAL_TELEPATHY, Talent.RAIN_GRACE, Talent.DEVOTIONAL });
		set.setTier2(new Talent[]{ Talent.BLESS_FOOD, Talent.SOUL_NOWIFI, Talent.LIGHT_STEP, Talent.GOD_BODY, Talent.NOHOPE_LANG });
		set.setTier3(new Talent[]{ Talent.ACT_GODPROGRESS, Talent.SMART_BLESSING });
		set.setTier4(new Talent[]{ Talent.SUPERSTITION, Talent.VITAE_BOOST });
		return set;
	}

	@Override public SkinDefinition[] skins() {
		return new SkinDefinition[]{ new JumbleSkin(this) };
	}

	@Override protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new MetalCross()).identify();

		new SmallWoodenCross().identify().collect();
		new ScrollHolder().identify().collect();
		Dungeon.LimitedDrops.SCROLL_HOLDER.drop();
		new Food().identify().collect();

		Buff.affect(hero, Belief.class);

		new ScrollOfIdentify().identify();
		new PotionOfExperience().identify();
		new ScrollOfRemoveCurse().identify();
	}

	@Override public void onExpGain(Hero hero, int exp) {
		Belief belief = hero.buff(Belief.class);
		if (belief != null && hero.superstitionCounter != null){
			belief.getBelief(hero.superstitionCounter.briefRet(exp));
		}
	}
}
