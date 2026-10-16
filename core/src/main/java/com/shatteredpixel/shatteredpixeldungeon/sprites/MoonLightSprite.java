package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

public class MoonLightSprite extends MobSprite{
    public MoonLightSprite(){
        super();
        texture(Assets.Sprites.NEW_NPC);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 6, true );
        idle.frames( frames, 112,113,114,115,116,117,118,119,120,120,120,121,122,122,121,121 );

        run = new Animation( 20, true );
        run.frames( frames, 112 );

        die = new Animation( 20, false );
        die.frames( frames, 112,124,123 );
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
