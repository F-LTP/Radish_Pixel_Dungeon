/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class Greaves extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 10 + 2 * level() };
	}


	public Greaves() {
		super(5, 2, 5, 18);
		image = "greaves";
	}
}
