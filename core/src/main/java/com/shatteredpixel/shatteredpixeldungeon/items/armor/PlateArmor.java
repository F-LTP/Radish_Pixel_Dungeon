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

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class PlateArmor extends Armor {

	{
		image = ItemSpriteSheet.ARMOR_PLATE;
	}

	public PlateArmor() {
		super( 5 );
	}

	/**
	 * 板甲特效：免疫减免前低于 4+0.5*等级的伤害（向下取整）
	 * @param damage 原始伤害
	 * @return 如果伤害低于阈值返回 0（完全免疫），否则返回原始伤害
	 */
	public float damageReduce(Char wearer, float damage) {
		if (wearer != null && Char.defendingArmor(wearer) == this) {
			int threshold = 4 + (int)(buffedLvl() * 0.5f);
			if (damage < threshold) {
				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY);
				return 0;
			}
		}
		return damage;
	}

}
