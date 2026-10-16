package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

/**
 * 赌徒皮肤精灵（盗贼的可选皮肤）。
 * <p>
 * 采用怪物的贴图模板：这是一个自足完整的精灵类，所有动画都在本类内定义，
 * 不依赖英雄的盔甲分层逻辑，可直接替换角色的原版动作。
 * 继承 {@link HeroSprite} 是为了让 17+ 处强转（移动 sprint、卷轴 read、伪装 disguise、
 * 换装 updateArmor 等）依然可用，从而让 Hero 无缝使用本精灵。
 * <p>
 * 贴图为 gambler.png（240x48），按 12x16 帧格划分，共 20 列 x 3 行：
 * 第 1 行：待机 2 帧、行走 6 帧、死亡 6 帧；
 * 第 2 行：攻击 3 帧、使用 2 帧、阅读 3 帧；
 * 第 3 行：进入隐身 8 帧、斩击特效 7 帧、退出隐身 5 帧。
 */
public class GamblerSprite extends HeroSprite {

	private static final int FRAME_W = 12;
	private static final int FRAME_H = 16;

	private Animation enterInvisible;
	private Animation slash;
	private Animation exitInvisible;

	public GamblerSprite() {
		super();
	}

	@Override
	public void updateArmor() {
		texture( Assets.Sprites.GAMBLER );

		TextureFilm film = new TextureFilm( texture, FRAME_W, FRAME_H );

		// 待机：第 1 行帧 0-1
		idle = new Animation( 2, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

		// 行走：第 1 行帧 2-7
		run = new Animation( 20, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		// 攻击：第 2 行帧 20-22
		attack = new Animation( 15, false );
		attack.frames( film, 20, 21, 22, 20 );

		zap = attack.clone();

		// 互动：第 2 行帧 23-24
		operate = new Animation( 8, false );
		operate.frames( film, 23, 24, 23, 24 );

		// 死亡：第 1 行帧 8-13
		die = new Animation( 20, false );
		die.frames( film, 8, 9, 10, 11, 12, 13 );

		Animation enterInvisible = new Animation( 8, false );
		enterInvisible.frames( film, 40, 41, 42, 43, 44, 45, 46, 47, 18, 19 );

		slash = new Animation( 12, false );
		slash.frames( film, 48, 49, 50, 51, 52, 53, 54 );

		exitInvisible = new Animation( 8, false );
		exitInvisible.frames( film, 55, 56, 57, 58, 59 );

		fly = idle.clone();
		read = new Animation( 20, false );
		read.frames( film, 25, 26, 27, 27, 27, 27, 27, 27, 27, 25 );

		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	/** 播放进入隐身动画。 */
	public void enterInvisible() {
		play( enterInvisible );
	}

	/** 播放斩击特效动画。 */
	public void slash() {
		play( slash );
	}

	/** 播放退出隐身动画。 */
	public void exitInvisible() {
		play( exitInvisible );
	}
}
