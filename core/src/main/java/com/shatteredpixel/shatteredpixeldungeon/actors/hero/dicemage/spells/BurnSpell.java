package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

/**
 * 燃烧（血液学派 L2）：自身受8-12伤害，对视野内所有怪物造成7-14火焰伤害。
 */
public class BurnSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLOOD;
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
        return "burn";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int selfDmg = Random.IntRange(8, 12);
        hero.damage(DamageInfo.physicalNoArmor(selfDmg, BurnSpell.this));
        if (hero.sprite != null) hero.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(selfDmg));

        int hit = 0;
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (!Dungeon.level.heroFOV[mob.pos] || mob.alignment != Char.Alignment.ENEMY) continue;
            int dmg = Random.IntRange(7, 14) + strBonusDamage(hero);
            mob.damage(DamageInfo.fire(dmg, BurnSpell.this));
            if (mob.isAlive()) {
                CellEmitter.center(mob.pos).burst(FlameParticle.FACTORY, 6);
            }
            hit++;
        }
        hero.spendAndNext(1f);
    }
}
