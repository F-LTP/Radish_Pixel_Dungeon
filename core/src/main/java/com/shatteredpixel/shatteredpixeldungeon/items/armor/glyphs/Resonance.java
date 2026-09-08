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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 共鸣刻印（护甲）。
 * <p>
 * 平时不会显露出能量，只有同时装备了带有共鸣附魔的武器时才生效：
 * 此时护甲所能格挡的伤害提升50%。
 */
public class Resonance extends Armor.Glyph {

	private static final ItemSprite.Glowing RESONANCE = new ItemSprite.Glowing(0x66CCFF);

	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		// 格挡提升在角色的 drRoll 中通过 isResonanceActive 判定生效
		return damage;
	}

	/**
	 * 共鸣是否激活：需要同时装备共鸣附魔武器与共鸣刻印护甲。
	 */
	public static boolean isResonanceActive(Char wearer) {
		if (wearer == null) return false;
		Item weapon = wearer.attackingWeapon();
		Item armor = Char.defendingArmor(wearer);
		Armor arm = armor instanceof Armor ? (Armor) armor : null;
		if (weapon == null || arm == null) return false;
		boolean weaponHasResonance = weapon instanceof Weapon
				&& ((Weapon) weapon).enchantment instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Resonance;
		boolean armorHasResonance = arm.glyph instanceof Resonance;
		return weaponHasResonance && armorHasResonance;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return RESONANCE;
	}
}
