package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class ClusteredSkeletonSprite extends MobSprite {
    public ClusteredSkeletonSprite() {
        super();

        texture(Assets.Sprites.CLUSTERED_SKELETON);

        TextureFilm frames = new TextureFilm( texture, 18, 17 );
        idle = new Animation( 3, true );
        idle.frames( frames, 0,1,2,3);

        run = new Animation( 8, true );
        run.frames( frames,  4, 5, 6 ,7,8);

        attack = new Animation( 11, false );
        attack.frames( frames, 9,10,11,12);

        die = new Animation( 11, false );
        die.frames( frames, 13,14,15,16,17,18);

        play(idle);
    }
}
