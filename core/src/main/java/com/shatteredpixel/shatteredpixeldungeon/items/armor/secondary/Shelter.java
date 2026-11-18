/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class Shelter extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 3 + level() };
	}


	public Shelter() {
		super(3, 2, 4, 14);
		image = "shelter";
	}
}
