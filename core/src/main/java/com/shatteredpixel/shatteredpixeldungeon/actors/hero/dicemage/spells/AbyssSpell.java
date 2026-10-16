package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 深渊（特殊学派）：蓝耗40，杀死半血以下的敌人（无视锁血），冷却10回合。
 */
public class AbyssSpell extends DiceMageSpell {

    private static final float COOLDOWN = 10f;

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
        return 40;
    }
    @Override
    public String sndImageName() {
        return "abyss";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || !(target instanceof Mob)) {
                    GLog.w(Messages.get(AbyssSpell.this, "invalid_target"));
                    return;
                }
                if (target.HP >= target.HT / 2) {
                    GLog.w(Messages.get(AbyssSpell.this, "not_half"));
                    return;
                }
                if (!spendMagic(hero)) return;

                executeKill(target, hero);
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 14);
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(AbyssSpell.this, "prompt");
            }
        });
    }
}
