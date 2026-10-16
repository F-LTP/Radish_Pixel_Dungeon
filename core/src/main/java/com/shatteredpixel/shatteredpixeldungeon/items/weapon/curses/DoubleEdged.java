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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class DoubleEdged extends Weapon.Enchantment {

	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	//反伤触发概率 = 反伤比例，随武器等级指数衰减（0.1 * 0.5^(lvl/3)），+3 时约为 5%
	//该值不会导致二次衰减，因为武器升级时自身伤害也在增加
	// +0 : 10.000%
	// +1 :  7.937%
	// +2 :  6.300%
	// +3 :  5.000%
	// +4 :  3.969%
	// +5 :  3.150%
	// +6 :  2.500%
	// +7 :  1.984%
	// +8 :  1.575%
	// +9 :  1.250%
	// +10:  0.992%
	private static float backlashFactor( int level ) {
		return 0.1f * (float)Math.pow( 0.5, level / 3.0 );
	}

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {

		//仅近战武器生效
		if (!(weapon instanceof MeleeWeapon)) {
			return damage;
		}

		//伤害+50%，加成受奥术戒指附魔强度影响
		float arcana = RingOfArcana.enchantPowerMultiplier(attacker);
		int boosted = Math.round(damage * (1f + 0.5f * arcana));

		//反伤概率与比例相同，随武器等级指数衰减
		float backlash = backlashFactor(Math.max(0, weapon.buffedLvl()));
		if (Random.Float() < backlash) {
			int selfDamage = Math.round(boosted * backlash);
			if (selfDamage > 0) {
				attacker.damage(new DamageInfo(selfDamage, DamageType.PHYSICAL, attacker, weapon, this));
			}
		}

		return boosted;
	}

	@Override
	public boolean curse() {
		return true;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}

}
