package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deminion;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class DeminionSprite extends MobSprite {
    public DeminionSprite() {
        super();

        texture(Assets.Sprites.DEMINION);

        TextureFilm frames = new TextureFilm( texture, 16, 16 );
        idle = new Animation( 1, true );
        idle.frames( frames, 0,1);

        run = new Animation( 9, true );
        run.frames( frames,  2,3,4, 5);

        attack = new Animation( 11, false );
        attack.frames( frames, 6,7,8);

        zap = new Animation(11,false);
        zap.frames(frames,9,10,11,12,13);

        die = new Animation( 11, false );
        die.frames( frames, 14,15,16,17,18);

        play(idle);
    }
    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.FIRE,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((Deminion)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.BURNING );
    }
    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }
}
