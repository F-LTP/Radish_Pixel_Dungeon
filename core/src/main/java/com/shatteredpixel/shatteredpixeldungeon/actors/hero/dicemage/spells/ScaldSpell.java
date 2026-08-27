package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 滚烫（火焰学派 L2）：视野内血量&lt;80%的敌人受到15-21火焰伤害。
 */
public class ScaldSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_FIRE;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 3;
    }
    @Override
    public String sndImageName() {
        return "scald";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int hit = 0;
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (!Dungeon.level.heroFOV[mob.pos] || mob.alignment != Char.Alignment.ENEMY) continue;
            if (mob.HP >= mob.HT * 0.8f) continue;
            int dmg = Random.IntRange(15, 21) + strBonusDamage(hero);
            mob.damage(DamageInfo.fire(dmg, ScaldSpell.this));
            if (mob.isAlive()) {
                CellEmitter.center(mob.pos).burst(FlameParticle.FACTORY, 6);
            }
            hit++;
        }
        if (hit == 0) {
            GLog.i(Messages.get(ScaldSpell.this, "none"));
        } else {
        }
        hero.spendAndNext(1f);
    }
}
