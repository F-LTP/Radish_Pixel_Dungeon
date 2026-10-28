package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deviloon;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class DeviloonSprite extends MobSprite {
    private int zapPos;
    private Animation charging;
    private Emitter chargeParticles;
    public DeviloonSprite() {
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

    @Override
    public void link(Char ch) {
        super.link(ch);

        chargeParticles = centerEmitter();
        chargeParticles.autoKill = false;
        chargeParticles.pour(Deviloon.DeviloonParticle.ATTRACTING, 0.05f);
        chargeParticles.on = false;

        if (((Deviloon)ch).blastCharged) play(charging);
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

    @Override
    public void zap( int pos ) {
        zapPos = pos;
        super.zap( pos );
        MagicMissile.boltFromChar( parent,
                MagicMissile.MAGIC_MISSILE,
                this,
                pos,
                new Callback() {
                    @Override
                    public void call() {
                        ((Deviloon)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.BURNING );
    }

    @Override
    public void onComplete( Animation anim ) {
        super.onComplete( anim );

        if (anim == zap) {
            idle();
            ((Deviloon)ch).callOfRune();
            ch.next();
        } else if (anim == die){
            chargeParticles.killAndErase();
        }
    }
}
