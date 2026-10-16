package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 充能（特殊学派）：10回合内伤害提升30%。
 */
public class ChargeBoost extends FlavourBuff {

    public static final float DURATION = 10f;

    @Override
    public String icon() {
        return BuffIndicator.WEAPON;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", cooldown());
    }
}
