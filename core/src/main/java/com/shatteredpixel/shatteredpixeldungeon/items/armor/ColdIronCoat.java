/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** 寒铁大衣：三阶护甲，免疫冻伤与冰冻。 */
public class ColdIronCoat extends Armor {

	{ image = ItemSpriteSheet.ARMOR_COLDIRON; }

	public ColdIronCoat() { super( 3 ); }

	@Override public int DRMax(int lvl){ return 6 + 3 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 1 + lvl; }
	@Override protected ArmorBuff buff(){ return new ColdIronBuff(); }

	/** 免疫走 Char.isImmune 收集 buff.immunities() 的既有链路。 */
	public class ColdIronBuff extends ArmorBuff {
		{ immunities.add( Frost.class ); immunities.add( Chill.class ); }
	}
}
