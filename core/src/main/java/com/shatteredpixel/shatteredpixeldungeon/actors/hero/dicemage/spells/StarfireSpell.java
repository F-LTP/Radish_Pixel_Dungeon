package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StarfireBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;

/**
 * 星火（法力学派 L3）：3回合内，视野内怪物受到法术伤害时，每10点伤害为你提供1法力值。冷却50回合。
 */
public class StarfireSpell extends DiceMageSpell {

    private static final float COOLDOWN = 50f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_MANA;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 4;
    }
    @Override
    public String sndImageName() {
        return "flare";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;
        Buff.affect(hero, StarfireBuff.class, StarfireBuff.DURATION);
        startCooldown(hero, COOLDOWN);
        hero.spendAndNext(1f);
    }
}
