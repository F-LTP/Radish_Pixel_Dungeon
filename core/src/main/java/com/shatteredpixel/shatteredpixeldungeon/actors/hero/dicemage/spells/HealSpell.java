package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class HealSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        return 2;
    }

    @Override
    protected void onCast(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return;

        int healValue = mp.healValue();
        if (healValue <= 0) {
            GLog.w(Messages.get(this, "exhausted"));
            return;
        }

        if (!spendMagic(hero)) return;

        int oldHP = hero.HP;
        hero.HP = Math.min(healValue, hero.HT);
        CellEmitter.center(hero.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
        int healed = hero.HP - oldHP;
        mp.decreaseHealValue();

        if (healed > 0) {
            GLog.p(Messages.get(this, "cast", healed, Math.max(0, mp.healValue())));
        } else {
            GLog.i(Messages.get(this, "no_heal", Math.max(0, mp.healValue())));
        }

        hero.spendAndNext(1f);
    }
}
