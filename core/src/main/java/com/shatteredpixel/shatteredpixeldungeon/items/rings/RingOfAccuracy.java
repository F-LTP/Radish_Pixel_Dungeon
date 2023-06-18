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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfAccuracy extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ACCURACY;
	}
	@Override
	public boolean doEquip(Hero hero) {
		if (super.doEquip(hero)){
			hero.updateCritSkill(  );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			hero.updateCritSkill(  );
			return true;
		} else {
			return false;
		}
	}
	@Override
	public Item upgrade() {
		super.upgrade();
		if (buff != null && buff.target instanceof Hero){
			((Hero) buff.target).updateCritSkill();
		}
		return this;
	}
	@Override
	public void level(int value) {
		super.level(value);
		Dungeon.hero.updateCritSkill();
	}
	public String statsInfo() {
		if (isIdentified()){
			return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.2f, soloBuffedBonus()) - 1f)),new DecimalFormat("#.##").format(5f*soloBuffedBonus()),new DecimalFormat("#.##").format(0.1f*soloBuffedBonus()));
		} else {
			return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(20f),new DecimalFormat("#.##").format(5f),new DecimalFormat("#.##").format(0.1f));
		}
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Accuracy();
	}
	
	public static float accuracyMultiplier( Char target ){
		return (float)Math.pow(1.2f, getBuffedBonus(target, Accuracy.class));
	}
	public static float critBonus( Char target ){
		return 5f*getBuffedBonus(target, Accuracy.class);
	}
	public static float critDamgeBonus( Char target ){
		return 0.1f*getBuffedBonus(target, Accuracy.class);
	}
	public class Accuracy extends RingBuff {
	}
}
