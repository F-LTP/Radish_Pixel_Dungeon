package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Mayfly;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class MayflySprite extends MobSprite {
    public MayflySprite(){
        super();

        texture(Assets.Sprites.MAYFLY);

        TextureFilm frames = new TextureFilm( texture, 20, 16 );
        idle = new Animation( 9, true );
        idle.frames( frames, 0,1,2,3);

        run = new Animation( 9, true );
        run.frames( frames, 4, 5, 6,7 );

        attack = new Animation( 9, false );
        attack.frames( frames, 8, 9,10);

        zap = attack.clone();

        die = new Animation( 9, false );
        die.frames( frames, 11, 12,13,14,15 );

        play(idle);
    }
    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.FOLIAGE,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((Mayfly)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }
}
