package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class DiceMage extends HeroSubClass {

	public DiceMage(){
		super("DICE_MAGE", HeroIcon.DICE_MAGE);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{
				Talent.SCHOOL_FIRE, Talent.SCHOOL_BLADES, Talent.SCHOOL_CONJURATION, Talent.SCHOOL_MANA, Talent.SCHOOL_BLOOD,
				Talent.SCHOOL_NATURE, Talent.SCHOOL_MEDICAL, Talent.SCHOOL_PHYSICAL, Talent.SCHOOL_EMERGENCY, Talent.SCHOOL_SPECIAL,
				Talent.D3_SKIPPED
		};
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.SPELL_EMPOWER, Talent.EGG_BASKET };
	}
}
