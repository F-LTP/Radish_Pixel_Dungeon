package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChargeBoost;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.watabou.utils.Random;

/**
 * 充能（特殊学派）：获得10-18护盾，并在10回合内增伤30%。
 */
public class ChargeSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_SPECIAL;
    }

    @Override
    public int level() {
        return DiceMageSchools.specialSlot(getClass());
    }

    @Override
    public int mpCost() {
        return 4;
    }
    @Override
    public String sndImageName() {
        return "charge";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int shield = Random.IntRange(10, 18);
        Buff.affect(hero, Barrier.class).incShield(shield);
        Buff.affect(hero, ChargeBoost.class, ChargeBoost.DURATION);
        CellEmitter.center(hero.pos).burst(EnergyParticle.FACTORY, 10);
        hero.spendAndNext(1f);
    }
}
