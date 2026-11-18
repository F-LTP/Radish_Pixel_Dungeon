package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class RedCardinal extends HeroSubClass {

	public RedCardinal(){
		super("REDCARDINAL", HeroIcon.RED_CARDINAL);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.FIRE_GLASS, Talent.LIGHT_WASH, Talent.SKY_TOWER };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.SOUL_POSSESSION, Talent.BLOODY_VITAE };
	}
}
