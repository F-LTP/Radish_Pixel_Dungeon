/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2026 TheCatist
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.damage;

/**
 * 伤害来源工具类
 * 
 * 提供从来源对象自动判断伤害类型的辅助方法。
 * 主要用于兼容现有代码的渐进式迁移。
 */
public class DamageSource {
	
	// ========== 类型判断方法 ==========
	
	/**
	 * 根据来源对象获取伤害类型
	 * 通过类名匹配来判断
	 */
	public static DamageType getType(Object source) {
		if (source == null) {
			return DamageType.PHYSICAL;
		}
		return DamageType.fromSource(source);
	}
	
	/**
	 * 判断是否为魔法伤害来源
	 */
	public static boolean isMagical(Object source) {
		if (source == null) return false;
		DamageType type = getType(source);
		return type.isMagical();
	}
	
	/**
	 * 判断是否为无视护甲的伤害来源
	 */
	public static boolean ignoresArmor(Object source) {
		if (source == null) return false;
		DamageType type = getType(source);
		return type.ignoresArmor();
	}
	
	/**
	 * 判断是否为真实伤害来源
	 */
	public static boolean isTrueDamage(Object source) {
		if (source == null) return false;
		return getType(source) == DamageType.TRUE;
	}
	
	/**
	 * 创建DamageInfo（从来源自动判断类型）
	 */
	public static DamageInfo create(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, getType(source), null, null, source);
	}
	
	/**
	 * 创建DamageInfo（带攻击者）
	 */
	public static DamageInfo create(int baseDamage, Object source, Object attacker) {
		DamageInfo info = create(baseDamage, source);
		if (attacker != null && attacker instanceof com.shatteredpixel.shatteredpixeldungeon.actors.Char) {
			info.setAttacker((com.shatteredpixel.shatteredpixeldungeon.actors.Char) attacker);
		}
		return info;
	}
}