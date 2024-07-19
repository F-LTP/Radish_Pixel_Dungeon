package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class GrudgeSprite extends MobSprite {
    public GrudgeSprite(){
        super();

        texture(Assets.Sprites.GRUDGE);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );
        idle = new MovieClip.Animation( 7, true );
        idle.frames( frames, 0,1,2,3,4,5,6,7);

        run = new MovieClip.Animation( 9, true );
        run.frames( frames,  8,9,10,11,12,13);

        attack = new MovieClip.Animation( 9, false );
        attack.frames( frames, 14,15,16);

        die = new MovieClip.Animation( 9, false );
        die.frames( frames, 17,18,19,20,21);

        play(idle);
    }
}
