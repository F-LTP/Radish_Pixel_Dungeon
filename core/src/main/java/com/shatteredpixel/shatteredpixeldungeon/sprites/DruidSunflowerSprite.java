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

/** 德鲁伊召唤物贴图：太阳花（11x22，素材来自 德鲁伊召唤物.zip）。 */
public class DruidSunflowerSprite extends MobSprite {

	{
		shadowWidth  = 0.7f;
		shadowHeight = 0.7f;
		shadowOffset = 1.1f;
	}

	public DruidSunflowerSprite() {
		texture( Assets.Sprites.DRUID_SUNFLOWER );

		TextureFilm frames = new TextureFilm( texture, 11, 22 );

		idle = new Animation( 8, true );
		idle.frames( frames, 0 );

		run = idle.clone();

		attack = idle.clone();

		zap = attack.clone();
		operate = idle.clone();

		die = new Animation( 8, false );
		die.frames( frames, 1, 2, 3 );

		play( idle );
	}

}
