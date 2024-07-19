package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class TorturerSprite  extends MobSprite {
    public TorturerSprite(){
        super();

        texture(Assets.Sprites.TORTURER);

        TextureFilm frames = new TextureFilm( texture, 22, 17);
        idle = new MovieClip.Animation( 3, true );
        idle.frames( frames, 0,1);

        run = new MovieClip.Animation( 7, true );
        run.frames( frames,  2,3,4,5);

        attack = new MovieClip.Animation( 11, false );
        attack.frames( frames, 6,7,8,9,10);

        die = new MovieClip.Animation( 11, false );
        die.frames( frames, 11,12,13);

        play(idle);
    }
}
