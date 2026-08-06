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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;

public class InventorySlot extends ItemSlot {

	private static final int NORMAL		= 0x9953564D;
	private static final int EQUIPPED	= 0x9991938C;

	private ColorBlock bg;
	private ColorBlock lineTop;
	private ColorBlock lineBottom;
	private ColorBlock lineLeft;
	private ColorBlock lineRight;

	public InventorySlot( Item item ) {

		super( item );
	}

	@Override
	protected void createChildren() {
		bg = new ColorBlock( 1, 1, NORMAL );
		add( bg );

		super.createChildren();

		lineTop = new ColorBlock( 1, 1, 0xFF000000 | DiceMageUI.GREY_LINE );
		add( lineTop );
		lineBottom = new ColorBlock( 1, 1, 0xFF000000 | DiceMageUI.GREY_LINE );
		add( lineBottom );
		lineLeft = new ColorBlock( 1, 1, 0xFF000000 | DiceMageUI.GREY_LINE );
		add( lineLeft );
		lineRight = new ColorBlock( 1, 1, 0xFF000000 | DiceMageUI.GREY_LINE );
		add( lineRight );
	}

	@Override
	protected void layout() {
		bg.size(width, height);
		bg.x = x;
		bg.y = y;

		lineTop.x = x;
		lineTop.y = y;
		lineTop.size(width, 1);

		lineBottom.x = x;
		lineBottom.y = y + height - 1;
		lineBottom.size(width, 1);

		lineLeft.x = x;
		lineLeft.y = y;
		lineLeft.size(1, height);

		lineRight.x = x + width - 1;
		lineRight.y = y;
		lineRight.size(1, height);

		super.layout();
	}

	@Override
	public void alpha(float value) {
		super.alpha(value);
		bg.alpha(value);
		lineTop.alpha(value);
		lineBottom.alpha(value);
		lineLeft.alpha(value);
		lineRight.alpha(value);
	}

	@Override
	public void item( Item item ) {

		super.item( item );

		boolean diceMage = DiceMageUI.active();
		bg.visible = diceMage || !(item instanceof Gold || item instanceof Bag);
		lineTop.visible = diceMage;
		lineBottom.visible = diceMage;
		lineLeft.visible = diceMage;
		lineRight.visible = diceMage;

		if (item != null) {

			boolean equipped = item.isEquipped(Dungeon.hero) ||
					item == Dungeon.hero.belongings.weapon ||
					item == Dungeon.hero.belongings.armor ||
					item == Dungeon.hero.belongings.artifact ||
					item == Dungeon.hero.belongings.misc ||
					item == Dungeon.hero.belongings.ring ||
					item == Dungeon.hero.belongings.secondWep;

			if (diceMage) {
				bg.texture( TextureCache.createSolid( equipped ? DiceMageUI.PANEL_ALT : DiceMageUI.BLACK ) );
				bg.resetColor();
				hardlightLines(DiceMageUI.itemLineColor(item, equipped));
			} else {
				bg.texture( TextureCache.createSolid( equipped ? EQUIPPED : NORMAL ) );
				bg.resetColor();
			}
			if (item.cursed && item.cursedKnown) {
				if (diceMage) {
					bg.ra = +0.10f;
					bg.ga = -0.05f;
				} else {
					bg.ra = +0.3f;
					bg.ga = -0.15f;
				}
			} else if (!item.isIdentified()) {
				if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown){
					bg.ba = diceMage ? 0.12f : 0.3f;
				} else {
					bg.ra = diceMage ? 0.12f : 0.3f;
					bg.ba = diceMage ? 0.12f : 0.3f;
				}
			}

			if (item.name() == null) {
				enable( false );
			} else if (Dungeon.hero.belongings.lostInventory()
					&& !item.keptThroughLostInventory()){
				enable(false);
			}
		} else {
			bg.texture( TextureCache.createSolid( diceMage ? DiceMageUI.BLACK : NORMAL ) );
			bg.resetColor();
			hardlightLines(DiceMageUI.GREY_LINE);
		}
	}

	private void hardlightLines(int color) {
		lineTop.hardlight(color);
		lineBottom.hardlight(color);
		lineLeft.hardlight(color);
		lineRight.hardlight(color);
	}

	public Item item(){
		return item;
	}

	@Override
	protected void onPointerDown() {
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
	}

	protected void onPointerUp() {
		bg.brightness( 1.0f );
	}

}
