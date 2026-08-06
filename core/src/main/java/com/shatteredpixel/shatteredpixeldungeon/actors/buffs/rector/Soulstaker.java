package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;


import java.util.Collections;
import java.util.Set;

public class Soulstaker extends FlavourBuff {

    public static final float DURATION = 30f;

    private int prevTargetHp = 0;
    private int currTargetHp = 0;

    private float left = 0;

    private static final String PREV_HP	= "prev_hp";
    private static final String LEFT	= "left";

    {
        type = buffType.NEUTRAL;
        announced = true;
    }


    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( PREV_HP, target.HP );
        bundle.put( LEFT, left );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        prevTargetHp = bundle.getInt(PREV_HP);
        left = bundle.getFloat(LEFT);
    }

    @Override
    public boolean attachTo(Char target) {
        prevTargetHp = target.HP;

        left = DURATION;

        return super.attachTo(target);
    }

    @Override
    public boolean act() {
        currTargetHp = target.HP;
//        GLog.n("curr hp:"+currTargetHp+";"+"prevHp:"+prevTargetHp + ";Tick:" + left);
        int healTick = currTargetHp - prevTargetHp;
        healTick = Math.max(healTick,0);
        prevTargetHp = currTargetHp;

        Set<Mob> mobs = Collections.synchronizedSet(Dungeon.level.mobs);
        for(Mob mob:mobs){
            if(target.fieldOfView[mob.pos] && mob.alignment == Char.Alignment.ENEMY && healTick > 0){
                mob.damage(healTick,Soulstaker.class);
            }
        }

        spend( TICK );
        left -= TICK;

        if((DURATION - cooldown()) / DURATION <= 1){
            detach();
        }

        return true;
    }

    @Override
    public String icon() {
        return BuffIndicator.CORRUPT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0f, 0.75f, 1f);
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - cooldown()) / DURATION);
    }

    @Override
    public void detach() {
        // Sacrifice heal
        if(target instanceof Hero){
            if(((Hero)target).pointsInTalent(Talent.SACRIFICE) > 1){
                Buff.affect(target, Healing.class).setHeal(((Hero) target).lvl * 2,1,0);
            } else {
                Buff.affect(target, Healing.class).setHeal(((Hero) target).lvl,1,0);
            }
        }

        super.detach();
    }
}
