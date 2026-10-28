package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MoveCount;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Freerunner extends HeroSubClass {

	public Freerunner(){
		super("FREERUNNER", HeroIcon.FREERUNNER);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.EVASIVE_ARMOR, Talent.PROJECTILE_MOMENTUM, Talent.SPEEDY_STEALTH };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.KINETIC_ENERGY, Talent.STORM_RUSH };
	}

	@Override public void onMove(Hero hero) {
		Buff.affect(hero, Momentum.class).gainStack();
		Buff.affect(hero, MoveCount.class).gainStack();
	}
}
