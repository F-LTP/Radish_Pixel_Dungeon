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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

/** 德鲁伊召唤物贴图：熊（32x21，素材来自 德鲁伊召唤物.zip）。 */
public class DruidBearSprite extends MobSprite {

	{
		shadowWidth  = 1.2f;
		shadowHeight = 1.2f;
		shadowOffset = 1.2f;
	}

	public DruidBearSprite() {
		texture( Assets.Sprites.DRUID_BEAR );

		TextureFilm frames = new TextureFilm( texture, 32, 21 );

		idle = new Animation( 8, true );
		idle.frames( frames, 0, 1 );

		run = new Animation( 8, true );
		run.frames( frames, 2, 3, 4, 5, 6 );

		attack = new Animation( 8, false );
		attack.frames( frames, 7, 8, 9 );

		zap = attack.clone();
		operate = idle.clone();

		die = new Animation( 8, false );
		die.frames( frames, 10, 11, 12, 13 );

		play( idle );
	}

}
