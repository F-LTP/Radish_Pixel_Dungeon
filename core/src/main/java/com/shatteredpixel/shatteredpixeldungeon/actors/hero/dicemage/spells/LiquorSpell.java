package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class LiquorSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        return hero != null && hero.pointsInTalent(Talent.LEARN_LIQUOR) > 0 && super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;

                Char target = Actor.findChar(cell);
                if (target != hero && !isValidAlly(target)) {
                    GLog.w(Messages.get(LiquorSpell.this, "invalid_target"));
                    return;
                }

                int points = hero.pointsInTalent(Talent.LEARN_LIQUOR);
                int shieldAmount = points == 1 ? 25 : (points == 2 ? 35 : 45);

                if (!spendMagic(hero)) return;

                for (Buff b : target.buffs()) {
                    if (b.type == Buff.buffType.NEGATIVE && !(b instanceof Hunger)) {
                        b.detach();
                    }
                }

                Buff.affect(target, Barrier.class).incShield(shieldAmount);
                CellEmitter.center(target.pos).burst(SparkParticle.FACTORY, 5);
                GLog.p(Messages.get(LiquorSpell.this, "cast", shieldAmount));
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(LiquorSpell.this, "prompt");
            }
        });
    }
}
