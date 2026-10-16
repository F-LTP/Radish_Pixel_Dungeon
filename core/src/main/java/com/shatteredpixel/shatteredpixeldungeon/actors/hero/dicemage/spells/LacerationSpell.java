package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 割破（血液学派 L1）：自身受8-12伤害，对视野内最上方随机怪物造成17-31伤害。冷却10回合。
 */
public class LacerationSpell extends DiceMageSpell {

    private static final float COOLDOWN = 10f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLOOD;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 1;
    }
    @Override
    public String sndImageName() {
        return "slice";
    }



    @Override
    protected void onCast(Hero hero) {
        int topY = Integer.MAX_VALUE;
        ArrayList<Mob> topMobs = new ArrayList<>();
        for (Mob mob : Dungeon.level.mobs) {
            if (!Dungeon.level.heroFOV[mob.pos] || mob.alignment != Char.Alignment.ENEMY) continue;
            int y = mob.pos / Dungeon.level.width();
            if (y < topY) {
                topY = y;
                topMobs.clear();
                topMobs.add(mob);
            } else if (y == topY) {
                topMobs.add(mob);
            }
        }
        if (topMobs.isEmpty()) {
            GLog.w(Messages.get(LacerationSpell.this, "no_enemy"));
            return;
        }
        if (!spendMagic(hero)) return;

        int selfDmg = Random.IntRange(8, 12);
        hero.damage(DamageInfo.physicalNoArmor(selfDmg, LacerationSpell.this));
        if (hero.sprite != null) hero.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(selfDmg));

        Mob target = topMobs.get(Random.Int(topMobs.size()));
        int dmg = Random.IntRange(17, 31) + strBonusDamage(hero);
        target.damage(DamageInfo.physicalNoArmor(dmg, LacerationSpell.this));
        CellEmitter.center(target.pos).burst(BloodParticle.BURST, 10);
        startCooldown(hero, COOLDOWN);
        hero.spendAndNext(1f);
    }
}
