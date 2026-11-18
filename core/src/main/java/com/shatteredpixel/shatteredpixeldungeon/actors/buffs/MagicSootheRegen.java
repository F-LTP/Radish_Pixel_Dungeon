package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 抚慰（医疗学派 L3）：自然生命恢复提升 0.2/回合，持续 500 回合或直到下层。
 */
public class MagicSootheRegen extends Buff {

    public static final float DURATION = 500f;
    public static final float HEAL_PER_TURN = 0.2f;

    private float acc = 0f;
    private float turnsLeft = DURATION;

    {
        type = buffType.POSITIVE;
        actPriority = HERO_PRIO - 1;
    }

    @Override
    public boolean act() {
        if (target.HP < target.HT) {
            acc += HEAL_PER_TURN;
            int toHeal = (int) acc;
            if (toHeal > 0) {
                target.HP = Math.min(target.HT, target.HP + toHeal);
                acc -= toHeal;
            }
        }
        turnsLeft--;
        if (turnsLeft <= 0) {
            detach();
        }
        spend(TICK);
        return true;
    }

    @Override
    public String icon() {
        return BuffIndicator.HEALING;
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString((int) turnsLeft);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", (int) turnsLeft);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("acc", acc);
        bundle.put("turns", turnsLeft);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        acc = bundle.getFloat("acc");
        turnsLeft = bundle.getFloat("turns");
    }

    public static void apply(Char c) {
        MagicSootheRegen b = Buff.affect(c, MagicSootheRegen.class);
        b.turnsLeft = DURATION;
        b.acc = 0f;
    }
}
