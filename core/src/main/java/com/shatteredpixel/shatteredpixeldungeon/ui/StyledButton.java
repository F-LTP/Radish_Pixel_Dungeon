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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;

//simple button which support a background chrome, text, and an icon.
public class StyledButton extends Button {
	
	protected NinePatch bg;
	protected RenderedTextBlock text;
	protected Image icon;
	public boolean leftJustify = false;

	public boolean multiline;
	
	// dice-mode 1px border overlay
	private ColorBlock diceTop;
	private ColorBlock diceBottom;
	private ColorBlock diceLeft;
	private ColorBlock diceRight;
	
	public StyledButton(Chrome.Type type, String label ) {
		this(type, label, 9);
	}
	
	public StyledButton(Chrome.Type type, String label, int size ){
		super();
		
		bg = Chrome.get( type );
		addToBack( bg );
		
		text = PixelScene.renderTextBlock( size );
		text.text( label );
		add( text );
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();

		diceTop = new ColorBlock(1, 1, 0xFFFFFFFF);
		diceTop.hardlight(DiceMageUI.GREY_LINE);
		diceTop.visible = false;
		add(diceTop);

		diceBottom = new ColorBlock(1, 1, 0xFFFFFFFF);
		diceBottom.hardlight(DiceMageUI.GREY_LINE);
		diceBottom.visible = false;
		add(diceBottom);

		diceLeft = new ColorBlock(1, 1, 0xFFFFFFFF);
		diceLeft.hardlight(DiceMageUI.GREY_LINE);
		diceLeft.visible = false;
		add(diceLeft);

		diceRight = new ColorBlock(1, 1, 0xFFFFFFFF);
		diceRight.hardlight(DiceMageUI.GREY_LINE);
		diceRight.visible = false;
		add(diceRight);
	}
	
	@Override
	protected void layout() {
		
		super.layout();
		
		bg.x = x;
		bg.y = y;
		bg.size( width, height );

		boolean dice = DiceMageUI.active();
		diceTop.visible = dice;
		diceBottom.visible = dice;
		diceLeft.visible = dice;
		diceRight.visible = dice;

		if (dice) {
			bg.hardlight(DiceMageUI.BLACK);
			diceTop.x = x;
			diceTop.y = y;
			diceTop.size(width, 1);

			diceBottom.x = x;
			diceBottom.y = y + height - 1;
			diceBottom.size(width, 1);

			diceLeft.x = x;
			diceLeft.y = y;
			diceLeft.size(1, height);

			diceRight.x = x + width - 1;
			diceRight.y = y;
			diceRight.size(1, height);
		}
		
		float componentWidth = 0;
		
		if (icon != null) componentWidth += icon.width() + 2;
		
		if (text != null && !text.text().equals("")){
			if (multiline) text.maxWidth( (int)(width - componentWidth - bg.marginHor() - 2));
			componentWidth += text.width() + 2;

			text.setPos(
					x + (width() + componentWidth)/2f - text.width() - 1,
					y + (height() - text.height()) / 2f
			);
			PixelScene.align(text);
			
		}
		
		if (icon != null) {
			
			icon.x = x + (width() - componentWidth)/2f + 1;
			icon.y = y + (height() - icon.height()) / 2f;
			PixelScene.align(icon);
		}

		if (leftJustify){
			if (icon != null){
				icon.x = x + bg.marginLeft() + 1;
				PixelScene.align(icon);
				text.setPos( icon.x + icon.width() + 1, text.top());
				PixelScene.align(text);
			} else if (text != null) {
				text.setPos( x + bg.marginLeft() + 1, text.top());
				PixelScene.align(text);
			}
		}

	}
	
	@Override
	protected void onPointerDown() {
		bg.brightness( 1.2f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );

		if (diceTop.visible) {
			diceTop.hardlight(DiceMageUI.GOLD);
			diceBottom.hardlight(DiceMageUI.GOLD);
			diceLeft.hardlight(DiceMageUI.GOLD);
			diceRight.hardlight(DiceMageUI.GOLD);
		}
	}
	
	@Override
	protected void onPointerUp() {
		if (DiceMageUI.active()) {
			bg.hardlight(DiceMageUI.BLACK);
		} else {
			bg.resetColor();
		}

		if (diceTop.visible) {
			diceTop.hardlight(DiceMageUI.GREY_LINE);
			diceBottom.hardlight(DiceMageUI.GREY_LINE);
			diceLeft.hardlight(DiceMageUI.GREY_LINE);
			diceRight.hardlight(DiceMageUI.GREY_LINE);
		}
	}
	
	public void enable( boolean value ) {
		active = value;
		text.alpha( value ? 1.0f : 0.3f );
		if (icon != null) icon.alpha( value ? 1.0f : 0.3f );
	}
	
	public void text( String value ) {
		text.text( value );
		layout();
	}

	public String text(){
		return text.text();
	}
	
	public void textColor( int value ) {
		text.hardlight( value );
	}
	
	public void icon( Image icon ) {
		if (this.icon != null) {
			remove( this.icon );
		}
		this.icon = icon;
		if (this.icon != null) {
			add( this.icon );
			layout();
		}
	}
	
	public Image icon(){
		return icon;
	}

	public void alpha(float value){
		if (icon != null) icon.alpha(value);
		if (bg != null)   bg.alpha(value);
		if (text != null) text.alpha(value);
	}

	public float alpha(){
		if (icon != null)   return icon.alpha();
		else                return bg.alpha();
	}
	
	public float reqWidth() {
		float reqWidth = 0;
		if (icon != null){
			reqWidth += icon.width() + 2;
		}
		if (text != null && !text.text().equals("")){
			reqWidth += text.width() + 2;
		}
		return reqWidth;
	}
	
	public float reqHeight() {
		float reqHeight = 0;
		if (icon != null){
			reqHeight = Math.max(icon.height() + 4, reqHeight);
		}
		if (text != null && !text.text().equals("")){
			reqHeight = Math.max(text.height() + 4, reqHeight);
		}
		return reqHeight;
	}
}
