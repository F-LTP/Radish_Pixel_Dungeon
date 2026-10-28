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

package com.shatteredpixel.shatteredpixeldungeon.items.curses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;

/**
 * 沉重 / 超轻诅咒共用的数值计算。
 *
 * 沉重：临时等级 = round(1 × 奥术倍率)，力量需求修正固定 +2。
 * 超轻：临时等级固定 -1，力量需求修正 = -round(2 × 奥术倍率)。
 */
public class CurseTempLevels {

	private static float arcana() {
		return Dungeon.hero != null ? RingOfArcana.enchantPowerMultiplier(Dungeon.hero) : 1f;
	}

	public static int heavyTempLevel() {
		return Math.round(arcana());
	}

	public static int heavyStrReqMod() {
		return 2;
	}

	public static int ultralightTempLevel() {
		return -1;
	}

	public static int ultralightStrReqMod() {
		return -Math.round(2f * arcana());
	}

}
