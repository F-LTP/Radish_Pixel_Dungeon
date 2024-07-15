package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class MayflySprite extends MobSprite {
    public MayflySprite(){
        super();

        texture(Assets.Sprites.MAYFLY);

        TextureFilm frames = new TextureFilm( texture, 20, 16 );
        idle = new Animation( 7, true );
        idle.frames( frames, 0,1,2,3);

        run = new Animation( 7, true );
        run.frames( frames, 4, 5, 6,7 );

        attack = new Animation( 7, false );
        attack.frames( frames, 8, 9,10);

        die = new Animation( 7, false );
        die.frames( frames, 11, 12,13,14,15 );

        play(idle);
    }
}
