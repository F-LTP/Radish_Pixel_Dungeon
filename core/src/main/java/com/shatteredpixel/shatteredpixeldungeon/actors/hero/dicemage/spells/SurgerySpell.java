package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Reflection;

/**
 * 手术（紧急学派 L3）：召唤1个英雄（最近击杀的怪物化为盟友）攻击敌人，最多1个；
 * 召唤死亡后冷却250回合。
 */
public class SurgerySpell extends DiceMageSpell {

    private static final float COOLDOWN = 250f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_EMERGENCY;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 3;
    }
    @Override
    public String sndImageName() {
        return "operate";
    }



    @Override
    protected void onCast(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null || mp.lastKilledMob() == null) {
            GLog.w(Messages.get(this, "no_corpse"));
            return;
        }
        // 最多1个召唤在场
        if (mp.activeSurgerySummon() != -1) {
            Mob existing = (Mob) Actor.findById(mp.activeSurgerySummon());
            if (existing != null && existing.isAlive()) {
                GLog.w(Messages.get(this, "already_active"));
                return;
            }
        }

        Mob ally = Reflection.newInstance(mp.lastKilledMob());
        if (Char.hasProp(ally, Char.Property.BOSS)) {
            GLog.w(Messages.get(this, "too_big"));
            return;
        }

        int spawnPos = -1;
        for (int i : PathFinder.NEIGHBOURS8) {
            int pos = hero.pos + i;
            if (pos >= 0 && pos < Dungeon.level.length()
                    && Actor.findChar(pos) == null
                    && Dungeon.level.passable[pos]) {
                spawnPos = pos;
                break;
            }
        }
        if (spawnPos == -1) {
            GLog.w(Messages.get(this, "no_space"));
            return;
        }

        if (!spendMagic(hero)) return;

        ally.pos = spawnPos;
        ally.HP = ally.HT;
        ally.alignment = Char.Alignment.ALLY;
        ally.state = ally.WANDERING;
        GameScene.add(ally);
        mp.setActiveSurgerySummon(ally.id());
        CellEmitter.center(ally.pos).burst(PurpleParticle.BURST, 8);
        mp.clearLastKilledMob();
        hero.spendAndNext(1f);
    }
}
