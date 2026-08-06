package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class CutSpell extends DiceMageSpell {

    private static final int TOTAL_DAMAGE = 100;
    private static final int MAX_PER_ENEMY = 25;

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    protected void onCast(Hero hero) {
        int enemyCount = 0;
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                enemyCount++;
            }
        }

        if (enemyCount == 0) {
            GLog.w(Messages.get(this, "no_enemy"));
            return;
        }

        if (!spendMagic(hero)) return;

        int damagePerEnemy = Math.min(MAX_PER_ENEMY, TOTAL_DAMAGE / enemyCount);
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                mob.damage(damagePerEnemy, this);
                if (mob.isAlive()) {
                    CellEmitter.center(mob.pos).burst(BlastParticle.FACTORY, 6);
                    CellEmitter.center(mob.pos).burst(Speck.factory(Speck.RED_LIGHT), 2);
                }
            }
        }

        GLog.p(Messages.get(this, "cast", damagePerEnemy));
        hero.spendAndNext(1f);
    }
}
