package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class BurstSpell extends DiceMageSpell {

    private static final int RANGE = 3;
    private static final int SHIELD_AMOUNT = 15;

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "burst";
    }



    @Override
    public Talent iconTalent() {
        //TODO Change this icon
        return Talent.SPELL_EMPOWER;
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;

                Char target = Actor.findChar(cell);
                if (target == null || (target != hero && !isValidAlly(target) && !isValidEnemy(target))) {
                    GLog.w(Messages.get(BurstSpell.this, "invalid_target"));
                    return;
                }
                if (target != hero && Dungeon.level.distance(hero.pos, target.pos) > RANGE) {
                    GLog.w(Messages.get(BurstSpell.this, "out_of_range"));
                    return;
                }

                if (!spendMagic(hero)) return;

                if (target == hero || isValidAlly(target)) {
                    Buff.affect(target, Barrier.class).incShield(SHIELD_AMOUNT);
                    if (target.sprite != null) {
                        target.sprite.showStatusWithIcon(CharSprite.POSITIVE,
                                Integer.toString(SHIELD_AMOUNT), FloatingText.SHIELDING);
                    }
                    CellEmitter.center(target.pos).burst(SparkParticle.FACTORY, 5);
                    GLog.p(Messages.get(BurstSpell.this, "shield", SHIELD_AMOUNT));
                } else {
                    int damage = Random.IntRange(14, 21) + strBonusDamage(hero);
                    target.damage(DamageInfo.fire(damage, BurstSpell.this));
                    if (target.isAlive()) {
                        CellEmitter.center(target.pos).burst(FlameParticle.FACTORY, 8);
                    }
                    GLog.p(Messages.get(BurstSpell.this, "damage", damage));
                }
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(BurstSpell.this, "prompt");
            }
        });
    }
}
