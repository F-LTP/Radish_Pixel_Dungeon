package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.ShieldMage;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class ShieldMageSprite extends MobSprite {
    public ShieldMageSprite() {
        super();

        texture(Assets.Sprites.SHIELD_MAGE);

        TextureFilm frames = new TextureFilm( texture, 18, 18 );
        idle = new Animation( 9, true );
        idle.frames( frames, 0,0,0,0,0,0,0,0,0,0,1,2,3);

        run = new Animation( 9, true );
        run.frames( frames,  4, 5, 6 ,7);

        attack = new Animation( 11, false );
        attack.frames( frames, 8,9,10,11);

        zap = attack.clone();

        die = new Animation( 11, false );
        die.frames( frames, 12,13,14,15);

        play(idle);
    }
    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.BEACON,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((ShieldMage)ch).onZapComplete(cell);
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }
}
