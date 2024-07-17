package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class JailerSprite extends MobSprite {
    public JailerSprite(){
        super();

        texture(Assets.Sprites.JAILER);

        TextureFilm frames = new TextureFilm( texture, 22, 16 );
        idle = new MovieClip.Animation( 1, true );
        idle.frames( frames, 0,1);

        run = new MovieClip.Animation( 9, true );
        run.frames( frames,  2,3,4,5,6,7);

        attack = new MovieClip.Animation( 7, false );
        attack.frames( frames, 8,9,10);

        die = new MovieClip.Animation( 7, false );
        die.frames( frames, 11,12,13);

        play(idle);
    }

}
