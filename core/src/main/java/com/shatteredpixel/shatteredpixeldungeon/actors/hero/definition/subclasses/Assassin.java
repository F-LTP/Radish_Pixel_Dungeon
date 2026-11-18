package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Assassin extends HeroSubClass {

	public Assassin(){
		super("ASSASSIN", HeroIcon.ASSASSIN);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.ENHANCED_LETHALITY, Talent.ASSASSINS_REACH, Talent.BOUNTY_HUNTER };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.BRACE_YOURSELF, Talent.POWER_RECYCLE };
	}
}
