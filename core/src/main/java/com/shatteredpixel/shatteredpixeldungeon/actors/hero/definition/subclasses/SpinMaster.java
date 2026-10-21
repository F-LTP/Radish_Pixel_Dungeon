package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class SpinMaster extends HeroSubClass {

	public SpinMaster() {
		super("SPIN_MASTER", HeroIcon.SPINMASTER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.SPIN_CARD, Talent.GRAND_FINALE, Talent.PREPARED };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.PLACEHOLDER_T4_1, Talent.PLACEHOLDER_T4_2 };
	}
}
