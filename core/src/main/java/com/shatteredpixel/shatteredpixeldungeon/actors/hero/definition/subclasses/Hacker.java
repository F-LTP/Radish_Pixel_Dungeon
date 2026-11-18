package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Hacker extends HeroSubClass {

	public Hacker() {
		super("HACKER", HeroIcon.HACKER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{
				Talent.BACKFIRE, Talent.DIFFRACTION, Talent.ELECTRIC_CHARGE,
				Talent.DARK_MAGIC, Talent.CHARISMA };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[0];
	}
}
