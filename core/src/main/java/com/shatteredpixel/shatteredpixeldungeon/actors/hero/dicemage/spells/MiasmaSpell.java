package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.MiasmaGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

/**
 * 瘴气（自然学派 L3）：在目标格喷出浓烈瘴气（体积80，相当于+3腐蚀法杖），
 * 瘴气每回合造成毒气瓶毒素伤害并为敌人叠加2层中毒。
 */
public class MiasmaSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_NATURE;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public String sndImageName() {
        return "miasma";
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                if (!spendMagic(hero)) return;

                Blob gas = Blob.seed(cell, 80, MiasmaGas.class);
                GameScene.add(gas);
                CellEmitter.get(cell).burst(PoisonParticle.SPLASH, 10);
                Sample.INSTANCE.play(Assets.Sounds.GAS);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(MiasmaSpell.this, "prompt");
            }
        });
    }
}
