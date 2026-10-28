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

import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;

/**
 * 伤害类型枚举
 * 
 * 暴击不是伤害类型，而是DamageInfo的属性。
 * 每种类型有对应的浮动文字图标。
 */
public enum DamageType {
	
	// ========== 物理伤害 ==========
	/** 标准物理伤害，受护甲减免 */
	PHYSICAL("physical", FloatingText.PHYS_DMG, false, false),
	
	/** 物理伤害但无视护甲 */
	PHYSICAL_NO_ARMOR("physical_no_armor", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
	
	// ========== 魔法伤害 ==========
	/** 一般魔法伤害 */
	MAGICAL("magical", FloatingText.MAGIC_DMG, true, false),
	
	// ========== 元素伤害 ==========
	/** 火焰伤害 */
	FIRE("fire", FloatingText.BURNING, true, false),
	
	/** 冰霜伤害 */
	FROST("frost", FloatingText.FROST, true, false),
	
	/** 闪电伤害 */
	LIGHTNING("lightning", FloatingText.SHOCKING, true, false),
	
	/** 毒气伤害 */
	TOXIC("toxic", FloatingText.TOXIC, true, false),
	
	/** 腐蚀伤害 */
	CORROSIVE("corrosive", FloatingText.CORROSION, true, false),
	
	// ========== 状态伤害（持续伤害） ==========
	/** 流血 */
	BLEEDING("bleeding", FloatingText.BLEEDING, false, false),
	
	/** 中毒 */
	POISON("poison", FloatingText.POISON, true, false),
	
	/** 粘液 */
	OOZE("ooze", FloatingText.OOZE, true, false),
	
	/** 燃烧状态 */
	BURNING_STATUS("burning_status", FloatingText.BURNING, true, false),
	
	/** 寒冷状态 */
	CHILL("chill", FloatingText.FROST, true, false),
	
	// ========== 特殊伤害 ==========
	/** 饥饿伤害 */
	HUNGER("hunger", FloatingText.HUNGER, true, true),
	
	/** 坠落伤害 */
	FALL("fall", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
	
	/** 深渊伤害 */
	CHASM("chasm", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
	
	/** 延迟伤害（如粘性护甲） */
	DEFERRED("deferred", FloatingText.DEFERRED, false, false),
	
	/** 腐化伤害 */
	CORRUPTION("corruption", FloatingText.CORRUPTION, true, true),
	
	/** 钴镐挖掘伤害 */
	PICK("pick", FloatingText.PICK_DMG, false, false),
	
	/** 水伤害 */
	WATER("water", FloatingText.WATER, true, false),
	
	/** 护符伤害 */
	AMULET("amulet", FloatingText.AMULET, true, true),
	
	// ========== 真实伤害 ==========
	/** 真实伤害：无视一切减免（护甲、抗性、护盾等） */
	TRUE("true", FloatingText.PHYS_DMG, true, true),

	// ========== 混合伤害 ==========
	/** 混合伤害：由多种类型按百分比构成，减免与跳字按各成分分别处理。
	 *  具体成分存于 DamageInfo 的 MixedDamage 中（本枚举仅为哨兵）。 */
	MIXED("mixed", FloatingText.NO_ICON, false, false),
	
	// ========== 其他 ==========
	/** 未知/默认类型 */
	UNKNOWN("unknown", FloatingText.PHYS_DMG, false, false);
	
	private final String id;
	private final int floatingTextIcon;
	private final boolean magical;
	private final boolean ignoresArmor;
	
	DamageType(String id, int floatingTextIcon, boolean magical, boolean ignoresArmor) {
		this.id = id;
		this.floatingTextIcon = floatingTextIcon;
		this.magical = magical;
		this.ignoresArmor = ignoresArmor;
	}
	
	/** 获取类型ID */
	public String getId() {
		return id;
	}
	
	/** 获取浮动文字图标 */
	public int getFloatingTextIcon() {
		return floatingTextIcon;
	}
	
	/** 是否为魔法伤害（受魔法抗性影响） */
	public boolean isMagical() {
		return magical;
	}
	
	/** 是否无视护甲 */
	public boolean ignoresArmor() {
		return ignoresArmor;
	}
	
	/** 是否为真实伤害（无视一切减免） */
	public boolean isTrueDamage() {
		return this == TRUE;
	}
	
	/** Whether this damage skips shield buffers. */
	public boolean ignoresShields() {
		return this == TRUE || this == HUNGER;
	}

	/** 是否为物理伤害 */
	public boolean isPhysical() {
		return !magical && this != TRUE && this != UNKNOWN;
	}
	
	/** 是否为元素伤害 */
	public boolean isElemental() {
		return this == FIRE || this == FROST || this == LIGHTNING 
			|| this == TOXIC || this == CORROSIVE;
	}
	
	/** 是否为持续伤害（DoT） */
	public boolean isDoT() {
		return this == BLEEDING || this == POISON || this == OOZE 
			|| this == BURNING_STATUS || this == CHILL;
	}
}
