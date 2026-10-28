package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deminion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deviloon;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class DeminionSprite extends MobSprite {
    private int zapPos;
    private Animation charging;
    private Emitter chargeParticles;
    public DeminionSprite() {
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

        charging = zap.clone();

        die = new Animation( 11, false );
        die.frames( frames, 14,15,16,17,18,19);

        play(idle);
    }

    @Override
    public void link(Char ch) {
        super.link(ch);

        chargeParticles = centerEmitter();
        chargeParticles.autoKill = false;
        chargeParticles.pour(Deviloon.DeviloonParticle.ATTRACTING, 0.05f);
        chargeParticles.on = false;

        //if (((Deviloon)ch).blastCharged) play(charging);
    }

    @Override
    public void update() {
        super.update();
        if (chargeParticles != null){
            chargeParticles.pos( center() );
            chargeParticles.visible = visible;
        }
    }

    @Override
    public void die() {
        super.die();
        if (chargeParticles != null){
            chargeParticles.on = false;
        }
    }

    @Override
    public void kill() {
        super.kill();
        if (chargeParticles != null){
            chargeParticles.killAndErase();
        }
    }

    public void charge( int pos ){
        turnTo(ch.pos, pos);
        play(charging);
        if (visible) Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
    }

    @Override
    public void play(Animation anim) {
        if (chargeParticles != null) chargeParticles.on = anim == charging;
        super.play(anim);
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
