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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.WandererSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;

public class Warrior extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.WARRIOR; }
	@Override public String spritesheet() { return Assets.Sprites.WARRIOR; }
	@Override public String splashArt() { return Assets.Splashes.WARRIOR; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_WARRIOR; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.BERSERKER, HeroSubClasses.GLADIATOR };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new HeroicLeap(), new Shockwave(), new Endure() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.HEARTY_MEAL, Talent.ARMSMASTERS_INTUITION, Talent.PROVOKED_ANGER, Talent.IRON_WILL });
		set.setTier2(new Talent[]{ Talent.IRON_STOMACH, Talent.EMERGENCY_PROTECTION, Talent.RUNIC_TRANSFERENCE, Talent.LETHAL_MOMENTUM, Talent.IMPROVISED_PROJECTILES });
		set.setTier3(new Talent[]{ Talent.HOLD_FAST, Talent.STRONGMAN });
		set.setTier4(new Talent[]{ Talent.IRON_MUSCLE, Talent.HIGH_DIET });
		return set;
	}

	@Override public SkinDefinition[] skins() {
		return new SkinDefinition[]{ new WandererSkin(this), new JumbleSkin(this) };
	}

	@Override protected void initClassLoadout(Hero hero) {		(hero.belongings.weapon = new WornShortsword()).identify();
		ThrowingStone stones = new ThrowingStone();
		stones.quantity(3).collect();
		Dungeon.quickslot.setSlot(0, stones);

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
		}

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}
}
