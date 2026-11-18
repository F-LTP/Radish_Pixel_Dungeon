package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

public class GodsPossessionBuff extends FlavourBuff {

    public static final float DURATION = 30f;

    {
        type = buffType.POSITIVE;
    }

    // 读档恢复时 attachTo 也会被调用，用它区分「新挂上」和「从存档读回来」
    private boolean restored = false;

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        restored = true;
    }

    @Override
    public boolean attachTo(Char target) {
        // 祝福和七个精英词条各自都会存档，读档时它们按自己存下的剩余时间恢复。
        // 这里如果照样重发一遍，每读一次档就把它们全部刷回 30 回合。
        if (restored){
            restored = false;
            return super.attachTo(target);
        }

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
