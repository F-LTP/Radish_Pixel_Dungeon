/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class BlackBelt extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 2 + level(), 2 + level() };
	}


	public BlackBelt() {
		super(1, 0, 1, 10);
		image = "black_belt";
	}
}
