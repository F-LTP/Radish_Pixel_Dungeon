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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.gltextures.AtlasFrame;
import com.watabou.gltextures.AtlasSource;
import com.watabou.gltextures.RuntimeAtlas;
import com.watabou.gltextures.RuntimeAtlasRegistry;
import com.watabou.noosa.Image;

public class TalentIcon extends Image {

	public static final AtlasSource ATLAS_SOURCE =
			new AtlasSource("interfaces/talents", "developing");
	private static final RuntimeAtlas ATLAS = RuntimeAtlasRegistry.get(ATLAS_SOURCE);

	public TalentIcon(Talent talent){
		this(talent.icon());
	}

	public TalentIcon(String icon){
		super();
		AtlasFrame atlasFrame = ATLAS.frame(icon);
		texture = atlasFrame.texture;
		frame(atlasFrame.uv);
	}

}
