package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Warden extends HeroSubClass {

	public Warden(){
		super("WARDEN", HeroIcon.WARDEN);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.DURABLE_TIPS, Talent.BARKSKIN, Talent.VINE_TRAP };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.MORE_DARTS, Talent.GRASS_VISION };
	}
}
