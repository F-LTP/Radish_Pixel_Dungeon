package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Craftsman extends HeroSubClass {

	public Craftsman() {
		super("CRAFTSMAN", HeroIcon.CRAFTSMAN);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{
				Talent.ACTIVE_REPAIR, Talent.MULTI_MODIFY, Talent.PART_RECYCLING,
				Talent.FAVORITE_WORK, Talent.KINETIC_FRAGMENT };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[0];
	}
}
