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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;

public class Mage extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.MAGE; }
	@Override public String spritesheet() { return Assets.Sprites.MAGE; }
	@Override public String splashArt() { return Assets.Splashes.MAGE; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_MAGE; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.BATTLEMAGE, HeroSubClasses.WARLOCK };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new ElementalBlast(), new WildMagic(), new WarpBeacon() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.EMPOWERING_MEAL, Talent.SCHOLARS_INTUITION, Talent.LINGERING_MAGIC, Talent.BACKUP_BARRIER });
		set.setTier2(new Talent[]{ Talent.ENERGIZING_MEAL, Talent.ENERGIZING_UPGRADE, Talent.WAND_PRESERVATION, Talent.ARCANE_VISION, Talent.SHIELD_BATTERY });
		set.setTier3(new Talent[]{ Talent.SPELL_QUEUE, Talent.ALLY_WARP });
		set.setTier4(new Talent[]{ Talent.MAGIC_REFINING, Talent.MAGIC_TACTICS });
		return set;
	}

	@Override public SkinDefinition[] skins() {
		return new SkinDefinition[]{ new JumbleSkin(this) };
	}

	@Override protected void initClassLoadout(Hero hero) {
		MagesStaff staff = new MagesStaff(new WandOfMagicMissile());
		(hero.belongings.weapon = staff).identify();
		hero.belongings.weapon.activate(hero);

		Dungeon.quickslot.setSlot(0, staff);

		new ScrollOfUpgrade().identify();
		new PotionOfLiquidFlame().identify();
	}
}
