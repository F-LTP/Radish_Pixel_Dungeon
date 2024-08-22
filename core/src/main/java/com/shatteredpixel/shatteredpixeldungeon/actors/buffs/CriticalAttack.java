package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class CriticalAttack extends Buff{
    {
        type = buffType.POSITIVE;
    }

    @Override
    public int icon() {
        return BuffIndicator.WEAPON;
    }

}
