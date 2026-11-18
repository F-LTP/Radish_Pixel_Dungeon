/*
 * Radish Pixel Dungeon
 *
 * 典狱长（Warden）精灵
 *
 * 贴图 warden.png 是 120x45：每帧 12x15，10 列 x 3 行，正好整除，
 * 帧索引 = 行号 * 10 + 帧序号。
 *   行 0 = 红眼形态（带红色拖尾）—— 用作死亡/解体
 *   行 1 = 红眼形态（带橙色拖尾）—— 半血后的形态
 *   行 2 = 常态（黑眼）
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.watabou.noosa.TextureFilm;

public class WardenSprite extends MobSprite {

	private Animation normalIdle, normalRun, normalAttack;
	private Animation rageIdle, rageRun, rageAttack, rageDie;

	private boolean enraged = false;

	public WardenSprite() {
		super();

		texture( Assets.Sprites.WARDEN );

		//warden.png 是 120x45：10 列 x 3 行，每帧 12x15，正好整除
		TextureFilm frames = new TextureFilm( texture, 12, 15 );

		//行 2（20..29）= 常态
		normalIdle   = new Animation( 2, true );
		normalIdle.frames( frames, 20, 21 );

		normalRun    = new Animation( 10, true );
		normalRun.frames( frames, 20, 21, 22, 23, 24 );

		normalAttack = new Animation( 12, false );
		normalAttack.frames( frames, 24, 25, 26, 27, 28, 29, 20 );

		//行 1（10..19）= 红眼形态
		rageIdle   = new Animation( 2, true );
		rageIdle.frames( frames, 10, 11 );

		rageRun    = new Animation( 10, true );
		rageRun.frames( frames, 10, 11, 12, 13, 14 );

		rageAttack = new Animation( 12, false );
		rageAttack.frames( frames, 14, 15, 16, 17, 18, 19, 10 );

		//行 0（0..9）= 红眼解体帧，用作死亡动画
		rageDie    = new Animation( 12, false );
		rageDie.frames( frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 );

		idle = normalIdle;
		run = normalRun;
		attack = normalAttack;
		die = rageDie;

		play( idle );
	}

	//红眼形态按 Boss 的真实状态补上。放在 linkVisuals() 而不是 link()：
	//读档后新建的精灵会经由 link() 走到这里，查看窗口、攻击指示器用的精灵则只走这里
	@Override
	public void linkVisuals( Char ch ) {
		super.linkVisuals( ch );
		if (ch instanceof Warden) {
			setEnraged( ((Warden) ch).isEnraged() );
		}
	}

	/** 半血后切红眼形态（贴图换行，不换精灵类） */
	public void setEnraged( boolean value ) {
		if (enraged == value ) {
			return;
		}
		enraged = value;
		if (enraged ) {
			idle = rageIdle;
			run = rageRun;
			attack = rageAttack;
		} else {
			idle = normalIdle;
			run = normalRun;
			attack = normalAttack;
		}
		idle();
	}

	public boolean isEnraged() {
		return enraged;
	}
}
