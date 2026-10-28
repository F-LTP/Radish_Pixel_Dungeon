package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class CriticalAttack extends Buff{
    {
        type = buffType.POSITIVE;
    }

    @Override
    public String icon() {
        return BuffIndicator.TAI_CRIT;
    }

    // DoggingDog 20241221
    @Override
    public String iconTextDisplay() {
        return (int)(Dungeon.hero.critSkill()) + "%";
    }
}
