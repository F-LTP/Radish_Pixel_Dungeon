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
    protected boolean canTargetUnseenCell() {
        return true;
    }

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

                int width = Dungeon.level.width();
                int cx = cell % width, cy = cell / width;

                // 区域视野
                // Dungeon.observe / Level.updateFieldOfView 按 RevealedArea.pos 周围 3x3 直接取数组下标、不查越界，
                // 选到地图最外一圈（选格时点得到）会数组越界闪退；视野中心往里收到内圈，伤害范围仍以选中的格子为中心
                RevealedArea a = Buff.affect(hero, RevealedArea.class, 15f);
                a.depth = Dungeon.depth;
                // 上一次闪耀的视野还没结束时复用的是同一个 buff，分支要跟着这一次施法走
                a.branchId = Dungeon.branchId;
                int vx = Math.max(1, Math.min(width - 2, cx));
                int vy = Math.max(1, Math.min(Dungeon.level.height() - 2, cy));
                a.pos = vx + vy * width;
                Dungeon.observe();

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
