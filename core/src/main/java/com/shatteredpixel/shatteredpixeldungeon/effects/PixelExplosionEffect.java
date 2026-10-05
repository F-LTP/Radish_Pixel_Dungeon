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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

/**
 * 一次性像素爆炸特效。
 *
 * 素材: effects/cc0/pixel_explosion.png (CC0, BitingChaos, OpenGameArt)。
 * 80x16，5 帧 16x16。
 *
 * 默认接在爆破合剂命中时。可在测试模式工具 {@code EffectTester} 中开关与预览。
 */
public class PixelExplosionEffect extends MovieClip {

	/** 游戏内是否启用（测试工具可切换）。 */
	public static boolean enabled = true;

	private static TextureFilm film;
	private static Animation anim;

	public PixelExplosionEffect() {
		super( Assets.Effects.PIXEL_EXPLOSION );

		if (film == null) {
			film = new TextureFilm( texture, 16, 16 );
		}
		if (anim == null) {
			anim = new Animation( 16, false );
			anim.frames( film, 0, 1, 2, 3, 4 );
		}
		play( anim );
		scale.set( 2f );
		listener = a -> killAndErase();
	}

	/** 游戏内调用：受 {@link #enabled} 控制。 */
	public static void showAtCell( int cell ) {
		if (!enabled) {
			return;
		}
		previewAtCell( cell );
	}

	/** 工具预览调用：不受开关影响。 */
	public static void previewAtCell( int cell ) {
		if (Dungeon.hero == null || Dungeon.hero.sprite == null) {
			return;
		}
		preview( Dungeon.hero.sprite.parent, DungeonTilemap.tileCenterToWorld( cell ), 2f );
	}

	public static void preview( Group parent, PointF center, float scale ) {
		if (parent == null || center == null) {
			return;
		}
		PixelExplosionEffect e = new PixelExplosionEffect();
		e.scale.set( scale );
		e.x = center.x - e.width * scale / 2f;
		e.y = center.y - e.height * scale / 2f;
		parent.add( e );
	}
}
