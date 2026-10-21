package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Illusionist extends HeroSubClass {

	public Illusionist() {
		super("ILLUSIONIST", HeroIcon.ILLUSIONIST);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.CARD_ENCHANT, Talent.REFINED_ATTACK_DEFENSE, Talent.CARD_ASSIST };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.PLACEHOLDER_T4_1, Talent.PLACEHOLDER_T4_2 };
	}
}
