package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Warlock extends HeroSubClass {

	public Warlock(){
		super("WARLOCK", HeroIcon.WARLOCK);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.SOUL_EATER, Talent.SOUL_SIPHON, Talent.NECROMANCERS_MINIONS };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.DESPERATE_POWER, Talent.CORRUPT_SPIRIT };
	}
}
