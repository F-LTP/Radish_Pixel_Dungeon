package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

public class GodsPossessionBuff extends FlavourBuff {

    public static final float DURATION = 30f;

    {
        type = buffType.POSITIVE;
    }

    @Override
    public boolean attachTo(Char target) {
        // flush
        Buff buff = target.buff(Bless.class);
        if(buff != null)
            buff.detach();

        Buff.affect(target, Bless.class,DURATION);
        if(target instanceof Hero){
            // flush
            buff = target.buff(ChampionHero.Blazing.class);
            if(buff != null)
                buff.detach();
            buff = target.buff(ChampionHero.Projecting.class);
            if(buff != null)
                buff.detach();
            buff = target.buff(ChampionHero.AntiMagic.class);
            if(buff != null)
                buff.detach();
            buff = target.buff(ChampionHero.Giant.class);
            if(buff != null)
                buff.detach();
            buff = target.buff(ChampionHero.Blessed.class);
            if(buff != null)
                buff.detach();
            buff = target.buff(ChampionHero.Growing.class);
            if(buff != null)
                buff.detach();
            buff = target.buff(ChampionHero.BattleRector.class);
            if(buff != null)
                buff.detach();

            for(int i=0;i<7;i++){
                // buff
                ChampionHero.getElite((Hero) target,i,DURATION);
            }
        }
        return super.attachTo(target);
    }

}
