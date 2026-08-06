package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnDRegeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class SootheSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        return 4;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        return hero != null && hero.pointsInTalent(Talent.LEARN_SOOTHE) > 0 && super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_SOOTHE);
        if (points <= 0) return;

        int healAmount = points == 1 ? 10 : (points == 2 ? 15 : 20);
        int regenAmount = points == 1 ? 1 : 2;
        int regenDuration = points == 3 ? 20 : 15;

        int allyCount = 0;
        if (Dungeon.level.heroFOV[hero.pos]) {
            hero.HP = Math.min(hero.HP + healAmount, hero.HT);
            Buff.affect(hero, SnDRegeneration.class).set(regenAmount, regenDuration);
            CellEmitter.center(hero.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
            allyCount++;
        }

        for (Char ch : Dungeon.level.mobs) {
            if (ch.alignment == Char.Alignment.ALLY && Dungeon.level.heroFOV[ch.pos]) {
                ch.HP = Math.min(ch.HP + healAmount, ch.HT);
                Buff.affect(ch, SnDRegeneration.class).set(regenAmount, regenDuration);
                CellEmitter.center(ch.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
                allyCount++;
            }
        }

        if (allyCount == 0) {
            GLog.w(Messages.get(this, "no_ally"));
            return;
        }

        if (!spendMagic(hero)) return;
        GLog.p(Messages.get(this, "cast", allyCount, healAmount, regenAmount, regenDuration));
        hero.spendAndNext(1f);
    }
}
