package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.ShieldHalo;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.RectF;

public class BoatSprite extends MobSprite {

    public BoatSprite() {
        super();
        texture(Assets.Sprites.NEW_NPC);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 6, true );
		idle.frames( frames, 64,65,66,67,68,69,70,71,72,71,70,69,68,67,66,73,73,73,74,74,74 );

    run = new Animation( 20, true );
		run.frames( frames, 64 );

    die = new Animation( 20, false );
		die.frames( frames, 64,76,75 );
    play( idle );
}

}
