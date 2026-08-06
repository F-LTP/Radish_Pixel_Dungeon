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

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndInfoItem extends Window {
	
	private static final float GAP	= 2;
	private static final float DICE_PAD = 3;
	private static final int DICE_ICON = 28;

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;

	//only one WndInfoItem can appear at a time
	private static WndInfoItem INSTANCE;

	public WndInfoItem( Heap heap ) {

		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

		if (heap.type == Heap.Type.HEAP) {
			fillFields( heap.peek() );

		} else {
			fillFields( heap );

		}
	}
	
	public WndInfoItem( Item item ) {
		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;
		
		fillFields( item );
	}

	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}

	private void fillFields(Heap heap ) {
		if (DiceMageUI.active()) {
			layoutDiceHeap(heap);
			return;
		}
		
		IconTitle titlebar = new IconTitle( heap );
		titlebar.color( TITLE_COLOR );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( heap.info(), 6 );

		layoutFields(titlebar, txtInfo);
	}
	
	private void fillFields( Item item ) {
		if (DiceMageUI.active()) {
			layoutDiceItem(item);
			return;
		}
		
		int color = TITLE_COLOR;
		if (item.levelKnown && item.level() > 0) {
			color = ItemSlot.UPGRADED;
		} else if (item.levelKnown && item.level() < 0) {
			color = ItemSlot.DEGRADED;
		}

		IconTitle titlebar = new IconTitle( item );
		titlebar.color( color );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( item.info(), 6 );
		
		layoutFields(titlebar, txtInfo);
	}

	private void layoutFields(IconTitle title, RenderedTextBlock info){
		int width = WIDTH_MIN;

		info.maxWidth(width);

		//window can go out of the screen on landscape, so widen it as appropriate
		while (PixelScene.landscape()
				&& info.height() > 100
				&& width < WIDTH_MAX){
			width += 20;
			info.maxWidth(width);
		}

		title.setRect( 0, 0, width, 0 );
		add( title );

		info.setPos(title.left(), title.bottom() + GAP);
		add( info );

		resize( width, (int)(info.bottom() + 2) );
	}

	private void layoutDiceHeap(Heap heap) {
		int width = PixelScene.landscape() ? 150 : 132;
		chrome.hardlight(DiceMageUI.DARK);

		IconTitle titlebar = new IconTitle(heap);
		titlebar.color(DiceMageUI.GOLD);
		layoutDiceFields(titlebar, "[LOOT]", heap.info(), width, DiceMageUI.GREY_LINE);
	}

	private void layoutDiceItem(Item item) {
		int width = PixelScene.landscape() ? 150 : 132;
		chrome.hardlight(DiceMageUI.DARK);

		int lineColor = DiceMageUI.itemLineColor(item, item.isEquipped(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero));
		ItemSlot icon = new ItemSlot(item);
		icon.setRect(DICE_PAD, DICE_PAD, DICE_ICON, DICE_ICON);
		DiceMageUI.Frame iconFrame = new DiceMageUI.Frame(DiceMageUI.BLACK, lineColor);
		iconFrame.setRect(0, 0, DICE_ICON + DICE_PAD * 2, DICE_ICON + DICE_PAD * 2);
		add(iconFrame);
		add(icon);

		RenderedTextBlock title = PixelScene.renderTextBlock("[ITEM] " + item.name(), 8);
		title.hardlight(lineColor);
		title.maxWidth(width - DICE_ICON - (int)DICE_PAD * 4);
		title.setPos(DICE_ICON + DICE_PAD * 3, DICE_PAD);
		add(title);

		RenderedTextBlock tags = PixelScene.renderTextBlock(itemTags(item), 6);
		tags.hardlight(DiceMageUI.CREAM);
		tags.maxWidth(width - DICE_ICON - (int)DICE_PAD * 4);
		tags.setPos(title.left(), title.bottom() + 1);
		add(tags);

		float topHeight = Math.max(iconFrame.bottom(), tags.bottom()) + DICE_PAD;
		layoutDiceInfoBody(item.info(), width, topHeight, lineColor);
	}

	private void layoutDiceFields(IconTitle titlebar, String label, String body, int width, int lineColor) {
		DiceMageUI.Frame top = new DiceMageUI.Frame(DiceMageUI.BLACK, lineColor);
		top.setRect(0, 0, width, 34);
		add(top);

		RenderedTextBlock labelText = PixelScene.renderTextBlock(label, 8);
		labelText.hardlight(DiceMageUI.GOLD);
		labelText.setPos(DICE_PAD, DICE_PAD);
		add(labelText);

		titlebar.setRect(DICE_PAD, labelText.bottom() + 1, width - DICE_PAD * 2, 0);
		add(titlebar);

		layoutDiceInfoBody(body, width, Math.max(34, titlebar.bottom() + DICE_PAD), lineColor);
	}

	private void layoutDiceInfoBody(String body, int width, float top, int lineColor) {
		RenderedTextBlock info = PixelScene.renderTextBlock(body, 6);
		info.hardlight(DiceMageUI.CREAM);
		info.maxWidth((int)(width - DICE_PAD * 4));
		info.setPos(DICE_PAD * 2, top + DICE_PAD * 2);

		DiceMageUI.Frame bodyFrame = new DiceMageUI.Frame(DiceMageUI.PANEL, lineColor);
		bodyFrame.setRect(0, top + DICE_PAD, width, info.height() + DICE_PAD * 4);
		add(bodyFrame);
		add(info);

		resize(width, (int)(bodyFrame.bottom() + DICE_PAD));
	}

	private String itemTags(Item item) {
		String tags = "[card]";
		if (item.levelKnown && item.level() > 0) {
			tags += " +" + item.level();
		} else if (item.levelKnown && item.level() < 0) {
			tags += " " + item.level();
		}
		if (item.cursedKnown) {
			tags += item.cursed ? " cursed" : " clean";
		}
		if (!item.isIdentified()) {
			tags += " unknown";
		}
		return tags;
	}
}
