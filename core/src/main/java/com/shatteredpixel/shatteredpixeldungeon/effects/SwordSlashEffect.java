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
 * 一次性剑斩特效，会顺着攻击方向旋转。
 *
 * 素材: effects/cc0/sword_slash.png (CC0, tbbk, OpenGameArt)。
 * 3x3 网格，每格 64x47，共 9 帧。
 *
 * 默认接在 {@link com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword}、
 * {@link com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.KillBoatSword} 命中时，
 * 以及 {@link com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute} 攻击时。
 * 可在测试模式工具 {@code EffectTester} 中开关、预览并微调朝向。
 */
public class SwordSlashEffect extends MovieClip {

	/** 游戏内是否启用（测试工具可切换）。 */
	public static boolean enabled = true;

	/** 素材基准朝向的补偿角（度），供测试工具微调。 */
	public static float angleOffset = 0f;

	private static TextureFilm film;
	private static Animation anim;

	public SwordSlashEffect() {
		super( Assets.Effects.SWORD_SLASH );

		if (film == null) {
			film = new TextureFilm( texture, 64, 47 );
		}
		if (anim == null) {
			anim = new Animation( 24, false );
			anim.frames( film, 0, 1, 2, 3, 4, 5, 6, 7, 8 );
		}
		play( anim );
		scale.set( 0.6f );
		origin.set( width / 2f, height / 2f );
		listener = a -> killAndErase();
	}

	/** 游戏内调用：在 defender 身上、朝向 attacker -> defender 的方向播放。 */
	public static void showOn( Char attacker, Char defender ) {
		if (!enabled) {
			return;
		}
		previewOn( attacker, defender );
	}

	/** 工具预览调用：不受开关影响。 */
	public static void previewOn( Char attacker, Char defender ) {
		if (attacker == null || defender == null
				|| !(attacker.sprite instanceof CharSprite)
				|| !(defender.sprite instanceof CharSprite)
				|| defender.sprite.parent == null) {
			return;
		}
		PointF from = ((CharSprite) attacker.sprite).destinationCenter();
		PointF to = ((CharSprite) defender.sprite).destinationCenter();
		float angle = (float) Math.toDegrees( Math.atan2( to.y - from.y, to.x - from.x ) );
		preview( defender.sprite.parent, to, 0.6f, angle + angleOffset );
	}

	public static void preview( Group parent, PointF center, float scale, float angleDeg ) {
		if (parent == null || center == null) {
			return;
		}
		SwordSlashEffect e = new SwordSlashEffect();
		e.scale.set( scale );
		e.origin.set( e.width / 2f, e.height / 2f );
		e.x = center.x - e.width / 2f;
		e.y = center.y - e.height / 2f;
		e.angle = angleDeg;
		parent.add( e );
	}
}
