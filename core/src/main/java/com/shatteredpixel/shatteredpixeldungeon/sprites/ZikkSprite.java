package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.BigSnake_Zikk;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

public class ZikkSprite extends MobSprite {

    private Animation poison;

    public ZikkSprite() {
        super();

        texture( Assets.Sprites.ZIKK );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 9, true );
        idle.frames( frames,  0,0,0,0,0,0,0,0,0,0,1,2,2,2,3,4,5,6);

        run = new Animation( 11, true );
        run.frames( frames, 7,8,9,10,11 );

        attack = new Animation( 11, false );
        attack.frames( frames, 12,13,14,15 );

        poison = new Animation( 13, false );
        poison.frames( frames, 12,13 );

        die = new Animation( 11, false );
        die.frames( frames, 16,17,18,19,20 );

        play( idle );
    }

    @Override
    public void attack( int cell ) {
        //只有正好隔 3 格才是喷毒液；靠武器攻击距离或精英词缀在 2 格、4 格出手时是普通攻击，要走带回调的攻击动画
        if (Dungeon.level.distance(cell, ch.pos) == 3) {

            MagicMissile.boltFromChar( parent,
                    MagicMissile.SHAMAN_PURPLE,
                    this,
                    cell,
                    () -> ((BigSnake_Zikk)ch).onZapComplete());
            Sample.INSTANCE.play( Assets.Sounds.ZAP );
            turnTo( ch.pos , cell );
            play( poison );
        } else {

            super.attack( cell );

        }
    }

}

