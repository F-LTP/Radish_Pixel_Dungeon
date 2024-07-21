package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class DM175_Sprite extends MobSprite {
    public DM175_Sprite(){
        super();

        texture(Assets.Sprites.DM175);

        TextureFilm frames = new TextureFilm( texture, 18, 15 );
        idle = new Animation( 7, true );
        idle.frames( frames, 0,1,2,3);

        run = new Animation( 9, true );
        run.frames( frames,  4, 5, 6 ,7);

        attack = new Animation( 11, false );
        attack.frames( frames, 8,9,10,11);

        die = new Animation( 9, false );
        die.frames( frames, 12,13,14,15,16,17,18,19);

        play(idle);
    }
}
