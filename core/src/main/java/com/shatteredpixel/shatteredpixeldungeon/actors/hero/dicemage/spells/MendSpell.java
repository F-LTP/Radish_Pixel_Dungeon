package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 愈合（紧急学派 L1）：将血量设置为50，冷却500回合；前往新区域前设置值衰减3。
 */
public class MendSpell extends DiceMageSpell {

    private static final float COOLDOWN = 500f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_EMERGENCY;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "mend";
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
        mp.decayHealValue();
        startCooldown(hero, COOLDOWN);
        int healed = hero.HP - oldHP;
        if (healed > 0) {
        } else {
            GLog.i(Messages.get(this, "no_heal", Math.max(0, mp.healValue())));
        }
        hero.spendAndNext(1f);
    }
}
