package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.JutteChampionWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class JutteChampion extends HeroSubClass {

	public JutteChampion(){
		super("JUTTE_CHAMPION", HeroIcon.JUTTE_CHAMPION);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.ONE_JUTTE, Talent.IRON_QUENCH, Talent.SURPRISE_JUTTE };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.ERROR };
	}

	@Override public float attackDelayMultiplier(Hero hero, Char enemy, boolean surpriseAttack) {
		if (surpriseAttack
				&& hero.hasTalent(Talent.SURPRISE_JUTTE)
				&& hero.belongings.attackingWeapon() instanceof JutteChampionWeapon){
			int points = hero.pointsInTalent(Talent.SURPRISE_JUTTE);
			return points == 2 ? 0.66f : 0.33f;
		}
		return 1f;
	}
}
