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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

//猎头蟹专用精灵：256x16 单行、16 帧 16x16。
public class HeadcrabSprite extends MobSprite {

	public HeadcrabSprite() {
		super();

		texture( Assets.Sprites.HEADCRAB );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new MovieClip.Animation( 6, true );
		idle.frames( frames, 0, 1, 0, 2 );

		run = new MovieClip.Animation( 12, true );
		run.frames( frames, 4, 5, 6, 7 );

		attack = new MovieClip.Animation( 14, false );
		attack.frames( frames, 8, 9, 10, 11 );

		die = new MovieClip.Animation( 12, false );
		die.frames( frames, 12, 13, 14, 15 );

		play( idle );
	}

	@Override
	public int blood() {
		//黄色螃蟹与巨型螃蟹同色
		return 0xFFFFEA80;
	}
}
