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

/** 德鲁伊召唤物贴图：荆棘藤（28x24，素材来自 德鲁伊召唤物.zip）。 */
public class DruidVinesSprite extends MobSprite {

	{
		shadowWidth  = 1.0f;
		shadowHeight = 0.6f;
		shadowOffset = 1.1f;
	}

	public DruidVinesSprite() {
		texture( Assets.Sprites.DRUID_VINES );

		TextureFilm frames = new TextureFilm( texture, 28, 24 );

		idle = new Animation( 8, true );
		idle.frames( frames, 0, 1, 2, 3 );

		run = idle.clone();

		attack = new Animation( 8, false );
		attack.frames( frames, 4, 5, 6, 7 );

		zap = attack.clone();
		operate = idle.clone();

		die = new Animation( 8, false );
		die.frames( frames, 8, 9, 10, 11, 12 );

		play( idle );
	}

}
