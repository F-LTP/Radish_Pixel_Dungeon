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
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 求生附魔（武器）。
 * <p>
 * 生命值每损失 (1 - 0.03*等级)% 便提高 0.5% 的命中，受奥术加成。
 * 具体效果见 {@link Weapon#accuracyFactor(Char, Char)}。
 */
public class Survival extends Weapon.Enchantment {

	private static ItemSprite.Glowing GREEN = new ItemSprite.Glowing( 0x33CC66 );

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		//no proc effect, see Weapon.accuracyFactor
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GREEN;
	}

	/** 命中倍率，1 表示无加成。 */
	public static float accuracyMultiplier(Char owner, int level) {
		return 1f + bonus(owner, level);
	}

	/** 基于已损失生命值的加成（小数，0.5 = +50%）。 */
	public static float bonus(Char owner, int level) {
		if (owner == null || owner.HT <= 0) return 0f;

		float missingPercent = (owner.HT - owner.HP) / (float)owner.HT * 100f;
		float step = Math.max(0.01f, 1f - 0.03f * Math.max(0, level));
		float bonus = missingPercent / step * 0.005f;
		return bonus * RingOfArcana.enchantPowerMultiplier(owner) * owner.talentProc();
	}
}
