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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Unstable extends Weapon.Enchantment {

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x999999 );

	private static Class<?extends Weapon.Enchantment>[] randomEnchants = new Class[]{
			Blazing.class,
			Blocking.class,
			Blooming.class,
			Chilling.class,
			Kinetic.class,
			Corrupting.class,
			Elastic.class,
			Grim.class,
			Lucky.class,
			//projecting not included, no on-hit effect
			Shocking.class,
			Vampiric.class
	};

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {

		// 储存的恒动伤害由 Kinetic.ConservedDamage 在构建期自行释放，这里不再重复结算
		return Reflection.newInstance(Random.oneOf(randomEnchants)).proc( weapon, attacker, defender, damage );
	}

	@Override
	public boolean releasesConservedDamage() {
		return true;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}
}
