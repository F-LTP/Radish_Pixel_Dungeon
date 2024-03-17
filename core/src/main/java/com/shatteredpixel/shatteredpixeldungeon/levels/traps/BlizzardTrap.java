package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blizzard;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Inferno;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class BlizzardTrap extends Trap {

    {
        color = WHITE;
        shape = GRILL;
    }


    @Override
    public void activate() {
        int baseVolume=15 + Dungeon.depth;
        int centerVolume = baseVolume;
        for (int i : PathFinder.NEIGHBOURS8){
            if (!Dungeon.level.solid[pos+i]){
                GameScene.add( Blob.seed( pos+i, baseVolume, Blizzard.class ) );
            } else {
                centerVolume += baseVolume;
            }
        }
        GameScene.add( Blob.seed( pos, centerVolume, Blizzard.class ) );
        Sample.INSTANCE.play(Assets.Sounds.GAS);
    }
}
