package com.shatteredpixel.shatteredpixeldungeon.custom.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 法术易伤：使目标受到的所有魔法/元素伤害提高。
 * 由“颠茄”命中时施加。
 */
public class SpellVulnerable extends FlavourBuff {

    /** 受到魔法伤害的倍率。1.33 = +33%。 */
    public static final float DAMAGE_FACTOR = 1.33f;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    @Override
    public String icon() {
        return BuffIndicator.SPELL_VULNERABLE;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns());
    }
}
