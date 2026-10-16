package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class FrogSprite extends MobSprite {

    public FrogSprite() {
        super();

        texture( Assets.Sprites.FROG );

        TextureFilm frames = new TextureFilm( texture, 16, 14 );

        idle = new Animation( 9, true );
        idle.frames( frames, 0,0,0,0,0,0,0,0,0,0,1,2,3
        );

        run = new Animation( 11, true );
        run.frames( frames, 4,5,6,7,8,9 );

        attack = new Animation( 11, false );
        attack.frames( frames, 10,11,12,13 );

        die = new Animation( 11, false );
        die.frames( frames, 14,15,16,17 );

        play( idle );
    }
}
