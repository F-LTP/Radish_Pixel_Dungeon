package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 切割（刀刃学派 L1）：视野内所有敌人受到7-11物理伤害，
 * 附加33%武器伤害与力量加成，并触发武器附魔。
 */
public class CutSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLADES;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public String sndImageName() {
        return "cut";
    }

    @Override
    protected void onCast(Hero hero) {
        int enemyCount = 0;
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                enemyCount++;
            }
        }

        if (enemyCount == 0) {
            GLog.w(Messages.get(this, "no_enemy"));
            return;
        }

        if (!spendMagic(hero)) return;

        KindOfWeapon weapon = hero.belongings.weapon;
        int weaponRoll = weapon != null ? weapon.damageRoll(hero) : 0;
        int weaponBonus = weaponRoll / 3;

        int hit = 0;
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                int dmg = Random.IntRange(7, 11) + weaponBonus + strBonusDamage(hero);
                dmg = Math.max(1, dmg);
                if (weapon != null) dmg = weapon.proc(hero, mob, dmg);
                mob.damage(DamageInfo.physicalNoArmor(dmg, CutSpell.this));
                if (mob.isAlive()) {
                    CellEmitter.center(mob.pos).burst(BlastParticle.FACTORY, 6);
                    CellEmitter.center(mob.pos).burst(Speck.factory(Speck.RED_LIGHT), 2);
                }
                hit++;
            }
        }

        if (hit > 0) applyStrShield(hero);
        hero.spendAndNext(1f);
    }
}
