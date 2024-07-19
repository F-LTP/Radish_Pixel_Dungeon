package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class StoneSpiritSprite extends MobSprite {
    public StoneSpiritSprite(){
        super();

        texture(Assets.Sprites.STONESPIRIT);

        TextureFilm frames = new TextureFilm( texture, 17, 16 );
        idle = new MovieClip.Animation( 3, true );
        idle.frames( frames, 0,1);

        run = new MovieClip.Animation( 9, true );
        run.frames( frames,  2,3,4, 5, 6 ,7,8,9);

        attack = new MovieClip.Animation( 9, false );
        attack.frames( frames, 10,11,12,13);

        die = new MovieClip.Animation( 9, false );
        die.frames( frames, 14,15,16);

        play(idle);
    }
}
