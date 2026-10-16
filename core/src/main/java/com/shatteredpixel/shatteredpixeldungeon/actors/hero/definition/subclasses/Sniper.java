package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Sniper extends HeroSubClass {

	public Sniper(){
		super("SNIPER", HeroIcon.SNIPER);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.FARSIGHT, Talent.SHARED_ENCHANTMENT, Talent.SHARED_UPGRADES };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.COMMON_SHOT, Talent.STORM_ATTACK };
	}
}
