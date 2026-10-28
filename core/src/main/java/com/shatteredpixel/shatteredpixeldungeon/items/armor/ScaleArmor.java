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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ScaleArmor extends Armor {

	{
		image = ItemSpriteSheet.ARMOR_SCALE;
	}

	public ScaleArmor() {
		super( 4 );
	}

	@Override
	public float evasionFactor(Char owner, float evasion) {
		// 鳞甲特效：在水中额外拥有 2+等级点闪避
		if (Dungeon.level != null && Dungeon.level.water[owner.pos]) {
			evasion += 2 + buffedLvl();
		}
		// 将计算后的闪避值传递给超类处理
		return super.evasionFactor(owner, evasion);
	}

}
