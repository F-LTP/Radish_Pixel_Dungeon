package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class BlazeSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_FIRE;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 6;
    }

    @Override
    public String sndImageName() {
        return "blaze";
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;

                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || target.buff(Burning.class) == null) {
                    GLog.w(Messages.get(BlazeSpell.this, "invalid_target"));
                    return;
                }

                int damage = Random.IntRange(100, 150) + strBonusDamage(hero);

                if (!spendMagic(hero)) return;

                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        CellEmitter.center(target.pos).burst(BlastParticle.FACTORY, 10);
                        WandOfBlastWave.BlastWave.blast(target.pos);
                        target.damage(DamageInfo.fire(damage, BlazeSpell.this));

                        // 引爆：目标自身的引燃立刻结束
                        Buff.detach(target, Burning.class);

                        // 扩散火焰到周围 5x5：敌人引燃 + 地面点火（目标所在格除外）
                        int width = Dungeon.level.width();
                        int cx = target.pos % width, cy = target.pos / width;
                        for (int x = cx - 2; x <= cx + 2; x++) {
                            for (int y = cy - 2; y <= cy + 2; y++) {
                                int pos = y * width + x;
                                if (x < 0 || x >= width || pos < 0 || pos >= Dungeon.level.length()) continue;
                                if (pos == target.pos || Dungeon.level.solid[pos]) continue;

                                GameScene.add(Blob.seed(pos, 4, Fire.class));

                                Char ch = Actor.findChar(pos);
                                if (ch != null) {
                                    Buff.affect(ch, Burning.class).reignite(ch);
                                }
                            }
                        }

                        if (target.isAlive()) {
                            CellEmitter.center(target.pos).burst(FlameParticle.FACTORY, 10);
                        }
                        Sample.INSTANCE.play(Assets.Sounds.BLAST);
                    }
                });
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(BlazeSpell.this, "prompt");
            }
        });
    }
}
