package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class DeadKnight extends HeroSubClass {

	public DeadKnight(){
		super("DEAD_KNIGHT", HeroIcon.DEAD_KNIGHT);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.BLACK_LOVE, Talent.DEAD_POWER, Talent.EXP_IMPOTION };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.ERROR };
	}
}
