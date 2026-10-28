package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.watabou.utils.Random;

/**
 * 绷带（医疗学派 L1）：视野内友军恢复5-9生命并获得5-9护盾。冷却30回合。
 */
public class BandageSpell extends DiceMageSpell {

    private static final float COOLDOWN = 30f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_MEDICAL;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "bandage";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int heal = Random.IntRange(5, 9);
        int shield = Random.IntRange(5, 9);
        int count = 0;
        for (Char ch : Dungeon.level.mobs) {
            if (ch.alignment != Char.Alignment.ALLY && ch != hero) continue;
            if (!Dungeon.level.heroFOV[ch.pos]) continue;
            ch.HP = Math.min(ch.HT, ch.HP + heal);
            Buff.affect(ch, Barrier.class).incShield(shield);
            CellEmitter.center(ch.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
            CellEmitter.center(ch.pos).burst(SparkParticle.FACTORY, 3);
            count++;
        }
        // hero counts as an ally
        hero.HP = Math.min(hero.HT, hero.HP + heal);
        Buff.affect(hero, Barrier.class).incShield(shield);
        CellEmitter.center(hero.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
        count++;

        startCooldown(hero, COOLDOWN);
        hero.spendAndNext(1f);
    }
}
