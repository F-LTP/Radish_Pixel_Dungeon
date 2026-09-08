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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.GamblerSkin;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;

public class Rogue extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.ROGUE; }
	@Override public String spritesheet() { return Assets.Sprites.ROGUE; }
	@Override public String splashArt() { return Assets.Splashes.ROGUE; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_ROGUE; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.ASSASSIN, HeroSubClasses.FREERUNNER };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new SmokeBomb(), new DeathMark(), new ShadowClone() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.CACHED_RATIONS, Talent.THIEFS_INTUITION, Talent.SUCKER_PUNCH, Talent.PROTECTIVE_SHADOWS });
		set.setTier2(new Talent[]{ Talent.MYSTICAL_MEAL, Talent.DUEL_DANCE, Talent.WIDE_SEARCH, Talent.SILENT_STEPS, Talent.ROGUES_INSTINCT });
		set.setTier3(new Talent[]{ Talent.DEATHBLOW, Talent.LIGHT_CLOAK });
		set.setTier4(new Talent[]{ Talent.HIDE_IN_CROWD, Talent.DARK_ARMOR });
		return set;
	}

	@Override public int sneakRadius() { return 2; }

	@Override public SkinDefinition[] skins() {
		return new SkinDefinition[]{ new GamblerSkin(this), new JumbleSkin(this) };
	}

	@Override protected void initClassLoadout(Hero hero) {
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
}
