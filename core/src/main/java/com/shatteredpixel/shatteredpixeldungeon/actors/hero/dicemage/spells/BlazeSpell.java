package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class BlazeSpell extends DiceMageSpell {

    @Override
    public int mpCost() {
        return 6;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        return hero != null && hero.pointsInTalent(Talent.LEARN_BLAZE) > 0 && super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;

                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(BlazeSpell.this, "invalid_target"));
                    return;
                }

                int points = hero.pointsInTalent(Talent.LEARN_BLAZE);
                int damage = points == 1 ? 80 : (points == 2 ? 105 : 130);

                if (!spendMagic(hero)) return;

                if (target.sprite != null) {
                    target.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(damage));
                }
                target.damage(damage, DamageType.FIRE);
                if (target.isAlive()) {
                    CellEmitter.center(target.pos).burst(FlameParticle.FACTORY, 10);
                }
                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        Sample.INSTANCE.play(Assets.Sounds.BLAST);
                    }
                });
                GLog.p(Messages.get(BlazeSpell.this, "cast", damage));
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(BlazeSpell.this, "prompt");
            }
        });
    }
}
