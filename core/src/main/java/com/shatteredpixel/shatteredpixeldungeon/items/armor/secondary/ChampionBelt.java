/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class ChampionBelt extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 10 + 5 * level() };
	}


	public ChampionBelt() {
		super(2, 1, 2, 12);
		image = "champion_belt";
	}
}
