package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Inferno;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class InfernoTrap extends Trap {

    {
        color = ORANGE;
        shape = GRILL;
    }


    @Override
    public void activate() {
        int baseVolume=30 + 2 * Dungeon.depth;
        int centerVolume = baseVolume;
        for (int i : PathFinder.NEIGHBOURS8){
            if (!Dungeon.level.solid[pos+i]){
                GameScene.add( Blob.seed( pos+i, baseVolume, Inferno.class ) );
            } else {
                centerVolume += baseVolume;
            }
        }
        GameScene.add( Blob.seed( pos, centerVolume, Inferno.class ) );
        Sample.INSTANCE.play(Assets.Sounds.GAS);
    }
}
