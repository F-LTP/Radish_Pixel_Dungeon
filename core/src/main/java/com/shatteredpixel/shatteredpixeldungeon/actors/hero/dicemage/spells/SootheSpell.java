package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicSootheRegen;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.utils.Random;

/**
 * 抚慰（医疗学派 L3）：视野内友军恢复5-9生命，且自然生命恢复提升0.2，持续500回合或直到下层。
 */
public class SootheSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_MEDICAL;
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
        return "soothe";
    }

    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int heal = Random.IntRange(5, 9);
        int count = 0;
        for (Char ch : Dungeon.level.mobs) {
            if (ch.alignment != Char.Alignment.ALLY) continue;
            if (!Dungeon.level.heroFOV[ch.pos]) continue;
            ch.HP = Math.min(ch.HT, ch.HP + heal);
            MagicSootheRegen.apply(ch);
            CellEmitter.center(ch.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
            count++;
        }
        hero.HP = Math.min(hero.HT, hero.HP + heal);
        MagicSootheRegen.apply(hero);
        CellEmitter.center(hero.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
        count++;

        hero.spendAndNext(1f);
    }
}
