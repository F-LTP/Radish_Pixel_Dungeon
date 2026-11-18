package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class LethalDefense extends Buff{
    public float duration;
    {
        type = buffType.POSITIVE;
    }

    public void resetTime(){
        int point = Dungeon.hero.pointsInTalent(Talent.LETHAL_DEFENSE);
        switch (point){
            case 1:
                duration = 2f;
                break;
            case 2:
                duration = 3f;
                break;
            case 3:
                duration = 4f;
                break;
            default:
                duration = 0;
                break;
        }
    }


    private static final String DURATION = "duration";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(DURATION, duration);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        //旧存档没有这个字段时保持 0：读档后的第一个回合自然结束，和以前的表现一样
        if (bundle.contains(DURATION)) duration = bundle.getFloat(DURATION);
    }

    @Override
    public boolean act() {
        duration-=TICK;
        spend(TICK);
        if (duration <= 0) {
            detach();
        }
        return true;
    }

    @Override
    public String icon() {
        return BuffIndicator.COMBO;
    }

    @Override
    public float iconFadePercent() {
        float time = 0;
        //总时长要和 resetTime() 用同一个天赋（以战养战）来算
        int point=Dungeon.hero.pointsInTalent(Talent.LETHAL_DEFENSE);
        switch (point){
            case 1:
                time=2f;
                break;
            case 2:
                time=3f;
                break;
            case 3:
                time=4f;
                break;
        }
        if (time <= 0) return 0;
        return Math.max(0, (time- duration)/time);
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString((int)duration);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(duration));
    }

}
