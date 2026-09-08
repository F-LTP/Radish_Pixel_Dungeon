package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Random;

public class Gladiator extends HeroSubClass {

	public Gladiator(){
		super("GLADIATOR", HeroIcon.GLADIATOR);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.KEEP_VIGILANCE, Talent.LETHAL_DEFENSE, Talent.VENT_NOPLACE };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.DEFENSIVE_STRIKE, Talent.WEAPON_MASTER };
	}

	@Override public void onAttackProc(Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy) {
		if (hit && wasEnemy){
			Buff.affect(hero, Combo.class).hit();
		}
	}

	@Override public void onDefenseProc(Hero hero, Char enemy, int damage) {
		if (hero.hasTalent(Talent.DEFENSIVE_STRIKE)){
			if (Random.Float() < 0.25F * hero.pointsInTalent(Talent.DEFENSIVE_STRIKE))
				Buff.affect(hero, Combo.class).hit();
		}
	}
}
