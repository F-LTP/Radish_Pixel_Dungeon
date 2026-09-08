package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

/**
 * 杂散皮肤精灵（所有职业共享的可选皮肤）。
 * <p>
 * 采用怪物的贴图模板：这是一个自足完整的精灵类，所有动画都在本类内定义，
 * 不依赖英雄的盔甲分层逻辑，可直接替换角色的原版动作。
 * 继承 {@link HeroSprite} 是为了让 17+ 处强转（移动 sprint、卷轴 read、伪装 disguise、
 * 换装 updateArmor 等）依然可用，从而让 Hero 无缝使用本精灵。
 * <p>
 * 贴图为 jumble.png（336x192），按 12x16 帧格划分，共 28 列 x 12 行。
 * 共 6 组贴图，每组占据两行，与职业无关（每 50 回合自动变身到其中一组）。
 * <p>
 * 组内第一行：站立 2 帧、行走 6 帧、死亡 4 帧、攻击 5 帧、使用 2 帧、阅读 3 帧；
 * 组内第二行：前 10 帧消失、后 10 帧出现。
 * 第 5 组攻击为 9 帧（12-20），第 6 组攻击为 10 帧（12-21），使用/阅读帧随之延后。
 */
public class JumbleSprite extends HeroSprite {

	private static final int FRAME_W = 12;
	private static final int FRAME_H = 16;

	private static final int COLS = 28;   //每行帧数

	// 当前使用第几组贴图（0-5）
	private int group = 0;

	private Animation enterInvisible;
	private Animation exitInvisible;

	public JumbleSprite() {
		super();
	}

	/** 切换到指定组（0-5）的贴图，重建动画并回到待机。 */
	public void setGroup( int g ) {
		this.group = ((g % 6) + 6) % 6;
		updateArmor();
		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	public int group() {
		return group;
	}

	@Override
	public void updateArmor() {
		texture( Assets.Sprites.JUMBLE );

		TextureFilm film = new TextureFilm( texture, FRAME_W, FRAME_H );

		int base = group * 2 * COLS;        //该组第一行行首索引
		int base2 = (group * 2 + 1) * COLS; //该组第二行行首索引

		// 站立：列 0-1
		idle = new Animation( 2, true ).named( "jumble.idle g" + group );
		idle.frames( film, base + 0, base + 0, base + 0, base + 1, base + 0, base + 0, base + 1, base + 1 );

		// 行走：列 2-7
		run = new Animation( 20, true ).named( "jumble.run g" + group );
		run.frames( film, base + 2, base + 3, base + 4, base + 5, base + 6, base + 7 );

		// 死亡：列 8-11
		die = new Animation( 20, false ).named( "jumble.die g" + group );
		die.frames( film, base + 8, base + 9, base + 10, base + 11 );

		// 攻击、使用、阅读：列位随组别不同
		int attackStart = base + 12;
		int attackEnd;
		if (group == 4) {         //第 5 组：攻击 12-20
			attackEnd = base + 20;
		} else if (group == 5) {  //第 6 组：攻击 12-21
			attackEnd = base + 21;
		} else {                  //前 4 组：攻击 12-16
			attackEnd = base + 16;
		}

		int[] attackFrames = new int[attackEnd - attackStart + 1];
		for (int i = 0; i < attackFrames.length; i++) attackFrames[i] = attackStart + i;

		attack = new Animation( 15, false ).named( "jumble.attack g" + group );
		// 不能用 int[] 直接传给 Object... 可变参数（会被当作单元素，film 查不到）
		Object[] attackFilm = new Object[attackFrames.length];
		for (int i = 0; i < attackFrames.length; i++) attackFilm[i] = attackFrames[i];
		attack.frames( film, attackFilm );

		zap = attack.clone();

		// 使用：紧随攻击之后 2 帧
		operate = new Animation( 8, false ).named( "jumble.operate g" + group );
		operate.frames( film, attackEnd + 1, attackEnd + 2, attackEnd + 1, attackEnd + 2 );

		// 阅读：紧随使用之后 3 帧
		read = new Animation( 20, false ).named( "jumble.read g" + group );
		read.frames( film, attackEnd + 3, attackEnd + 4, attackEnd + 5, attackEnd + 5, attackEnd + 5, attackEnd + 5, attackEnd + 5, attackEnd + 5, attackEnd + 5, attackEnd + 3 );

		// 第二行：前 10 帧消失、后 10 帧出现
		enterInvisible = new Animation( 12, false ).named( "jumble.enterInvisible g" + group );
		enterInvisible.frames( film, base2 + 0, base2 + 1, base2 + 2, base2 + 3, base2 + 4, base2 + 5, base2 + 6, base2 + 7, base2 + 8, base2 + 9 );

		exitInvisible = new Animation( 12, false ).named( "jumble.exitInvisible g" + group );
		exitInvisible.frames( film, base2 + 10, base2 + 11, base2 + 12, base2 + 13, base2 + 14, base2 + 15, base2 + 16, base2 + 17, base2 + 18, base2 + 19 );

		fly = idle.clone();

		// 确保变身倒计时 buff 存在（幂等，不重复）
		if (ch == Dungeon.hero) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.JumbleChangeBuff.resetCountdownIfMissing();
		}

		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	/** 播放消失动画。 */
	public void enterInvisible() {
		play( enterInvisible );
	}

	/** 播放出现动画。 */
	public void exitInvisible() {
		play( exitInvisible );
	}

	/** 播放当前组的变身（消失）动画，结束后回调。 */
	public void playChange( Callback onComplete ) {
		animCallback = onComplete;
		play( enterInvisible );
	}

	/**
	 * 切换到指定组并播放其出现动画（表示变身完成后的新形态），结束后回调并回到待机。
	 */
	public void playAppear( int newGroup, Callback onComplete ) {
		//先切贴图并建立新组的出现动画，再播放
		this.group = ((newGroup % 6) + 6) % 6;
		updateArmor();
		animCallback = onComplete;
		play( exitInvisible );
	}
}
