package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Goblin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GoblinSprite extends MobSprite {
    public GoblinSprite(){
        super();

        texture(Assets.Sprites.GOBLIN);

        TextureFilm frames = new TextureFilm( texture, 15, 14 );
        idle = new Animation( 3, true );
        idle.frames( frames, 0,1);

        run = new Animation( 10, true );
        run.frames( frames, 2,3, 4, 5 );

        attack = new Animation( 11, false );
        attack.frames( frames, 6,7, 8, 9);

        zap = attack.clone();

        die = new Animation( 9, false );
        die.frames( frames, 10, 11);

        play(idle);
    }
    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();

        }
        super.onComplete( anim );
    }
    public void zap( int cell ) {

        super.zap( cell );

        ((MissileSprite)parent.recycle( MissileSprite.class )).
                reset( this, cell, new ThrowingStone(), new Callback() {
                    @Override
                    public void call() {
                        ((Goblin)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.HIT );
    }
}
