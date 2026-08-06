package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class FaithObstruction extends FlavourBuff {

    public static final float DURATION	= 20f;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    @Override
    public void detach() {
        super.detach();
        Belief failed = Dungeon.hero.buff(Belief.class);
        if (failed != null){
           failed.not_link = false;
        }
    }

    @Override
    public String icon() {
        return BuffIndicator.BELIEF_DNOT;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }

}
