package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollShamanKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GnollShamanKingSprite extends MobSprite {

    private Animation rock;
    private Animation shadow;
    private Animation eye;

    public GnollShamanKingSprite() {
        super();

        texture( Assets.Sprites.GNOLL_SHAMAN_KING );

        TextureFilm frames = new TextureFilm( texture, 12, 16 );

        idle = new MovieClip.Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new MovieClip.Animation( 12, true );
        run.frames( frames, 4, 5, 6, 7 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 2, 3, 0 );

        die = new MovieClip.Animation( 12, false );
        die.frames( frames, 8, 9, 10 );

        zap = attack.clone();

        shadow = new MovieClip.Animation(10,false);
        shadow.frames(frames,16,16,17,18,19,20,21,22,23,24,25,26,27,28,29,29);

        eye = new MovieClip.Animation(10,false);
        eye.frames(frames, 32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,47);

        rock = new MovieClip.Animation(10,false);
        rock.frames(frames,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63);

        play( idle );
    }


    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.SHAMAN_BLUE,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((GnollShamanKing)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }

    // 施法动画播完后回待机这一步，基类不做，要贴图类自己做（同原版 ShamanSprite）；不做就会停在施法的最后一帧
    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }

    public void eye( int cell ){
        turnTo( ch.pos, cell );
        play( eye );
    }

    public void rock( int cell ){
        turnTo( ch.pos, cell );
        play( rock );
    }

    public void shadow( int cell ){
        turnTo( ch.pos, cell );
        play( shadow );
    }

}
