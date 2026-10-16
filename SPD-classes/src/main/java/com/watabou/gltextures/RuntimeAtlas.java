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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.noosa.Game;
import com.watabou.utils.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class RuntimeAtlas {

	private static final String TAG = "RuntimeAtlas";
	private static final int BORDER = 1;
	private static final int MIN_PAGE_SIZE = 32;
	private static final int DEFAULT_MAX_TEXTURE_SIZE = 2048;

	private final AtlasSource source;
	private final Map<String, AtlasFrame> frames = new HashMap<>();
	private final Map<String, AtlasFrame> trimmedFrames = new HashMap<>();
	private final List<Object> textureKeys = new ArrayList<>();
	private final Set<String> warnedNames = new HashSet<>();

	private AtlasFrame errorFrame;
	private boolean built;
	private long cacheGeneration = -1;

	RuntimeAtlas( AtlasSource source ) {
		this.source = source;
	}

	public synchronized AtlasFrame frame( String name ) {
		ensureBuilt();
		String normalized = normalizeName( name );
		AtlasFrame frame = normalized == null ? null : frames.get( normalized );
		if (frame != null) return frame;

		warnMissingOnce( String.valueOf( name ) );
		frame = frames.get( source.fallback );
		return frame != null ? frame : errorFrame;
	}

	public synchronized boolean contains( String name ) {
		ensureBuilt();
		String normalized = normalizeName( name );
		return normalized != null && frames.containsKey( normalized );
	}

	/**
	 * Returns the smallest frame containing pixels at or above minAlpha.
	 * Empty frames retain their original bounds.
	 */
	public synchronized AtlasFrame trimmedFrame( String name, int minAlpha ) {
		if (minAlpha < 0 || minAlpha > 255) {
			throw new IllegalArgumentException("Alpha threshold must be between 0 and 255");
		}
		AtlasFrame original = frame( name );
		String normalized = normalizeName( name );
		String cacheKey = (normalized == null ? source.fallback : normalized) + ":" + minAlpha;
		AtlasFrame cached = trimmedFrames.get( cacheKey );
		if (cached != null && cached.texture == original.texture) return cached;

		int frameLeft = Math.round( original.uv.left * original.texture.width );
		int frameTop = Math.round( original.uv.top * original.texture.height );
		int minX = original.width;
		int minY = original.height;
		int maxX = -1;
		int maxY = -1;
		for (int y = 0; y < original.height; y++) {
			for (int x = 0; x < original.width; x++) {
				int alpha = original.texture.getPixel( frameLeft + x, frameTop + y ) >>> 24;
				if (alpha >= minAlpha) {
					minX = Math.min( minX, x );
					minY = Math.min( minY, y );
					maxX = Math.max( maxX, x );
					maxY = Math.max( maxY, y );
				}
			}
		}

		if (maxX < minX || maxY < minY) return original;
		AtlasFrame trimmed = new AtlasFrame(
				original.texture,
				original.texture.uvRect(
						frameLeft + minX,
						frameTop + minY,
						frameLeft + maxX + 1,
						frameTop + maxY + 1 ),
				maxX - minX + 1,
				maxY - minY + 1 );
		trimmedFrames.put( cacheKey, trimmed );
		return trimmed;
	}

	public synchronized int pixel( String name, int x, int y ) {
		AtlasFrame frame = frame( name );
		if (x < 0 || y < 0 || x >= frame.width || y >= frame.height) {
			throw new IndexOutOfBoundsException("Pixel outside atlas frame: " + x + "," + y);
		}
		int left = Math.round( frame.uv.left * frame.texture.width );
		int top = Math.round( frame.uv.top * frame.texture.height );
		return frame.texture.getPixel( left + x, top + y );
	}

	public synchronized void invalidate() {
		removeCachedTextures();
		clearState();
	}

	private void clearState() {
		built = false;
		frames.clear();
		trimmedFrames.clear();
		errorFrame = null;
		warnedNames.clear();
	}

	public synchronized void dispose() {
		removeCachedTextures();
		clearState();
		cacheGeneration = TextureCache.generation();
	}

	private void removeCachedTextures() {
		if (cacheGeneration == TextureCache.generation()) {
			for (Object key : textureKeys) TextureCache.remove( key );
		}
		textureKeys.clear();
	}

	private void ensureBuilt() {
		long currentGeneration = TextureCache.generation();
		if (cacheGeneration != currentGeneration) {
			textureKeys.clear();
			clearState();
			cacheGeneration = currentGeneration;
		}
		if (built) return;

		try {
			build();
		} catch (RuntimeException exception) {
			Game.reportException( new RuntimeException(
					"Unable to build runtime atlas " + source.directory, exception ) );
			removeCachedTextures();
			frames.clear();
			createErrorFrame();
		}
		built = true;
	}

	private void build() {
		List<String> ids = readManifest();
		Map<String, SourceImage> images = loadImages( ids );
		try {
			pack( images );
		} finally {
			for (SourceImage image : images.values()) {
				image.pixmap.dispose();
			}
		}
		if (!frames.containsKey( source.fallback )) {
			Gdx.app.error( TAG, "Fallback is missing: " + source.directory + "/" + source.fallback + ".png" );
			createErrorFrame();
		}
	}

	private List<String> readManifest() {
		FileHandle manifest = Gdx.files.internal( source.directory + "/manifest.txt" );
		if (!manifest.exists()) {
			throw new IllegalStateException("Manifest does not exist: " + manifest.path());
		}

		List<String> ids = new ArrayList<>();
		Set<String> unique = new HashSet<>();
		String[] lines = manifest.readString( "UTF-8" ).split( "\\r?\\n" );
		for (int i = 0; i < lines.length; i++) {
			String entry = lines[i].trim();
			if (entry.isEmpty() || entry.startsWith("#")) continue;
			if (!entry.matches("[a-z0-9_]+\\.png")) {
				throw new IllegalArgumentException("Invalid manifest entry at line " + (i + 1) + ": " + entry);
			}
			String id = entry.substring( 0, entry.length() - 4 );
			if (!unique.add( id )) {
				throw new IllegalArgumentException("Duplicate atlas ID: " + id);
			}
			ids.add( id );
		}
		return ids;
	}

	private Map<String, SourceImage> loadImages( List<String> ids ) {
		Map<String, SourceImage> images = new LinkedHashMap<>();
		try {
			for (String id : ids) {
				FileHandle file = Gdx.files.internal( source.directory + "/" + id + ".png" );
				if (!file.exists()) throw new IllegalStateException("Atlas image does not exist: " + file.path());
				Pixmap pixmap = new Pixmap( file );
				if (pixmap.getWidth() <= 0 || pixmap.getHeight() <= 0) {
					pixmap.dispose();
					throw new IllegalArgumentException("Atlas image is empty: " + file.path());
				}
				images.put( id, new SourceImage( id, pixmap ) );
			}
			return images;
		} catch (RuntimeException exception) {
			for (SourceImage image : images.values()) image.pixmap.dispose();
			throw exception;
		}
	}

	private void pack( Map<String, SourceImage> imageMap ) {
		List<SourceImage> images = new ArrayList<>( imageMap.values() );
		Collections.sort( images, new Comparator<SourceImage>() {
			@Override
			public int compare( SourceImage a, SourceImage b ) {
				int result = Integer.compare( b.maxEdge, a.maxEdge );
				if (result == 0) result = Long.compare( b.area, a.area );
				if (result == 0) result = a.id.compareTo( b.id );
				return result;
			}
		} );

		if (images.isEmpty()) return;
		// Atlases can be built while InterlevelScene generates a level off the render
		// thread. Do not query GL here: that thread has no current OpenGL context.
		int maxTextureSize = DEFAULT_MAX_TEXTURE_SIZE;
		long totalArea = 0;
		int largest = 0;
		for (SourceImage image : images) {
			int packedWidth = image.width + BORDER * 2;
			int packedHeight = image.height + BORDER * 2;
			if (packedWidth > maxTextureSize || packedHeight > maxTextureSize) {
				throw new IllegalArgumentException("Atlas image exceeds maximum texture size: " + image.id);
			}
			totalArea += (long) packedWidth * packedHeight;
			largest = Math.max( largest, Math.max( packedWidth, packedHeight ) );
		}

		int pageSize = nextPowerOfTwo( Math.max( largest, (int)Math.ceil( Math.sqrt( totalArea ) ) ) );
		pageSize = Math.max( MIN_PAGE_SIZE, Math.min( pageSize, maxTextureSize ) );
		List<Page> pages = new ArrayList<>();
		for (SourceImage image : images) {
			Placement placement = null;
			for (Page page : pages) {
				placement = page.place( image );
				if (placement != null) break;
			}
			if (placement == null) {
				Page page = new Page( pageSize );
				pages.add( page );
				placement = page.place( image );
			}
			if (placement == null) throw new IllegalStateException("Unable to pack atlas image: " + image.id);
		}

		for (int i = 0; i < pages.size(); i++) createPage( pages.get( i ), i );
	}

	private void createPage( Page page, int index ) {
		Pixmap atlas = new Pixmap( page.size, page.size, Pixmap.Format.RGBA8888 );
		for (Placement placement : page.placements) {
			int x = placement.x + BORDER;
			int y = placement.y + BORDER;
			atlas.drawPixmap( placement.image.pixmap, x, y );
			extrudeEdges( atlas, placement.image.pixmap, x, y );
		}

		Object key = new TextureKey( source, index );
		SmartTexture texture = TextureCache.put( key, new SmartTexture( atlas ) );
		textureKeys.add( key );
		for (Placement placement : page.placements) {
			int x = placement.x + BORDER;
			int y = placement.y + BORDER;
			frames.put( placement.image.id, new AtlasFrame(
					texture,
					texture.uvRectBySize( x, y, placement.image.width, placement.image.height ),
					placement.image.width,
					placement.image.height ) );
		}
	}

	private static void extrudeEdges( Pixmap atlas, Pixmap image, int x, int y ) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int px = 0; px < width; px++) {
			atlas.drawPixel( x + px, y - 1, image.getPixel( px, 0 ) );
			atlas.drawPixel( x + px, y + height, image.getPixel( px, height - 1 ) );
		}
		for (int py = 0; py < height; py++) {
			atlas.drawPixel( x - 1, y + py, image.getPixel( 0, py ) );
			atlas.drawPixel( x + width, y + py, image.getPixel( width - 1, py ) );
		}
		atlas.drawPixel( x - 1, y - 1, image.getPixel( 0, 0 ) );
		atlas.drawPixel( x + width, y - 1, image.getPixel( width - 1, 0 ) );
		atlas.drawPixel( x - 1, y + height, image.getPixel( 0, height - 1 ) );
		atlas.drawPixel( x + width, y + height, image.getPixel( width - 1, height - 1 ) );
	}

	private void createErrorFrame() {
		if (errorFrame != null) return;
		Pixmap pixmap = new Pixmap( 2, 2, Pixmap.Format.RGBA8888 );
		pixmap.drawPixel( 0, 0, 0xFF00FFFF );
		pixmap.drawPixel( 1, 0, 0x000000FF );
		pixmap.drawPixel( 0, 1, 0x000000FF );
		pixmap.drawPixel( 1, 1, 0xFF00FFFF );
		Object key = new TextureKey( source, -1 );
		SmartTexture texture = TextureCache.put( key, new SmartTexture( pixmap ) );
		textureKeys.add( key );
		errorFrame = new AtlasFrame( texture, new RectF( 0, 0, 1, 1 ), 2, 2 );
	}

	private static int nextPowerOfTwo( int value ) {
		int result = 1;
		while (result < value && result > 0) result <<= 1;
		return result > 0 ? result : Integer.MAX_VALUE;
	}

	private static String normalizeName( String name ) {
		if (name == null) return null;
		String normalized = name.toLowerCase( Locale.ROOT );
		return normalized.matches("[a-z0-9_]+") ? normalized : null;
	}

	private void warnMissingOnce( String name ) {
		if (warnedNames.add( name )) {
			Gdx.app.error( TAG, "Frame not found in " + source.directory + ": " + name );
		}
	}

	private static final class SourceImage {
		final String id;
		final Pixmap pixmap;
		final int width;
		final int height;
		final int maxEdge;
		final long area;

		SourceImage( String id, Pixmap pixmap ) {
			this.id = id;
			this.pixmap = pixmap;
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			maxEdge = Math.max( width, height );
			area = (long) width * height;
		}
	}

	private static final class Placement {
		final SourceImage image;
		final int x;
		final int y;

		Placement( SourceImage image, int x, int y ) {
			this.image = image;
			this.x = x;
			this.y = y;
		}
	}

	private static final class Page {
		final int size;
		final List<Placement> placements = new ArrayList<>();
		int x;
		int y;
		int rowHeight;

		Page( int size ) {
			this.size = size;
		}

		Placement place( SourceImage image ) {
			int width = image.width + BORDER * 2;
			int height = image.height + BORDER * 2;
			if (x + width > size) {
				x = 0;
				y += rowHeight;
				rowHeight = 0;
			}
			if (y + height > size) return null;
			Placement placement = new Placement( image, x, y );
			placements.add( placement );
			x += width;
			rowHeight = Math.max( rowHeight, height );
			return placement;
		}
	}

	private static final class TextureKey {
		final AtlasSource source;
		final int page;

		TextureKey( AtlasSource source, int page ) {
			this.source = source;
			this.page = page;
		}

		@Override
		public boolean equals( Object other ) {
			if (this == other) return true;
			if (!(other instanceof TextureKey)) return false;
			TextureKey key = (TextureKey) other;
			return page == key.page && source.equals( key.source );
		}

		@Override
		public int hashCode() {
			return 31 * source.hashCode() + page;
		}
	}
}
