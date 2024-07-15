package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class DogSprite extends MobSprite {
    public DogSprite(){
        super();

        texture(Assets.Sprites.DOG);

        TextureFilm frames = new TextureFilm( texture, 22, 16 );
        idle = new Animation( 4, true );
        idle.frames( frames, 0,1,2,3);

        run = new Animation( 9, true );
        run.frames( frames,  4, 5, 6 ,7,8,9);

        attack = new Animation( 7, false );
        attack.frames( frames, 10,11,12,13);

        die = new Animation( 7, false );
        die.frames( frames, 14,15,16,17,18);

        play(idle);
    }
}
