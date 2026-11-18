/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

//传奇重犯精灵：442x25，17 帧 26x25。
//待机 0,1；行走 2-5；攻击 6-9；跳跃 10-13；死亡 14-16。
public class FelonSprite extends MobSprite {

	//跃起坠落动画（10-13）
	public MovieClip.Animation jump;

	public FelonSprite() {
		super();

		texture( Assets.Sprites.FELON );

		TextureFilm frames = new TextureFilm( texture, 26, 25 );

		idle = new MovieClip.Animation( 1, true );
		idle.frames( frames, 0, 1 );

		run = new MovieClip.Animation( 10, true );
		run.frames( frames, 2, 3, 4, 5 );

		attack = new MovieClip.Animation( 10, false );
		attack.frames( frames, 6, 7, 8, 9 );

		jump = new MovieClip.Animation( 12, false );
		jump.frames( frames, 10, 11, 12, 13 );

		zap = new MovieClip.Animation( 12, false );
		zap.frames( frames, 10, 11, 12, 13 );

		die = new MovieClip.Animation( 10, false );
		die.frames( frames, 14, 15, 16 );

		play( idle );
	}

	//播放跃起动画并沿弧线跳到目标格，落地后回调
	public void leap( int from, int to, Callback callback ) {
		turnTo( from, to );
		play( jump );
		super.jump( from, to, callback );
	}

	@Override
	public int blood() {
		return 0xFFCC3333;
	}
}
