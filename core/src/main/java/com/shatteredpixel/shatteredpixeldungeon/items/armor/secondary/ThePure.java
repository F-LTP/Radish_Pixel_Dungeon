/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class ThePure extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ Math.round( 2 + 0.2f * level() ) };
	}


	public ThePure() {
		super(1, 0, 1, 10);
		image = "the_pure";
	}
}
