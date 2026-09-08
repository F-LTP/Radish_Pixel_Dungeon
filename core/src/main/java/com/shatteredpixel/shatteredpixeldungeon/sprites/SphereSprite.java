package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

/**
 * 圆球皮肤精灵（月华 MOONLIGHT 的可选皮肤）。
 * <p>
 * 采用怪物的贴图模板：这是一个自足完整的精灵类，所有动画都在本类内定义，
 * 不依赖英雄的盔甲分层逻辑，可直接替换角色的原版动作。
 * 继承 {@link HeroSprite} 是为了让 17+ 处强转（移动 sprint、卷轴 read、伪装 disguise、
 * 换装 updateArmor 等）依然可用，从而让 Hero 无缝使用本精灵。
 * <p>
 * 贴图为 sphere.png（168x48），按 12x16 帧格划分，共 14 列 x 3 行：
 * 第 1 行：闪烁待机 5 帧（首帧时长 7 倍）、滚动 10 帧；
 * 第 2 行：跳跃 10 帧（用作互动动画）、卷轴阅读 4 帧；
 * 第 3 行：死亡裂开 4 帧。
 * <p>
 * <b>特殊移动表现</b>：移动结束后不再回退到待机闪烁，而是冻结在当前滚动帧；
 * 再次移动时从该帧继续滚动，形成连贯的滚动效果。同时移动耗时放大一倍（减速 50%），
 * 让滚动动画有更充足的时间播放。
 */
public class SphereSprite extends HeroSprite {

	private static final int FRAME_W = 12;
	private static final int FRAME_H = 16;

	// 是否处于"滚动冻结"状态：移动结束后停留在当前滚动帧，而非回退到待机闪烁
	private boolean holdRun = false;

	public SphereSprite() {
		super();
	}

	@Override
	public void updateArmor() {
		texture( Assets.Sprites.SPHERE );

		TextureFilm film = new TextureFilm( texture, FRAME_W, FRAME_H );

		// 闪烁待机：第 1 行帧 0-4，首帧（帧 0）重复 7 次实现 7 倍时长
		idle = new Animation( 10, true );
		idle.frames( film, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 0, 0, 1, 2, 3, 4 );

		// 滚动：第 1 行帧 0、5-13
		run = new Animation( 30, true );
		run.frames( film, 0, 5, 6, 7, 8, 9, 10, 11, 12, 13 );

		// 跳跃（互动）：第 2 行帧 14-23
		operate = new Animation( 30, false );
		operate.frames( film, 14, 15, 16, 17, 17,18,18, 19, 20, 21, 22,0);

		// 攻击：滚动跳跃，复用跳跃帧
		attack = operate.clone();
		zap = operate.clone();

		// 卷轴阅读：第 2 行帧 24-27
		read = new Animation( 10, false );
		read.frames( film, 23, 24, 25, 26, 27, 27, 27, 27);

		// 死亡裂开：第 3 行帧 28-31
		die = new Animation( 5, false );
		die.frames( film, 28, 29, 30, 31 );

		fly = idle.clone();

		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	@Override
	public void move( int from, int to ) {
		holdRun = false;
		super.move( from, to );
		// 移动耗时放大一倍：从一格移动到另一格的速度降低 50%，
		// 以便滚动动画有更充足的时间播放。
		if (motion != null) {
			motion.interval *= 1.5f;
		}
	}

	/**
	 * 原地旋转动画：从 {@code fromAngle} 沿最短弧旋转到 {@code toAngle}。
	 * <p>根据要旋转的角度差，从移动动画（滚动 run）的帧中取出对应的一段，动态拼成一个
	 * 一次性播放的动画，并沿用 run 的 fps（{@code run.delay}）。这样旋转就以与移动完全一致的
	 * 帧率播放，中间帧由引擎逐帧渲染，不会跳帧。第 0 帧即正确角度（0°）。</p>
	 *
	 * @param fromAngle 起始角度（0-360）
	 * @param toAngle   目标角度（0-360；0 为正确角度）
	 */
	public void spin( float fromAngle, float toAngle ){

		int fromIdx = angleToFrame( fromAngle );
		int toIdx   = angleToFrame( toAngle );

		int diff = toIdx - fromIdx;
		if (diff > 5) diff -= 10;
		else if (diff < -5) diff += 10;

		int steps = Math.abs( diff );
		int dir   = (int)Math.signum( diff );

		if (steps == 0){
			// 已在正确角度，直接停在第 0 帧
			play( run, true );
			paused = true;
			curFrame = toIdx;
			return;
		}

		// 按最短弧方向取出 run 帧的一段（含起点与终点），拼成一次性动画，沿用移动动画 fps
		RectF[] cells = new RectF[steps + 1];
		int idx = fromIdx;
		for (int i = 0; i <= steps; i++){
			cells[i] = run.frames[idx];
			idx = (idx + dir + 10) % 10;
		}
		Animation rotate = new Animation( Math.round( 1f / run.delay ), false ).frames( cells );

		flipHorizontal = dir < 0;
		play( rotate, true );
	}

	/** 把角度（0-360）映射为滚动动画帧索引（0-9，0 = 正确角度）。 */
	private int angleToFrame( float angle ){
		float a = ((angle % 360f) + 360f) % 360f;
		return (Math.round( a / 36f )) % 10;
	}

	/**
	 * 让圆球显示指定角度对应的滚动帧（用于载入存档时同步角度）。
	 * @param angle 当前朝向角度（0-360）
	 */
	public void setAngle( float angle ){
		int idx = angleToFrame( angle );
		play( run, true );
		paused = true;
		curFrame = idx;
		flipHorizontal = false;
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		// 载入/创建精灵时：把贴图切换到当前角度对应的滚动帧
		if (ch instanceof Hero && ((Hero) ch).isSphereSkin()){
			setAngle( ((Hero) ch).sphereAngle() );
		}
	}

	@Override
	public void idle() {
		// 移动结束后不切换到待机闪烁，而是冻结在当前的滚动帧。
		if (curAnim == run) {
			holdRun = true;
			// 滚动停止时，让逻辑角度跟随视觉定格帧（角度 = 帧 × 36°）。
			// 这样角度为 0 时一定显示第 0 帧，不会停在最后一帧，且无需打断自然的滚动定格。
			if (ch instanceof Hero && ((Hero) ch).isSphereSkin()){
				((Hero) ch).sphereAngleFromFrame( curFrame );
			}
			return;
		}
		super.idle();
	}

	@Override
	public synchronized void play( Animation anim, boolean force ) {
		// 播放任意动画时退出滚动冻结状态并解除暂停，使动画得以播放。
		holdRun = false;
		paused = false;
		super.play( anim, force );
	}

	@Override
	public void onComplete( Animation anim ) {
		// 攻击动画（无回调）结束后，切回滚动动画第 0 帧（正确角度），
		// 而不是停留在攻击动画的最后一帧。
		if (anim == attack && animCallback == null) {
			setAngle( 0f );
			if (ch != null) ch.onAttackComplete();
		} else {
			super.onComplete( anim );
		}
	}

	@Override
	public void update() {
		// 持续冻结滚动帧：即使 paralysis 等状态清除了 paused 标志，
		// 只要仍处于 holdRun，就保持动画停在当前帧。
		if (holdRun && curAnim == run) {
			paused = true;
		}
		super.update();
	}
}
