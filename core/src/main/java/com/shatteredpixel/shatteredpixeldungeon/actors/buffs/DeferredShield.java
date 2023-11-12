package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class DeferredShield extends FlavourBuff{
    {
        actPriority = HERO_PRIO+1;
    }
    int amount = 0;

    private static final String AMOUNT	= "amount";
    @Override
    public void storeInBundle(Bundle bundle){
        super.storeInBundle(bundle);
        bundle.put(AMOUNT,amount);
    }
    @Override
    public void restoreFromBundle(Bundle bundle){
        super.restoreFromBundle(bundle);
        if (bundle.contains(AMOUNT)){
            amount=bundle.getInt(AMOUNT);
        }
        else amount=0;
    }
    @Override
    public void detach() {
        Buff.affect(target, instantBarrier.class).incShield(amount);
        super.detach();
    }
    public void inc(int amt){
        amount+=amt;
    }
    public static class instantBarrier extends Barrier{
        @Override
        public boolean act(){
            spend(TICK);
            detach();
            return true;
        }
    }
}
