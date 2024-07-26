package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class ShieldMageSprite extends MobSprite {
    public ShieldMageSprite() {
        super();

        texture(Assets.Sprites.SHIELD_MAGE);

        TextureFilm frames = new TextureFilm( texture, 18, 18 );
        idle = new Animation( 9, true );
        idle.frames( frames, 0,0,0,0,0,0,0,0,0,0,1,2,3);

        run = new Animation( 9, true );
        run.frames( frames,  4, 5, 6 ,7);

        attack = new Animation( 11, false );
        attack.frames( frames, 8,9,10,11);

        die = new Animation( 11, false );
        die.frames( frames, 12,13,14,15);

        play(idle);
    }
}
