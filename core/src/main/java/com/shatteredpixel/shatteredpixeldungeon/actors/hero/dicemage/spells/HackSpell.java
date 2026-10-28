package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;

/**
 * 劈砍（刀刃学派 L2，主动）：朝瞄准方向挥出 60° 扇形、最长 7 格的斩击，
 * 命中范围内所有敌人，造成武器伤害+力量加成，并触发武器附魔。
 * 必中，斩击不能穿过墙壁。
 */
public class HackSpell extends DiceMageSpell {

    private static final int MAX_RANGE = 7;
    private static final float CONE_DEGREES = 60f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLADES;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public String sndImageName() {
        return "hack";
    }

    @Override
    protected void onCast(final Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                if (!spendMagic(hero)) return;

                Ballistica core = new Ballistica(hero.pos, cell, Ballistica.WONT_STOP);
                int dist = Math.min(core.dist, MAX_RANGE);
                ConeAOE cone = new ConeAOE(core, dist, CONE_DEGREES, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);

                KindOfWeapon weapon = hero.belongings.weapon;
                int hit = 0;
                for (int c : cone.cells) {
                    if (c == hero.pos || !Dungeon.level.heroFOV[c]) continue;
                    Char ch = Actor.findChar(c);
                    if (ch == null || ch == hero || ch.alignment != Char.Alignment.ENEMY || !ch.isAlive()) continue;

                    int dmg = (weapon != null ? weapon.damageRoll(hero) : 0) + strBonusDamage(hero);
                    dmg = Math.max(1, dmg);
                    if (weapon != null) dmg = weapon.proc(hero, ch, dmg);
                    ch.damage(DamageInfo.physicalNoArmor(dmg, HackSpell.this));
                    if (ch.isAlive()) {
                        CellEmitter.center(ch.pos).burst(BlastParticle.FACTORY, 6);
                        CellEmitter.center(ch.pos).burst(Speck.factory(Speck.RED_LIGHT), 2);
                    }
                    hit++;
                }

                if (hit > 0) applyStrShield(hero);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(HackSpell.this, "prompt");
            }
        });
    }
}
