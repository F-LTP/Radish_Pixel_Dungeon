package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

import java.util.HashMap;
import java.util.Map;

/**
 * 流浪者皮肤精灵（战士的可选皮肤）。
 * <p>
 * 采用怪物的贴图模板：这是一个自足完整的精灵类，所有动画都在本类内定义，
 * 不依赖英雄的盔甲分层逻辑，可直接替换角色的原版动作。
 * 继承 {@link HeroSprite} 是为了让 17+ 处强转（移动 sprint、卷轴 read、伪装 disguise、
 * 换装 updateArmor 等）依然可用，从而让 Hero 无缝使用本精灵。
 * <p>
 * 贴图为 wanderer.png（180x48），按 12x16 帧格划分，共 15 列 x 3 行。
 * 每行动画排版与赌徒（gambler.png）一致，仅末行缺少赌徒的"退出隐身"5 帧：
 * 第 1 行：待机 2 帧、行走 6 帧、死亡 6 帧；
 * 第 2 行：攻击 3 帧、使用 2 帧、阅读 3 帧；
 * 第 3 行：拳击 5 帧（34-38，带拳击特效 affix）、斩击 5 帧（40-44，带斩击特效 affix）。
 */
public class WandererSprite extends HeroSprite {

	private static final int FRAME_W = 12;
	private static final int FRAME_H = 16;

	private Animation punch;
	private Animation slash;

	// 这些affix的偏移是用游戏内的动画调整器做的，不要傻傻的自己调哦
	private static final Map<Integer, Affix> FRAME_AFFIX = new HashMap<>();
	static {
		FRAME_AFFIX.put( 34, new Affix( "sprites/RadishSnDSprite/wanderer/punch1.png", -1.0f, 5.0f ) );
		FRAME_AFFIX.put( 35, new Affix( "sprites/RadishSnDSprite/wanderer/punch2.png", 0.0f, -1.0f ) );
		FRAME_AFFIX.put( 36, new Affix( "sprites/RadishSnDSprite/wanderer/punch3.png", 2.0f, -4.0f ) );
		FRAME_AFFIX.put( 37, new Affix( "sprites/RadishSnDSprite/wanderer/punch4.png", 7.0f, -2.0f ) );
		FRAME_AFFIX.put( 38, new Affix( "sprites/RadishSnDSprite/wanderer/punch5.png", 12.0f, 3.0f ) );
		FRAME_AFFIX.put( 40, new Affix( "sprites/RadishSnDSprite/wanderer/slash1.png", -1.0f, -1.0f ) );
		FRAME_AFFIX.put( 41, new Affix( "sprites/RadishSnDSprite/wanderer/slash2.png", -1.0f, -1.0f ) );
		FRAME_AFFIX.put( 42, new Affix( "sprites/RadishSnDSprite/wanderer/slash3.png", -1.0f, -1.0f ) );
		FRAME_AFFIX.put( 43, new Affix( "sprites/RadishSnDSprite/wanderer/slash4.png", -1.0f, -1.0f ) );
		FRAME_AFFIX.put( 44, new Affix( "sprites/RadishSnDSprite/wanderer/slash5.png", -1.0f, -1.0f ) );
	}

	// 动画 -> 序列位置到贴图帧下标的映射（仅有 affix 的动画需要登记）
	// 注意：不能用字段初始化，HeroSprite 构造时就会调用 updateArmor()（虚方法），
	// 此时子类字段尚未初始化，因此这里必须惰性创建。
	private Map<Animation, int[]> affixSeqs;

	private Map<Animation, int[]> affixSeqs() {
		if (affixSeqs == null) affixSeqs = new HashMap<>();
		return affixSeqs;
	}

	private Image affixImage; // 手动在 draw() 中叠加绘制，保证处于角色帧图上层
	private int lastAffixFrame = -1; // 初始 -1，保证首帧即触发显示
	private Animation lastAffixAnim; // 上一次处理 affix 的动画，用于切换 punch/slash 时强制刷新

	public WandererSprite() {
		super();
	}

	@Override
	public void updateArmor() {
		texture( Assets.Sprites.WANDERER );

		TextureFilm film = new TextureFilm( texture, FRAME_W, FRAME_H );

		// 待机：第 1 行帧 0-1
		idle = new Animation( 2, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

		// 行走：第 1 行帧 2-7
		run = new Animation( 20, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		// 攻击：第 2 行帧 15-17
		attack = new Animation( 15, false );
		attack.frames( film, 15, 16, 17, 15 );

		zap = attack.clone();

		// 互动：第 2 行帧 18-19
		operate = new Animation( 8, false );
		operate.frames( film, 18, 19, 18, 19 );

		// 死亡：第 1 行帧 8-12
		die = new Animation( 20, false );
		die.frames( film, 8, 9, 10, 11, 12 );

		// 拳击：第 3 行帧 34-38（带拳击 affix）
		punch = new Animation( 5, false );
		punch.frames( film, 34, 35, 36, 37, 38 );
		affixSeqs().put( punch, new int[]{ 34, 35, 36, 37, 38 } );

		// 斩击：第 3 行帧 40-44（带斩击 affix）
		slash = new Animation( 5, false );
		slash.frames( film, 40, 41, 42, 43, 44 );
		affixSeqs().put( slash, new int[]{ 40, 41, 42, 43, 44 } );

		fly = idle.clone();
		read = new Animation( 20, false );
		read.frames( film, 20, 21, 22, 22, 22, 22, 22, 22, 22, 20 );

		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	/** 播放拳击动画（帧 34-38，带拳击特效）。 */
	public void punch() {
		play( punch );
	}

	/** 播放斩击动画（帧 40-44，带斩击特效）。 */
	public void slash() {
		play( slash );
	}

	// ==================== affix 渲染 ====================

	static class Affix {
		final String path;
		final float x, y; // x/y = 相对角色帧左上角的像素偏移
		Affix( String p, float x, float y ) {
			path = p;
			this.x = x;
			this.y = y;
		}
	}

	private void showAffix( int frameIndex ) {
		Affix a = FRAME_AFFIX.get( frameIndex );
		if (a == null) {
			if (affixImage != null) affixImage.visible = false;
			return;
		}
		if (affixImage == null) {
			affixImage = new Image();
		}
		affixImage.texture( a.path ); // 整图渲染，不做裁剪
		affixImage.flipHorizontal = flipHorizontal;
		if (flipHorizontal) {
			// 角色面向左时整帧镜像，affix 也随之镜像，并沿帧中心对称摆放
			affixImage.x = x + FRAME_W - a.x - affixImage.width();
		} else {
			affixImage.x = x + a.x;
		}
		affixImage.y = y + a.y;
		affixImage.visible = true;
	}

	// 自动检测帧切换：仅对登记过序列的动画（punch/slash）查找 affix，其余动画自动隐藏
	@Override
	public void update() {
		super.update();
		int[] seq = curAnim == null ? null : affixSeqs().get( curAnim );
		if (seq == null) {
			if (affixImage != null) affixImage.visible = false;
			lastAffixFrame = -1;
			lastAffixAnim = null;
			return;
		}
		if (curAnim != lastAffixAnim || curFrame != lastAffixFrame) {
			lastAffixAnim = curAnim;
			lastAffixFrame = curFrame;
			showAffix( seq[curFrame] );
		}
	}

	@Override
	public void draw() {
		super.draw();
		if (affixImage != null && affixImage.visible && visible) {
			affixImage.camera = camera();
			affixImage.draw();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		if (affixImage != null) {
			affixImage.destroy();
			affixImage = null;
		}
	}
}