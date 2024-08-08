package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class DeviloonSprite extends MobSprite {
    public DeviloonSprite() {
        super();

        texture(Assets.Sprites.DEVILOON);

        TextureFilm frames = new TextureFilm( texture, 24, 17 );
        idle = new Animation( 9, true );
        idle.frames( frames, 0,1,2,3);

        run = new Animation( 9, true );
        run.frames( frames,  3,4, 5, 6);

        attack = new Animation( 9, false );
        attack.frames( frames, 7,8,9);

        zap = new Animation(11,false);
        zap.frames(frames,10,11,12,13);

        die = new Animation( 11, false );
        die.frames( frames, 14,15,16,17,18,19);

        play(idle);
    }
}
