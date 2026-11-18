package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * 弹指（物理学派 L2）：贴身必中，造成8-16伤害（对满血敌人翻倍）+力量加成，击退100格。冷却10回合。
 */
public class FlickSpell extends DiceMageSpell {

    private static final float COOLDOWN = 10f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_PHYSICAL;
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
        return "flick";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(FlickSpell.this, "invalid_target"));
                    return;
                }
                boolean adjacent = false;
                for (int i : PathFinder.NEIGHBOURS8) {
                    if (target.pos == hero.pos + i) adjacent = true;
                }
                if (!adjacent) {
                    GLog.w(Messages.get(FlickSpell.this, "not_adjacent"));
                    return;
                }
                if (!spendMagic(hero)) return;

                int dmg = Random.IntRange(8, 16) + strBonusDamage(hero);
                if (target.HP >= target.HT) dmg *= 2; // 满血翻倍
                target.damage(DamageInfo.physicalNoArmor(dmg, FlickSpell.this));
                CellEmitter.center(target.pos).burst(BlastParticle.FACTORY, 6);
                applyStrShield(hero);
                // 击退100格，方向为远离玩家
                WandOfBlastWave.throwChar(target, new Ballistica(target.pos, target.pos + (target.pos - hero.pos), Ballistica.MAGIC_BOLT),
                        100, false, true, FlickSpell.this);
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(FlickSpell.this, "prompt");
            }
        });
    }
}
