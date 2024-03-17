package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Bundle;

public class MoveCount extends Buff{

    {
        type = buffType.POSITIVE;

        //acts after other buff
        actPriority = BUFF_PRIO-1;
    }
    private int moveCounts = 0;
    @Override
    public boolean act(){
        moveCounts=0;
        spend(TICK);
        return true;
    }
    public void gainStack(){
        moveCounts++;
    }
    public float chargeMultiplier(Hero hero){
        if (!hero.hasTalent(Talent.KINETIC_ENERGY) || moveCounts<1) return 1f;
        return Math.max(0.25f* hero.pointsInTalent(Talent.KINETIC_ENERGY)*moveCounts,1f);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("move_counts",moveCounts);
    }


    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains("move_counts")) moveCounts=bundle.getInt("move_counts");
    }
}
