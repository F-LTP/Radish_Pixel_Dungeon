package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NonDecayingPoison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 毒芹（自然学派 L1）：造成3-7绿毒伤害并附加等量中毒层数。冷却10回合。
 */
public class HemlockSpell extends DiceMageSpell {

    private static final float COOLDOWN = 10f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_NATURE;
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
        return "hemlock";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(HemlockSpell.this, "invalid_target"));
                    return;
                }
                if (!spendMagic(hero)) return;

                int dmg = Random.IntRange(3, 7) + strBonusDamage(hero);
                target.damage(DamageInfo.poison(dmg, HemlockSpell.this));
                if (target.isAlive()) {
                    Buff.affect(target, NonDecayingPoison.class).set(dmg);
                    CellEmitter.center(target.pos).burst(PoisonParticle.SPLASH, 8);
                }
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(HemlockSpell.this, "prompt");
            }
        });
    }
}
