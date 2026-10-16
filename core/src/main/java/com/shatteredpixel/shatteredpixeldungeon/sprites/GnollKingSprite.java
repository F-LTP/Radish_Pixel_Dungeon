package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class GnollKingSprite extends MobSprite {
    private Animation stick;
    private Animation shadow;
    private Animation eyeing;
    private Animation charging;

    public GnollKingSprite() {
        super();

        texture( Assets.Sprites.GNOLL_KING );

        TextureFilm frames = new TextureFilm( texture, 12, 16 );

        idle = new Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new Animation( 12, true );
        run.frames( frames, 4, 5, 6, 7 );

        attack = new Animation( 12, false );
        attack.frames( frames, 2, 3, 0 );

        die = new Animation( 12, false );
        die.frames( frames, 8, 9, 10 );

        stick = new Animation( 10, false );
        stick.frames( frames, 11,12,13,14,15,16,17,18,19,20 );

        shadow = new Animation( 10, false );
        shadow.frames( frames, 22,23,24,25,26,27,28,29 );

        eyeing = new Animation( 10, false );
        eyeing.frames( frames, 33,34,35,36,37,38,38,38,38,38,39,40,41 );

        charging = new Animation( 12, true);
        charging.frames( frames, 13,13 );

        play( idle );
    }

    public void stickAttack( int cell ){
        turnTo( ch.pos, cell );
        play( charging );
    }

    public void stick( int cell ){
        turnTo( ch.pos, cell );
        play( stick );
    }

    public void shadow( int cell ){
        turnTo( ch.pos, cell );
        play( shadow );
    }

}
