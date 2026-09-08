package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 诛杀（咒法学派 L1）：杀死视野内1个生命值不高于 30+力量 的敌人，冷却15回合。
 */
public class ExecuteSpell extends DiceMageSpell {

    private static final float COOLDOWN = 15f;
    private static final int BASE_HP = 30;

    @Override
    public Talent school() {
        return Talent.SCHOOL_CONJURATION;
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
        return "slay";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || !(target instanceof Mob)) {
                    GLog.w(Messages.get(ExecuteSpell.this, "invalid_target"));
                    return;
                }
                if (!Dungeon.level.heroFOV[target.pos]) {
                    GLog.w(Messages.get(ExecuteSpell.this, "not_in_view"));
                    return;
                }
                if (target.HP > BASE_HP + hero.STR()) {
                    GLog.w(Messages.get(ExecuteSpell.this, "out_of_range_hp", target.HP));
                    return;
                }
                if (!spendMagic(hero)) return;

                target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(ExecuteSpell.this, "executed"));
                // 真伤，无视护甲/护盾，确保处决
                target.damage(new DamageInfo(target.HP, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.TRUE, hero, null, ExecuteSpell.this));
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 8);
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(ExecuteSpell.this, "prompt");
            }
        });
    }
}
