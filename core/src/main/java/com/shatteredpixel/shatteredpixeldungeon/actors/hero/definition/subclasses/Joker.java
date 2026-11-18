package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

/** Third specialization placeholder. */
public class Joker extends HeroSubClass {

	public Joker() {
		super("JOKER", HeroIcon.JOKER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.TRAP_CRAFT, Talent.SPIRIT_COORDINATION, Talent.DECK_COMBO };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.TRAP_MASTERY, Talent.CAREFUL_SELECTION };
	}
}
