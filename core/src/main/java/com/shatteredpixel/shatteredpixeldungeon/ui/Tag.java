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

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;

public class Tag extends Button {

	private float r;
	private float g;
	private float b;
	protected NinePatch bg;
	private RoundedFrame themedBg;
	
	protected float lightness = 0;

	public static int SIZE = 24;

	protected boolean flipped = false;
	
	public Tag( int color ) {
		super();
		
		this.r = (color >> 16) / 255f;
		this.g = ((color >> 8) & 0xFF) / 255f;
		this.b = (color & 0xFF) / 255f;
	}
	
	@Override
	protected void createChildren() {
		
		super.createChildren();
		
		bg = Chrome.get( Chrome.Type.TAG );
		bg.hardlight( r, g, b );
		add( bg );

		themedBg = UITheme.roundedFrame(DiceMageUI.BLACK, DiceMageUI.ORANGE);
		themedBg.visible = false;
		add(themedBg);
	}

	@Override
	protected void onClick() {
		GameScene.tagDisappeared = false;
	}

	@Override
	protected void layout() {
		
		super.layout();
		
		bg.x = x;
		bg.y = y;
		bg.size( width, height );
		boolean themed = UITheme.isDiceMage();
		// Keep the original tag visible for state checks, but make it transparent.
		bg.alpha(themed ? 0f : 1f);
		themedBg.visible = themed && bg.visible;
		themedBg.setRect(x, y, width, height);
	}

	protected void backgroundVisible(boolean value) {
		bg.visible = value;
		themedBg.visible = value && UITheme.isDiceMage();
	}
	
	public void flash() {
		lightness = 1f;
	}

	public void flip(boolean value){
		flipped = value;
		bg.flipHorizontal(value);
		layout();
	}

	public void setColor( int color ){
		this.r = (color >> 16) / 255f;
		this.g = ((color >> 8) & 0xFF) / 255f;
		this.b = (color & 0xFF) / 255f;
		bg.hardlight( r, g, b );
	}
	
	@Override
	public void update() {
		super.update();
		themedBg.visible = UITheme.isDiceMage() && bg.visible;
		
		if (visible && lightness > 0.5) {
			if ((lightness -= Game.elapsed) > 0.5) {
				bg.ra = bg.ga = bg.ba = 2 * lightness - 1;
				bg.rm = 2 * r * (1 - lightness);
				bg.gm = 2 * g * (1 - lightness);
				bg.bm = 2 * b * (1 - lightness);
			} else {
				bg.hardlight( r, g, b );
			}
		}
	}
}
