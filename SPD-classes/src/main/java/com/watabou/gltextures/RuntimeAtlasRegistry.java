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

import java.util.HashMap;
import java.util.Map;

public final class RuntimeAtlasRegistry {

	private static final Map<AtlasSource, RuntimeAtlas> atlases = new HashMap<>();

	private RuntimeAtlasRegistry() {
	}

	public static synchronized RuntimeAtlas get( AtlasSource source ) {
		RuntimeAtlas atlas = atlases.get( source );
		if (atlas == null) {
			atlas = new RuntimeAtlas( source );
			atlases.put( source, atlas );
		}
		return atlas;
	}

	public static synchronized void invalidateAll() {
		for (RuntimeAtlas atlas : atlases.values()) {
			atlas.invalidate();
		}
	}

	public static synchronized void disposeAll() {
		for (RuntimeAtlas atlas : atlases.values()) {
			atlas.dispose();
		}
		atlases.clear();
	}
}
