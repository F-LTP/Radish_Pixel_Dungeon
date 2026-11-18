package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.events.PreAttackEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
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

    /** 攻击前由攻击前事件驱动：充能期间攻击伤害提升 30%（对任意持有者生效）。 */
    @SubscribeEvent(event = PreAttackEvent.class, priority = 500)
    public static void onPreAttack(PreAttackEvent event) {
        Char attacker = event.getAttacker();
        if (attacker != null && attacker.buff(ChargeBoost.class) != null) {
            event.multiplyDmgMulti(1.3f);
        }
    }
}
