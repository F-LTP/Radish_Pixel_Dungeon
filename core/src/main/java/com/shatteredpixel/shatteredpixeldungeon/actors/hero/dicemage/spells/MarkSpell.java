package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MarkDebuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 标记（特殊学派）：造成17-31魔法伤害，并使目标在20回合内受到的伤害额外+7-11。
 */
public class MarkSpell extends DiceMageSpell {

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
        return "mark";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(MarkSpell.this, "invalid_target"));
                    return;
                }
                int dmg = Random.IntRange(17, 31) + strBonusDamage(hero);
                if (!spendMagic(hero)) return;
                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.SHADOW, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        target.damage(DamageInfo.magical(dmg, MarkSpell.this));
                        if (target.isAlive()) {
                            Buff.prolong(target, MarkDebuff.class, 20f);
                            CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 10);
                        }
                        Sample.INSTANCE.play(Assets.Sounds.CURSED);
                    }
                });
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(MarkSpell.this, "prompt");
            }
        });
    }
}
