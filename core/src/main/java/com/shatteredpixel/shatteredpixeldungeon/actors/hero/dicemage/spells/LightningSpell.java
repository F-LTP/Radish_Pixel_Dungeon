package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 电闪（咒法学派 L2）：弹道雷电，造成15-25闪电伤害+当前魔力值 * 2加成，冷却50回合。
 */
public class LightningSpell extends DiceMageSpell {

    private static final float COOLDOWN = 50f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_CONJURATION;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "bolt";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || !(target instanceof Mob)) {
                    GLog.w(Messages.get(LightningSpell.this, "invalid_target"));
                    return;
                }
                if (!spendMagic(hero)) return;

                MagicPoint mp = hero.buff(MagicPoint.class);
                int mpBonus = mp != null ? mp.getIntPoints() : 0;
                final int dmg = Random.IntRange(15, 25) + mpBonus * 2 + strBonusDamage(hero);

                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.STAR, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        target.damage(DamageInfo.lightning(dmg, LightningSpell.this));
                        CellEmitter.center(target.pos).burst(SparkParticle.FACTORY, 10);
                        Sample.INSTANCE.play(Assets.Sounds.ZAP);
                    }
                });
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(LightningSpell.this, "prompt");
            }
        });
    }
}
