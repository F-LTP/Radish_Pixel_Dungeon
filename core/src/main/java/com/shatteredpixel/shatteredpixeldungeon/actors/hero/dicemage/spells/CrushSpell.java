package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class CrushSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        return hero != null && hero.pointsInTalent(Talent.LEARN_CRUSH) > 0 && super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_CRUSH);
        int damage = points == 1 ? 25 : (points == 2 ? 38 : 40);

        Mob topEnemy = null, bottomEnemy = null;
        int topY = Integer.MAX_VALUE, bottomY = Integer.MIN_VALUE;

        for (Mob mob : Dungeon.level.mobs) {
            if (!Dungeon.level.heroFOV[mob.pos] || mob.alignment != Char.Alignment.ENEMY) continue;

            int y = mob.pos / Dungeon.level.width();
            if (y < topY) {
                topY = y;
                topEnemy = mob;
            }
            if (y > bottomY) {
                bottomY = y;
                bottomEnemy = mob;
            }
        }

        if (topEnemy == null || bottomEnemy == null || topEnemy == bottomEnemy) {
            GLog.w(Messages.get(this, "not_enough_enemies"));
            return;
        }

        if (!spendMagic(hero)) return;

        topEnemy.damage(damage, this);
        if (topEnemy.isAlive()) {
            CellEmitter.center(topEnemy.pos).burst(BlastParticle.FACTORY, 10);
            CellEmitter.center(topEnemy.pos).start(Speck.factory(Speck.ROCK), 0.08f, 4);
        }
        bottomEnemy.damage(damage, this);
        if (bottomEnemy.isAlive()) {
            CellEmitter.center(bottomEnemy.pos).burst(BlastParticle.FACTORY, 10);
            CellEmitter.center(bottomEnemy.pos).start(Speck.factory(Speck.ROCK), 0.08f, 4);
        }
        GLog.p(Messages.get(this, "cast", damage));
        hero.spendAndNext(1f);
    }
}
