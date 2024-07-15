package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class GoblinSprite extends MobSprite {
    public GoblinSprite(){
        super();

        texture(Assets.Sprites.GOBLIN);

        TextureFilm frames = new TextureFilm( texture, 15, 14 );
        idle = new Animation( 1, true );
        idle.frames( frames, 0,1);

        run = new Animation( 8, true );
        run.frames( frames, 2,3, 4, 5 );

        attack = new Animation( 9, false );
        attack.frames( frames, 6,7, 8, 9);

        die = new Animation( 7, false );
        die.frames( frames, 10, 11);

        play(idle);
    }
}
