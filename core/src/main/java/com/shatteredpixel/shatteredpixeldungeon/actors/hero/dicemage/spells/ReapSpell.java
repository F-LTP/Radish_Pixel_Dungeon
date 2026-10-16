package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 收割（物理学派 L3）：杀死9-11血敌人，获得3点魔力。
 */
public class ReapSpell extends DiceMageSpell {

    private static final int MIN_HP = 9;
    private static final int MAX_HP = 11;

    @Override
    public Talent school() {
        return Talent.SCHOOL_PHYSICAL;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 1;
    }
    @Override
    public String sndImageName() {
        return "harvest";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || !(target instanceof Mob)) {
                    GLog.w(Messages.get(ReapSpell.this, "invalid_target"));
                    return;
                }
                if (target.HP < MIN_HP || target.HP > MAX_HP) {
                    GLog.w(Messages.get(ReapSpell.this, "out_of_range_hp", target.HP));
                    return;
                }
                if (!spendMagic(hero)) return;

                target.damage(new DamageInfo(target.HP, DamageType.TRUE, hero, null, ReapSpell.this));
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 8);
                applyStrShield(hero);
                Buff.affect(hero, MagicPoint.class).addPoints(3f);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(ReapSpell.this, "prompt");
            }
        });
    }
}
