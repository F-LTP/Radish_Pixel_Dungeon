package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class SeaShoreSprite extends MobSprite{
    public SeaShoreSprite(){
        super();
        texture(Assets.Sprites.NEW_NPC);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 6, true );
        idle.frames( frames, 80,81,82,83,84,85,86,87,88,87,86,85,84,83,82,81,89,89,89,90,90,90 );

        run = new Animation( 20, true );
        run.frames( frames, 80 );

        die = new Animation( 20, false );
        die.frames( frames, 80,92,91 );
        play( idle );
    }
}
