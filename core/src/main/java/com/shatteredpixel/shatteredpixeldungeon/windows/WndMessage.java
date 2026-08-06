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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndMessage extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 16;
	
	public WndMessage( String text ) {
		
		super();

		if (DiceMageUI.active()) {
			layoutDiceNotice(text);
			return;
		}
		
		RenderedTextBlock info = PixelScene.renderTextBlock( text, 6 );
		info.maxWidth((PixelScene.landscape() ? WIDTH_L : WIDTH_P) - MARGIN * 2);
		info.setPos(MARGIN, MARGIN);
		add( info );

		resize(
			(int)info.width() + MARGIN * 2,
			(int)info.height() + MARGIN * 2 );
	}

	private void layoutDiceNotice(String text) {
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		chrome.hardlight(DiceMageUI.DARK);

		RenderedTextBlock label = PixelScene.renderTextBlock("[MESSAGE]", 8);
		label.hardlight(DiceMageUI.GOLD);
		label.setPos(MARGIN, MARGIN);
		add(label);

		RenderedTextBlock info = PixelScene.renderTextBlock(text, 6);
		info.hardlight(DiceMageUI.CREAM);
		info.maxWidth(width - MARGIN * 4);
		info.setPos(MARGIN * 2, label.bottom() + MARGIN * 2);

		DiceMageUI.Frame messageFrame = new DiceMageUI.Frame(DiceMageUI.PANEL, DiceMageUI.GREY_LINE);
		messageFrame.setRect(0, label.bottom() + MARGIN, width, info.height() + MARGIN * 4);
		add(messageFrame);
		add(info);

		DiceMageUI.DiceButton ok = new DiceMageUI.DiceButton("哦") {
			@Override
			protected void onClick() {
				hide();
			}
		};
		ok.setRect((width - 30) / 2f, messageFrame.bottom() + MARGIN, 30, BUTTON_HEIGHT);
		add(ok);

		resize(width, (int)(ok.bottom() + MARGIN));
	}
}
