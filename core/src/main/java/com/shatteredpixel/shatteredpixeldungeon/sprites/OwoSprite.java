package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

public class OwoSprite extends MobSprite{
    public OwoSprite(){
        super();
        texture(Assets.Sprites.NEW_NPC);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 3, true );
        idle.frames( frames, 32,32,41,41,42,42 );

        run = new Animation( 20, true );
        run.frames( frames, 32 );

        die = new Animation( 20, false );
        die.frames( frames, 32,44,43 );
        operate= new Animation(6,false);
        operate.frames(frames,32,33,34,35,36,37,38,39,40,41,42);
        play( idle );
    }
}
