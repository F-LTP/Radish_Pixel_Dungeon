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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.watabou.gltextures.AtlasFrame;
import com.watabou.gltextures.AtlasSource;
import com.watabou.gltextures.RuntimeAtlas;
import com.watabou.gltextures.RuntimeAtlasRegistry;
import com.watabou.noosa.Image;

public class BuffIcon extends Image {

	public static final AtlasSource SMALL_ATLAS =
			new AtlasSource("interfaces/buffs/small", "none");
	public static final AtlasSource LARGE_ATLAS =
			new AtlasSource("interfaces/buffs/large", "none");
	private static final RuntimeAtlas SMALL = RuntimeAtlasRegistry.get(SMALL_ATLAS);
	private static final RuntimeAtlas LARGE = RuntimeAtlasRegistry.get(LARGE_ATLAS);

	private final boolean large;

	public BuffIcon(Buff buff, boolean large){
		super();
		this.large = large;
		refresh(buff);
	}

	public BuffIcon(String icon, boolean large){
		super();
		this.large = large;
		refresh(icon);
	}

	public void refresh(Buff buff){
		refresh(buff.icon());
		buff.tintIcon(this);
	}

	public void refresh(String icon){
		AtlasFrame atlasFrame = (large ? LARGE : SMALL).frame(icon);
		texture = atlasFrame.texture;
		frame(atlasFrame.uv);
	}

}
