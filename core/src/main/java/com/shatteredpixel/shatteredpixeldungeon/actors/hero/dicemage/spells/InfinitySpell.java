package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 无限（特殊学派）：蓝耗100，直接杀死敌人，冷却10回合。
 */
public class InfinitySpell extends DiceMageSpell {

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
        return 100;
    }
    @Override
    public String sndImageName() {
        return "infinity";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(InfinitySpell.this, "invalid_target"));
                    return;
                }
                if (!spendMagic(hero)) return;

                // 无限：无视目标的无敌、锁血（deathMarked）、抗性及各类免疫，直接处决（含Boss单位，
                // 天狗/矮人国王等带流程的 Boss 会走其原生死亡流程，保证开门等关卡逻辑正常触发）。
                executeKill(target, hero);
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 18);
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(InfinitySpell.this, "prompt");
            }
        });
    }
}
