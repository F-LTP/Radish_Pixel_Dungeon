package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.watabou.utils.Random;

/**
 * 闪耀（特殊学派）：可选战争迷雾任意点造成30-50光伤害和失明，获区域视野，周围5格怪物失明。
 */
public class ShineSpell extends DiceMageSpell {

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
        return 4;
    }
    @Override
    public String sndImageName() {
        return "glow";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                if (!spendMagic(hero)) return;

                // 区域视野
                RevealedArea a = Buff.affect(hero, RevealedArea.class, 15f);
                a.depth = Dungeon.depth;
                a.pos = cell;
                Dungeon.observe();

                int width = Dungeon.level.width();
                int cx = cell % width, cy = cell / width;
                int hit = 0;
                for (int x = cx - 5; x <= cx + 5; x++) {
                    for (int y = cy - 5; y <= cy + 5; y++) {
                        int pos = y * width + x;
                        if (x < 0 || x >= width || pos < 0 || pos >= Dungeon.level.length()) continue;
                        if (Math.max(Math.abs(x - cx), Math.abs(y - cy)) > 5) continue;
                        Char ch = Actor.findChar(pos);
                        if (ch == null || ch.alignment != Char.Alignment.ENEMY) continue;
                        int dmg = Random.IntRange(30, 50) + strBonusDamage(hero);
                        ch.damage(DamageInfo.magical(dmg, ShineSpell.this));
                        if (ch.isAlive()) {
                            Buff.affect(ch, Blindness.class, 5f);
                            CellEmitter.center(ch.pos).burst(EnergyParticle.FACTORY, 8);
                        }
                        hit++;
                    }
                }
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(ShineSpell.this, "prompt");
            }
        });
    }
}
