package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class LittleKnight extends HeroSubClass {

	public LittleKnight(){
		super("LITTLE_KNIGHT", HeroIcon.LITTLE_KNIGHT);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.WONT_LOSE, Talent.WET_ENCHANT, Talent.LEFT_BOW_RAPID };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.SHIELD_POKE, Talent.KNIGHT_SPIRIT };
	}
}
