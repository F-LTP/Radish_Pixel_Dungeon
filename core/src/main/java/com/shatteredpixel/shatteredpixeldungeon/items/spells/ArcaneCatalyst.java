/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ArcaneCatalyst extends Spell {
	
	{
		image = ItemSpriteSheet.SCROLL_CATALYST;
	}
	
	private static HashMap<Class<? extends Scroll>, Float> scrollChances = new HashMap<>();
	static{
		scrollChances.put( ScrollOfIdentify.class,      3f );
		scrollChances.put( ScrollOfRemoveCurse.class,   2f );
		scrollChances.put( ScrollOfMagicMapping.class,  2f );
		scrollChances.put( ScrollOfMirrorImage.class,   2f );
		scrollChances.put( ScrollOfRecharging.class,    2f );
		scrollChances.put( ScrollOfLullaby.class,       2f );
		scrollChances.put( ScrollOfRetribution.class,   2f );
		scrollChances.put( ScrollOfRage.class,          2f );
		scrollChances.put( ScrollOfTeleportation.class, 2f );
		scrollChances.put( ScrollOfTerror.class,        2f );
		scrollChances.put( ScrollOfTransmutation.class, 1f );
	}
	
	@Override
	protected void onCast(Hero hero) {
		
		detach( curUser.belongings.backpack );
		updateQuickslot();

		if (Dungeon.hero.pointsInTalent(Talent.MAGIC_REFINING) >= 3){
			GameScene.show(new ScrollSelectionWindow());
		} else {
			Scroll s = Reflection.newInstance(Random.chances(scrollChances));
			s.anonymize();
			curItem = s;
			s.doRead();
		}


	}
	
	@Override
	public int value() {
		return 40 * quantity;
	}

	@Override
	public int energyVal() {
		return 8 * quantity;
	}


	public static class ScrollSelectionWindow extends WndOptions {
		private final Class<? extends Scroll>[] scrollOptions;

		public ScrollSelectionWindow() {
			super(new ItemSprite(ItemSpriteSheet.SCROLL_CATALYST),
					Messages.get(ArcaneCatalyst.class,"title"),
					Messages.get(ArcaneCatalyst.class,"descx"),
					Messages.get(ArcaneCatalyst.class,"scroll1"),
					Messages.get(ArcaneCatalyst.class,"scroll2"),
					Messages.get(ArcaneCatalyst.class,"scroll3"),
					Messages.get(ArcaneCatalyst.class,"cancel"));

			scrollOptions = new Class[]{
					ScrollOfIdentify.class,
					ScrollOfRemoveCurse.class,
					ScrollOfMagicMapping.class,
					ScrollOfMirrorImage.class,
					ScrollOfRecharging.class,
					ScrollOfLullaby.class,
					ScrollOfRetribution.class,
					ScrollOfRage.class,
					ScrollOfTeleportation.class,
					ScrollOfTerror.class,
					ScrollOfTransmutation.class,
			};
			Random.shuffle(Arrays.asList(scrollOptions));
		}

		@Override
		protected void onSelect(int index) {
			if (index < 3) {
				useRandomScroll(scrollOptions[index]);
			}
			hide();
		}

		private void useRandomScroll(Class<? extends Scroll> scrollClass) {
			Scroll scroll = Reflection.newInstance(scrollClass);
			scroll.anonymize();
			scroll.doRead();
		}

		@Override
		protected boolean hasInfo(int index) {
			return index < 3;
		}

		@Override
		protected void onInfo(int index) {
			try {
				Class<? extends Scroll> scrollClass = scrollOptions[index];
				// 创建卷轴类的实例
				Scroll scrollInstance = scrollClass.getDeclaredConstructor().newInstance();
				// 获取 name 方法
				String title = (String) scrollClass.getMethod("name").invoke(scrollInstance);
				// 获取 desc 方法
				String desc = (String) scrollClass.getMethod("desc").invoke(scrollInstance);
				GameScene.show(new WndTitledMessage(
						Icons.get(Icons.INFO),
						Messages.titleCase(title),
						desc));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}



		@Override
		public void onBackPressed() {
			// Handle back press if needed
		}
	}


	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {
		
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean scroll = false;
			boolean secondary = false;
			
			for (Item i : ingredients){
				if (i instanceof Plant.Seed || i instanceof Runestone){
					secondary = true;
					//if it is a regular or exotic potion
				} else if (ExoticScroll.regToExo.containsKey(i.getClass())
						|| ExoticScroll.regToExo.containsValue(i.getClass())) {
					scroll = true;
				}
			}
			
			return scroll && secondary;
		}
		
		@Override
		public int cost(ArrayList<Item> ingredients) {
			for (Item i : ingredients){
				if (i instanceof Plant.Seed){
					return 1;
				} else if (i instanceof Runestone){
					return 0;
				}
			}
			return 0;
		}
		
		@Override
		public Item brew(ArrayList<Item> ingredients) {
			
			for (Item i : ingredients){
				i.quantity(i.quantity()-1);
			}
			
			return sampleOutput(null);
		}
		
		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new ArcaneCatalyst();
		}
	}

}
