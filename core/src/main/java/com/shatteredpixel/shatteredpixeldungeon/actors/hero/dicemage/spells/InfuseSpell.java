package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.watabou.utils.Random;

/**
 * 注入（医疗学派 L2）：视野内友军获得5-9护盾。
 */
public class InfuseSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_MEDICAL;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "infuse";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int shield = Random.IntRange(5, 9);
        int count = 0;
        for (Char ch : Dungeon.level.mobs) {
            if (ch.alignment != Char.Alignment.ALLY) continue;
            if (!Dungeon.level.heroFOV[ch.pos]) continue;
            Buff.affect(ch, Barrier.class).incShield(shield);
            CellEmitter.center(ch.pos).burst(SparkParticle.FACTORY, 4);
            count++;
        }
        Buff.affect(hero, Barrier.class).incShield(shield);
        CellEmitter.center(hero.pos).burst(SparkParticle.FACTORY, 4);
        count++;

        hero.spendAndNext(1f);
    }
}
