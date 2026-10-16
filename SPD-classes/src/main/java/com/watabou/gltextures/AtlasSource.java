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

import java.util.Objects;

public final class AtlasSource {

	public final String directory;
	public final String fallback;

	public AtlasSource( String directory, String fallback ) {
		if (directory == null) {
			throw new IllegalArgumentException("Atlas directory must not be empty");
		}
		if (fallback == null || !fallback.matches("[a-z0-9_]+")) {
			throw new IllegalArgumentException("Invalid atlas fallback: " + fallback);
		}
		this.directory = trimTrailingSlashes( directory );
		if (this.directory.isEmpty() || this.directory.startsWith("/")
				|| this.directory.contains("\\") || this.directory.contains("..")) {
			throw new IllegalArgumentException("Invalid atlas directory: " + directory);
		}
		this.fallback = fallback;
	}

	private static String trimTrailingSlashes( String path ) {
		while (path.endsWith("/")) {
			path = path.substring( 0, path.length() - 1 );
		}
		return path;
	}

	@Override
	public boolean equals( Object other ) {
		if (this == other) return true;
		if (!(other instanceof AtlasSource)) return false;
		AtlasSource source = (AtlasSource) other;
		return directory.equals( source.directory ) && fallback.equals( source.fallback );
	}

	@Override
	public int hashCode() {
		return Objects.hash( directory, fallback );
	}
}
