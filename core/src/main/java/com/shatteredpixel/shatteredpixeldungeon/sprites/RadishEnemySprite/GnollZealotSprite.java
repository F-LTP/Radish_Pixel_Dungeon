package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.GnollZealot;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GnollZealotSprite extends MobSprite {
    public GnollZealotSprite(){
        super();

        texture(Assets.Sprites.GNOLLZEALOT);

        TextureFilm frames = new TextureFilm( texture, 14, 15 );
        idle = new MovieClip.Animation( 3, true );
        idle.frames( frames, 0,1);

        run = new MovieClip.Animation( 11, true );
        run.frames( frames,  2,3,4, 5, 6);

        attack = new MovieClip.Animation( 11, false );
        attack.frames( frames, 7,8,9,10);

        zap = attack.clone();

        die = new MovieClip.Animation( 11, false );
        die.frames( frames, 11,12,13,14);

        play(idle);
    }
    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.MAGIC_MISSILE,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((GnollZealot)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }
    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }
}
