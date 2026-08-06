package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class MiasmaSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        return hero != null && hero.pointsInTalent(Talent.LEARN_MIASMA) > 0 && super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;

                int points = hero.pointsInTalent(Talent.LEARN_MIASMA);
                int radius = points == 1 ? 1 : (points == 2 ? 2 : 3);
                int baseDamage = 3;
                int width = Dungeon.level.width();
                int centerX = cell % width;
                int centerY = cell / width;
                int hitCount = 0;

                if (!spendMagic(hero)) return;

                for (int x = centerX - radius; x <= centerX + radius; x++) {
                    for (int y = centerY - radius; y <= centerY + radius; y++) {
                        if (x < 0 || x >= width) continue;
                        int pos = y * width + x;
                        if (pos < 0 || pos >= Dungeon.level.length()) continue;

                        Char ch = Actor.findChar(pos);
                        if (ch != null) {
                            ch.damage(baseDamage, MiasmaSpell.this);
                            if (ch.isAlive()) {
                                CellEmitter.center(ch.pos).burst(PoisonParticle.SPLASH, 8);
                                Buff.affect(ch, Poison.class).set(baseDamage * 4f);
                            }
                            hitCount++;
                        }
                    }
                }

                GLog.p(Messages.get(MiasmaSpell.this, "cast", hitCount));
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(MiasmaSpell.this, "prompt");
            }
        });
    }
}
