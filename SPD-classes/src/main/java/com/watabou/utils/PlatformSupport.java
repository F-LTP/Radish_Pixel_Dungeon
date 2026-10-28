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

package com.watabou.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.watabou.noosa.Game;

import java.io.File;
import java.util.HashMap;

public abstract class PlatformSupport {

	public abstract boolean supportsVibration();
	
	public abstract void updateDisplaySize();
	
	public abstract void updateSystemUI();

	public abstract boolean connectedToUnmeteredNetwork();

	public void vibrate( int millis ){
		//regular GDX vibration by default
		Gdx.input.vibrate( millis );
	}

	public void setHonorSilentSwitch( boolean value ){
		//does nothing by default
	}

	public boolean openURI( String uri ){
		return Gdx.net.openURI( uri );
	}

	public void setOnscreenKeyboardVisible(boolean value){
		Gdx.input.setOnscreenKeyboardVisible(value);
	}

	//TODO should consider spinning this into its own class, rather than platform support getting ever bigger
	protected static HashMap<FreeTypeFontGenerator, HashMap<Integer, BitmapFont>> fonts;
	protected static HashMap<Integer, BitmapFont> tannFonts;
	private static FreeTypeFontGenerator tannFontGenerator;
	private static boolean tannFontMode;

	protected int pageSize;
	protected PixmapPacker packer;
	protected boolean systemfont;
	
	public abstract void setupFontGenerators(int pageSize, boolean systemFont );

	protected abstract FreeTypeFontGenerator getGeneratorForString( String input );

	public abstract String[] splitforTextBlock( String text, boolean multiline );

	public void setTannFontMode(boolean value){
		if (tannFontMode != value) {
			tannFontMode = value;
			disposeTannFonts();
			reloadGenerators();
		}
	}

	public boolean tannFontMode(){
		return tannFontMode;
	}

	private void disposeTannFonts(){
		if (tannFonts != null) {
			for (BitmapFont f : tannFonts.values()) {
				f.dispose();
			}
			tannFonts.clear();
			tannFonts = null;
		}
		if (tannFontGenerator != null) {
			tannFontGenerator.dispose();
			tannFontGenerator = null;
		}
	}

	public void resetGenerators(){
		resetGenerators( true );
	}

	public void resetGenerators( boolean setupAfter ){
		disposeTannFonts();
		if (fonts != null) {
			for (FreeTypeFontGenerator generator : fonts.keySet()) {
				for (BitmapFont f : fonts.get(generator).values()) {
					f.dispose();
				}
				fonts.get(generator).clear();
				generator.dispose();
			}
			fonts.clear();
			if (packer != null) {
				for (PixmapPacker.Page p : packer.getPages()) {
					p.getTexture().dispose();
				}
				packer.dispose();
			}
			fonts = null;
		}
		if (setupAfter) setupFontGenerators(pageSize, systemfont);
	}

	public void reloadGenerators(){
		disposeTannFonts();
		if (packer != null) {
			for (FreeTypeFontGenerator generator : fonts.keySet()) {
				for (BitmapFont f : fonts.get(generator).values()) {
					f.dispose();
				}
				fonts.get(generator).clear();
			}
			if (packer != null) {
				for (PixmapPacker.Page p : packer.getPages()) {
					p.getTexture().dispose();
				}
				packer.dispose();
			}
			packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
		}
	}

	//flipped is needed because Shattered's graphics are y-down, while GDX graphics are y-up.
	//this is very confusing, I know.
	public BitmapFont getFont(int size, String text, boolean flipped, boolean border) {
		if (tannFontMode) {
			return getTannFont(size, flipped, border);
		}

		FreeTypeFontGenerator generator = getGeneratorForString(text);

		if (generator == null){
			return null;
		}

		int key = size;
		if (border) key += Short.MAX_VALUE; //surely we'll never have a size above 32k
		if (flipped) key = -key;
		if (!fonts.get(generator).containsKey(key)) {
			FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameters.size = size;
			parameters.flip = flipped;
			if (border) {
				parameters.borderWidth = parameters.size / 10f;
			}
			if (size >= 20){
				parameters.renderCount = 2;
			} else {
				parameters.renderCount = 3;
			}
			parameters.hinting = FreeTypeFontGenerator.Hinting.None;
			parameters.spaceX = -(int) parameters.borderWidth;
			parameters.incremental = true;
			parameters.characters = "�";
			parameters.packer = packer;

			try {
				BitmapFont font = generator.generateFont(parameters);
				font.getData().missingGlyph = font.getData().getGlyph('�');
				fonts.get(generator).put(key, font);
			} catch ( Exception e ){
				Game.reportException(e);
				return null;
			}
		}

		return fonts.get(generator).get(key);
	}

	private BitmapFont getTannFont(int size, boolean flipped, boolean border) {
		if (tannFonts == null) {
			tannFonts = new HashMap<>();
			tannFontGenerator = new FreeTypeFontGenerator(
					Gdx.files.internal("fonts/fusion_pixel_10px_zh_hans.ttf"));
		}

		int key = size * 4 + (flipped ? 1 : 0) + (border ? 2 : 0);
		if (!tannFonts.containsKey(key)) {
			FreeTypeFontGenerator.FreeTypeFontParameter parameters =
					new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameters.size = 10;
			parameters.flip = flipped;
			parameters.incremental = true;
			parameters.hinting = FreeTypeFontGenerator.Hinting.None;
			parameters.minFilter = Texture.TextureFilter.Nearest;
			parameters.magFilter = Texture.TextureFilter.Nearest;
			BitmapFont font = tannFontGenerator.generateFont(parameters);
			font.getData().setScale(size / 10f);
			font.setUseIntegerPositions(false);
			font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
			tannFonts.put(key, font);
		}

		return tannFonts.get(key);
	}


	// 更新游戏
	public abstract void updateGame(String url, UpdateCallback listener);

	public abstract void install(File file);
	public interface UpdateCallback {

		/**
		 * 最开始调用(在onStart之前调用)
		 *
		 * @param isDownloading 为true时，表示已经在下载；为false时，表示当前未开始下载，即将开始下载
		 */
		void onDownloading(boolean isDownloading);

		/**
		 * 开始
		 */
		void onStart(String url);

		/**
		 * 更新进度
		 *
		 * @param progress  当前进度大小
		 * @param total     总文件大小
		 * @param isChanged 进度百分比是否有改变，（主要可以用来过滤无用的刷新，从而降低刷新频率）
		 */
		void onProgress(long progress, long total, boolean isChanged);

		/**
		 * 完成
		 *
		 * @param file APK文件
		 */
		void onFinish(File file);

		/**
		 * 错误
		 *
		 * @param e 异常
		 */
		void onError(Exception e);

		/**
		 * 取消
		 */
		void onCancel();
	}


}
