package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;

/**
 * 热量（特殊学派）：驱除等量于护盾值/3 回合数层数的debuff。
 */
public class HeatSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_SPECIAL;
    }

    @Override
    public int level() {
        return DiceMageSchools.specialSlot(getClass());
    }

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "heat";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int limit = hero.shielding() / 3;
        int removed = 0;
        for (Buff b : hero.buffs().toArray(new Buff[0])) {
            if (removed >= limit) break;
            if (b.type == Buff.buffType.NEGATIVE && !(b instanceof Hunger)) {
                b.detach();
                removed++;
            }
        }
        CellEmitter.center(hero.pos).burst(Speck.factory(Speck.HEALING), 6);
        hero.spendAndNext(1f);
    }
}
