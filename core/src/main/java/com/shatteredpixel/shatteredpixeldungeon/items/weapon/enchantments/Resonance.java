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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 共鸣附魔（武器）。
 * <p>
 * 平时不会显露出能量，只有同时装备了带有共鸣刻印的护甲时才生效：
 * 此时武器伤害提升50%。
 */
public class Resonance extends Weapon.Enchantment {

	private static final ItemSprite.Glowing RESONANCE = new ItemSprite.Glowing(0x66CCFF);

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		if (isResonanceActive(attacker, weapon)) {
			damage = Math.round(damage * 1.5f);
		}
		return damage;
	}

	/**
	 * 共鸣是否激活：需要同时装备共鸣附魔武器与共鸣刻印护甲。
	 */
	public static boolean isResonanceActive(Hero hero) {
		if (hero == null || hero.belongings == null) return false;
		if (!(hero.belongings.weapon() instanceof Weapon)) return false;
		return isResonanceActive(hero, (Weapon) hero.belongings.weapon());
	}

	/** 角色当前使用的武器与护甲是否组成共鸣，适用于英雄和装备武器的怪物。 */
	public static boolean isResonanceActive(Char owner, Weapon weapon) {
		if (owner == null || weapon == null) return false;
		Armor arm = owner instanceof Hero
				? ((Hero) owner).belongings.armor()
				: owner.armor();
		if (arm == null) return false;
		boolean weaponHasResonance = weapon.enchantment instanceof Resonance;
		boolean armorHasResonance = arm.glyph instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Resonance;
		return weaponHasResonance && armorHasResonance;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return RESONANCE;
	}
}
