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

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StoneOfDisarming extends InventoryStone {
	
	private static final int DIST = 8;
	public static final String AC_SELECT = "SELECT";

	{
		image = ItemSpriteSheet.STONE_DISARM;
	}

	@Override
	public String defaultAction(){
		return AC_SELECT;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_SELECT)){
			GameScene.show(
					new WndOptions(new ItemSprite(this),
							Messages.get(StoneOfDisarming.class, "palf"),
							Messages.get(StoneOfDisarming.class, "select"),
							Messages.get(StoneOfDisarming.class, "check_magic"),
							Messages.get(StoneOfDisarming.class, "check"),
							Messages.get(Potion.class, "no") ) {
						@Override
						protected void onSelect(int index) {
							if (index == 0) {
								activate(hero.pos);
							} else if (index == 1){
								GameScene.selectCell(thrower);
							}
						}
					}
			);
		}
	}

	@Override
	public boolean usableOnItem(Item item){
		return (item instanceof EquipableItem || item instanceof Wand)
				&& (!item.isIdentified() || !item.cursedKnown);
	}

	@Override
	protected void onItemSelected(Item item) {

		item.cursedKnown = true;
		useAnimation();

		boolean negativeMagic = false;
		boolean positiveMagic = false;

		negativeMagic = item.cursed;
		if (!negativeMagic){
			if (item instanceof Weapon && ((Weapon) item).hasCurseEnchant()){
				negativeMagic = true;
			} else if (item instanceof Armor && ((Armor) item).hasCurseGlyph()){
				negativeMagic = true;
			}
		}

		positiveMagic = item.trueLevel() > 0;
		if (!positiveMagic){
			if (item instanceof Weapon && ((Weapon) item).hasGoodEnchant()){
				positiveMagic = true;
			} else if (item instanceof Armor && ((Armor) item).hasGoodGlyph()){
				positiveMagic = true;
			}
		}

		if (!positiveMagic && !negativeMagic){
			GLog.i(Messages.get(this, "detected_none"));
		} else if (positiveMagic && negativeMagic) {
			GLog.h(Messages.get(this, "detected_both"));
		} else if (positiveMagic){
			GLog.p(Messages.get(this, "detected_good"));
		} else if (negativeMagic){
			GLog.w(Messages.get(this, "detected_bad"));
		}

		if (!anonymous) {
			curItem.detach(curUser.belongings.backpack);
			Catalog.countUse(getClass());
			//Talent.onRunestoneUsed(curUser, curUser.pos, getClass());
		}

	}

	@Override
	protected void onThrow(int cell) {
		activateALT(cell);
	}

	public void activateALT(final int cell) {
		boolean[] FOV = new boolean[Dungeon.level.length()];
		Point c = Dungeon.level.cellToPoint(cell);
		ShadowCaster.castShadow(c.x, c.y, Dungeon.level.width(), FOV, Dungeon.level.losBlocking, DIST);
		
		int sX = Math.max(0, c.x - DIST);
		int eX = Math.min(Dungeon.level.width()-1, c.x + DIST);
		
		int sY = Math.max(0, c.y - DIST);
		int eY = Math.min(Dungeon.level.height()-1, c.y + DIST);
		
		ArrayList<Trap> disarmCandidates = new ArrayList<>();
		
		for (int y = sY; y <= eY; y++){
			int curr = y*Dungeon.level.width() + sX;
			for ( int x = sX; x <= eX; x++){
				
				if (FOV[curr]){
					
					Trap t = Dungeon.level.traps.get(curr);
					if (t != null && t.active){
						disarmCandidates.add(t);
					}
					
				}
				curr++;
			}
		}

		Collections.shuffle(disarmCandidates);
		Collections.sort(disarmCandidates, new Comparator<Trap>() {
			@Override
			public int compare(Trap o1, Trap o2) {
				float diff = Dungeon.level.trueDistance(cell, o1.pos) - Dungeon.level.trueDistance(cell, o2.pos);
				if (diff < 0){
					return -1;
				} else if (diff == 0){
					return 0;
				} else {
					return 1;
				}
			}
		});
		
		//disarms at most nine traps
		while (disarmCandidates.size() > 9){
			disarmCandidates.remove(9);
		}
		
		for ( Trap t : disarmCandidates){
			t.reveal();
			t.disarm();
			CellEmitter.get(t.pos).burst(Speck.factory(Speck.STEAM), 6);
		}
		
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
	}
}
