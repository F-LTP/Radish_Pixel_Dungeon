package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Berserker extends HeroSubClass {

	public Berserker(){
		super("BERSERKER", HeroIcon.BERSERKER);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.ENDLESS_RAGE, Talent.PAIN_SCAR, Talent.FANATICISM_MAGIC };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.REVENGE_ROAR, Talent.THIRSTY_BLADE };
	}

	@Override public void onAttackProc(Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy) {
		if (damage > 0){
			Berserk berserk = Buff.affect(hero, Berserk.class);
			berserk.damage(damage);
		}
	}
}
