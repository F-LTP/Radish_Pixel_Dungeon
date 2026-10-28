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
 * 更新（紧急学派 L2）：将血量设置为40，冷却250回合；前往新区域前设置值衰减2。
 */
public class RefreshSpell extends DiceMageSpell {

    private static final float COOLDOWN = 250f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_EMERGENCY;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 1;
    }
    @Override
    public String sndImageName() {
        return "renew";
    }



    @Override
    protected void onCast(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return;
        int refreshValue = mp.refreshValue();
        if (refreshValue <= 0) {
            GLog.w(Messages.get(this, "exhausted"));
            return;
        }
        if (!spendMagic(hero)) return;

        int oldHP = hero.HP;
        hero.HP = Math.min(refreshValue, hero.HT);
        CellEmitter.center(hero.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
        mp.decayRefreshValue();
        startCooldown(hero, COOLDOWN);
        int healed = hero.HP - oldHP;
        if (healed > 0) {
        } else {
            GLog.i(Messages.get(this, "no_heal", Math.max(0, mp.refreshValue())));
        }
        hero.spendAndNext(1f);
    }
}
