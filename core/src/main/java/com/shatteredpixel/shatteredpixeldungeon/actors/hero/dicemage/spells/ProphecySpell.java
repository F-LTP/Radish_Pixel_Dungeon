package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;

/**
 * 预知（法力学派 L2）：消耗2魔力点，获得10回合的魔能透视。
 */
public class ProphecySpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_MANA;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 2;
    }

    @Override
    public String sndImageName() {
        return "foretell";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;
        Buff.prolong(hero, MagicalSight.class, 20f);
        hero.spendAndNext(0f);
    }
}
