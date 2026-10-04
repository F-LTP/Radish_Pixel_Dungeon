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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//Reading this scroll lets the reader choose a type of item in their backpack
//(equipment, scrolls, potions or rings) and identifies up to 4 such items.
public class ScrollOfDivination extends ExoticScroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_DIVINATE;
	}

	@Override
	public void doRead() {

		detach(curUser.belongings.backpack);
		curUser.sprite.parent.add( new Identification( curUser.sprite.center().offset( 0, -16 ) ) );

		Sample.INSTANCE.play( Assets.Sounds.READ );

		identify();
		readAnimation(true);

		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(this, "prompt"),
				Messages.get(this, "equipment"),
				Messages.get(this, "scroll"),
				Messages.get(this, "potion"),
				Messages.get(this, "ring") ) {
			@Override
			protected void onSelect( int index ) {

				ArrayList<Item> ided;
				switch (index) {
					case 0: ided = identifyItems(EquipableItem.class, true); break;
					case 1: ided = identifyItems(Scroll.class, false); break;
					case 2: ided = identifyItems(Potion.class, false); break;
					default: ided = identifyItems(Ring.class, false); break;
				}

				if (ided.isEmpty()) {
					GLog.n( Messages.get(ScrollOfDivination.class, "nothing_left") );
				} else {
					GLog.p( Messages.get(ScrollOfDivination.class, "identified", ided.size()) );
					GameScene.show( new WndDivination(ided) );
				}
			}
		});
	}

	//identifies up to 4 unidentified items of the given type in the backpack
	private ArrayList<Item> identifyItems(Class<?> type, boolean excludeRings) {
		ArrayList<Item> ided = new ArrayList<>();
		for (Item item : curUser.belongings.backpack.items) {
			if (ided.size() >= 4) {
				break;
			}
			if (item == null || item.isIdentified() || !type.isInstance(item)) {
				continue;
			}
			if (excludeRings && item instanceof Ring) {
				continue;
			}
			item.identify();
			ided.add(item);
		}
		return ided;
	}

	private class WndDivination extends Window {

		private static final int WIDTH = 120;

		WndDivination(ArrayList<Item> IDed ){
			IconTitle cur = new IconTitle(new ItemSprite(ScrollOfDivination.this),
					Messages.titleCase(Messages.get(ScrollOfDivination.class, "name")));
			cur.setRect(0, 0, WIDTH, 0);
			add(cur);

			RenderedTextBlock msg = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
			msg.maxWidth(120);
			msg.setPos(0, cur.bottom() + 2);
			add(msg);

			float pos = msg.bottom() + 10;

			for (Item i : IDed){

				cur = new IconTitle(i);
				cur.setRect(0, pos, WIDTH, 0);
				add(cur);
				pos = cur.bottom() + 2;

			}

			resize(WIDTH, (int)pos);
		}

	}
}
