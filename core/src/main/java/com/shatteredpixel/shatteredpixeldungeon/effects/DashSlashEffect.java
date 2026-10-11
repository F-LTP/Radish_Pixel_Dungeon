/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Group;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

/**
 * 冲刺斩击轨迹特效，会顺着冲刺方向旋转。
 *
 * 素材: effects/cc0/simplefx.png (CC0, Emcee Flesher, OpenGameArt)。
 * 使用第 4 行（0 基）第 8 列的一格 16x16 斜向风斩轨迹。
 *
 * 默认由影舞者的“解除隐身冲刺”播放。
 */
public class DashSlashEffect extends MovieClip {

	/** 游戏内是否启用（测试工具可切换）。 */
	public static boolean enabled = true;

	/** 素材基准朝向的补偿角（度），供微调。 */
	public static float angleOffset = 0f;

	private static TextureFilm film;
	private static Animation anim;

	// 素材第 4 行第 8 列的一格斜向斩击
	private static final int FRAME = 4 * 16 + 8;

	// 该格轨迹在屏幕坐标（y 向下）中大致沿右下 45° 方向
	private static final float BASE_ANGLE = 45f;

	public DashSlashEffect() {
		super( Assets.Effects.SIMPLE_FX );

		if (film == null) {
			film = new TextureFilm( texture, 16, 16 );
		}
		if (anim == null) {
			anim = new Animation( 15, false );
			anim.frames( film, FRAME, FRAME, FRAME, FRAME );
		}
		play( anim );
		scale.set( 1.5f );
		origin.set( width / 2f, height / 2f );
		listener = a -> killAndErase();
	}

	/** 在字符所在格播放，角度为屏幕坐标（y 向下）中的方向。 */
	public static void showOn( Char ch, float angleDeg ) {
		if (!enabled || ch == null || !(ch.sprite instanceof CharSprite)) {
			return;
		}
		CharSprite cs = (CharSprite) ch.sprite;
		if (cs.parent == null) {
			return;
		}
		showAt( cs.parent, cs.destinationCenter(), 1.5f, angleDeg );
	}

	public static void showAt( Group parent, PointF center, float scale, float angleDeg ) {
		if (!enabled || parent == null || center == null) {
			return;
		}
		DashSlashEffect e = new DashSlashEffect();
		e.scale.set( scale );
		e.origin.set( e.width / 2f, e.height / 2f );
		e.x = center.x - e.width / 2f;
		e.y = center.y - e.height / 2f;
		e.angle = angleDeg - BASE_ANGLE + angleOffset;
		parent.add( e );
	}
}
