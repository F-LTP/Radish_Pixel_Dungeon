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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpectralBlades;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;

public class Huntress extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.HUNTRESS; }
	@Override public String spritesheet() { return Assets.Sprites.HUNTRESS; }
	@Override public String splashArt() { return Assets.Splashes.HUNTRESS; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_HUNTRESS; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.SNIPER, HeroSubClasses.WARDEN };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new SpectralBlades(), new NaturesPower(), new SpiritHawk() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.NATURES_BOUNTY, Talent.SURVIVALISTS_INTUITION, Talent.FOLLOWUP_STRIKE, Talent.UNDERESTIMATED });
		set.setTier2(new Talent[]{ Talent.INVIGORATING_MEAL, Talent.HERB_MIXTURE, Talent.REJUVENATING_STEPS, Talent.HEIGHTENED_SENSES, Talent.DURABLE_PROJECTILES });
		set.setTier3(new Talent[]{ Talent.HOLD_BREATH, Talent.SEER_SHOT });
		set.setTier4(new Talent[]{ Talent.BRISK_PACE, Talent.PHASE_FILLING });
		return set;
	}

	@Override public SkinDefinition[] skins() {
		return new SkinDefinition[]{ new JumbleSkin(this) };
	}

	@Override protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new Gloves()).identify();
		SpiritBow bow = new SpiritBow();
		bow.identify().collect();

		Dungeon.quickslot.setSlot(0, bow);

		new PotionOfMindVision().identify();
		new ScrollOfLullaby().identify();
	}
}
