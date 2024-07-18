package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class DrakeSprite extends MobSprite {
    public DrakeSprite(){
        super();

        texture(Assets.Sprites.DRAKE);

        TextureFilm frames = new TextureFilm( texture, 24,10);
        idle = new MovieClip.Animation( 3, true );
        idle.frames( frames, 0,1,2,3);

        run = new MovieClip.Animation( 7, true );
        run.frames( frames,  4, 5, 6 ,7,8,9);

        attack = new MovieClip.Animation( 9, false );
        attack.frames( frames, 10,11,12,13);

        die = new MovieClip.Animation( 7, false );
        die.frames( frames, 14,15,16,17);

        play(idle);
    }
}
