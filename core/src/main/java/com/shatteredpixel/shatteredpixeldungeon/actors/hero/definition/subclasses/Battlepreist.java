package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Battlepreist extends HeroSubClass {

	public Battlepreist(){
		super("BATTLEPREIST", HeroIcon.BATTLE_PRIEST);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.IRON_SUN, Talent.PHARCIS_BLESS, Talent.BEN_WORK };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.ADRENAL_COMBAT, Talent.GIFT };
	}
}
