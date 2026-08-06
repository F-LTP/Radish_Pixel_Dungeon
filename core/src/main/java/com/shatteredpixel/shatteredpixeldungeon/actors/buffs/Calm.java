package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Calm extends Buff{

    {
        type = buffType.POSITIVE;
    }

    @Override
    public String icon() {
        return BuffIndicator.TAI_COLD;
    }

    // DoggingDog 20241221
    @Override
    public String iconTextDisplay() {
        return (int)(Dungeon.hero.critDamage()*100f) + "%";
    }
}
