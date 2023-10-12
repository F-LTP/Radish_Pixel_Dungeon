package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.TextureFilm;

public class ThunderStormSprite extends MobSprite {
    public ThunderStormSprite(){
        super();
        texture(Assets.Sprites.NEW_NPC);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 6, true );
        idle.frames( frames, 48,49,50,51,52,53,54,55,56,57,57,57,58,58,58 );

        run = new Animation( 20, true );
        run.frames( frames, 48 );

        die = new Animation( 20, false );
        die.frames( frames, 48,60,59 );
        play( idle );
    }
}
