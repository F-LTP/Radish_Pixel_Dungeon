package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

public class TheCatistSprite extends MobSprite {

    public TheCatistSprite(){
        super();
        texture(Assets.Sprites.THE_CATIST);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 5, true );
        idle.frames( frames, 0,0,0, 1,1,1,  8,8,8, 9,9,9, 1,1,1, 0,0,0, 9,9,9, 8,8,8, 2,3, 4, 5, 6, 7 );

        run = new Animation( 20, true );
        run.frames( frames, 0 );

        die = new Animation( 20, false );
        die.frames( frames, 0, 1, 2 );
        play( idle );
    }

    @Override
    public void die() {
        super.die();

        emitter().start( ElmoParticle.FACTORY, 0.03f, 60 );

        if (visible) {
            Sample.INSTANCE.play( Assets.Sounds.BURNING );
        }
    }

}