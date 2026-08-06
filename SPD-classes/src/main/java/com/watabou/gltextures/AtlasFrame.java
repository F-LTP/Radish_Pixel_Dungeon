/*
 * Radish Pixel Dungeon
 * Copyright (C) 2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.watabou.gltextures;

import com.watabou.utils.RectF;

public final class AtlasFrame {

	public final SmartTexture texture;
	public final RectF uv;
	public final int width;
	public final int height;

	AtlasFrame( SmartTexture texture, RectF uv, int width, int height ) {
		this.texture = texture;
		this.uv = uv;
		this.width = width;
		this.height = height;
	}
}
