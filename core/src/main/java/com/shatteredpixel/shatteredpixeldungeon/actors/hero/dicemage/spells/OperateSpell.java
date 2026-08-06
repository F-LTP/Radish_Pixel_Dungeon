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

public class OperateSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        Hero hero = Dungeon.hero;
        if (hero == null) return 3;
        MagicPoint mp = hero.buff(MagicPoint.class);
        int points = hero.pointsInTalent(Talent.LEARN_OPERATE);
        return mp == null ? 3 : mp.surgeryCost(points);
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        return hero != null && hero.pointsInTalent(Talent.LEARN_OPERATE) > 0 && super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null || mp.lastKilledMob() == null) {
            GLog.w(Messages.get(this, "no_corpse"));
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

        Mob ally = Reflection.newInstance(mp.lastKilledMob());
        ally.pos = spawnPos;
        ally.HP = ally.HT;
        ally.alignment = Char.Alignment.ALLY;
        ally.state = ally.WANDERING;
        GameScene.add(ally);
        CellEmitter.center(ally.pos).burst(PurpleParticle.BURST, 8);
        mp.clearLastKilledMob();
        mp.surgeryUsed();
        GLog.p(Messages.get(this, "cast", ally.name(), mp.surgeryCost(hero.pointsInTalent(Talent.LEARN_OPERATE))));
        hero.spendAndNext(1f);
    }
}
