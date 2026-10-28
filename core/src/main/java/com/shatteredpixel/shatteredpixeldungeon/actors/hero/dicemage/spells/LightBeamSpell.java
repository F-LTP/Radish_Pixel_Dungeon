package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 光束（特殊学派）：等于+10解离法杖，对目标造成大量魔法伤害。
 */
public class LightBeamSpell extends DiceMageSpell {

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
        return 5;
    }
    @Override
    public String sndImageName() {
        return "beam";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(LightBeamSpell.this, "invalid_target"));
                    return;
                }
                int dmg = Random.IntRange(45, 60) + strBonusDamage(hero); // 近似 +10 法杖
                if (!spendMagic(hero)) return;
                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FORCE, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        target.damage(DamageInfo.magical(dmg, LightBeamSpell.this));
                        if (target.isAlive()) {
                            CellEmitter.center(target.pos).burst(EnergyParticle.FACTORY, 8);
                        }
                        Sample.INSTANCE.play(Assets.Sounds.BLAST);
                    }
                });
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(LightBeamSpell.this, "prompt");
            }
        });
    }
}
