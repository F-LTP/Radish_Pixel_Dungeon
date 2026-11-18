/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
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

package com.shatteredpixel.shatteredpixeldungeon.effects;

/**
 * 伤害跳字配色：按浮动文字图标给出同色数字。
 * 每个颜色取对应图标中出现频率最高的非黑色像素，由 tools/damage_icon_colors.py
 * 从 effects/text_icons.png 提取后硬编码于此，运行时不再读取贴图。
 */
public final class DamagePalette {

	private DamagePalette() {}

	// 战斗伤害
	public static final int PHYS_DMG          = 0xcc0000;
	public static final int PHYS_DMG_NO_BLOCK = 0xffffff;
	public static final int MAGIC_DMG         = 0x3499ff;
	public static final int PICK_DMG          = 0xcc0000;
	public static final int HOLY              = 0xffd700;

	// 状态 / 持续伤害
	public static final int HUNGER     = 0xf2e4da;
	public static final int BURNING    = 0xffaa33;
	public static final int SHOCKING   = 0xffff00;
	public static final int FROST      = 0x5674b9;
	public static final int WATER      = 0xb2e9ff;
	public static final int BLEEDING   = 0xcc0000;
	public static final int TOXIC      = 0x50ff60;
	public static final int CORROSION  = 0xff8800;
	public static final int POISON     = 0x993399;
	public static final int OOZE       = 0x008056;
	public static final int DEFERRED   = 0xff2a00;
	public static final int CORRUPTION = 0xffffff;
	public static final int AMULET     = 0xedcb5e;

	// 暴击
	public static final int CRIT          = 0xcc0000;
	public static final int CRIT_NO_BLOCK = 0xffffff;

	/** 无对应图标（如混合伤害取不到成分）时的兜底颜色。 */
	public static final int DEFAULT = PHYS_DMG;

	/** @param icon FloatingText 图标索引；未知图标返回 {@link #DEFAULT} */
	public static int colorFor(int icon) {
		if (icon == FloatingText.PHYS_DMG)          return PHYS_DMG;
		if (icon == FloatingText.PHYS_DMG_NO_BLOCK) return PHYS_DMG_NO_BLOCK;
		if (icon == FloatingText.MAGIC_DMG)         return MAGIC_DMG;
		if (icon == FloatingText.PICK_DMG)          return PICK_DMG;
		if (icon == FloatingText.HOLY)              return HOLY;
		if (icon == FloatingText.HUNGER)            return HUNGER;
		if (icon == FloatingText.BURNING)           return BURNING;
		if (icon == FloatingText.SHOCKING)          return SHOCKING;
		if (icon == FloatingText.FROST)             return FROST;
		if (icon == FloatingText.WATER)             return WATER;
		if (icon == FloatingText.BLEEDING)          return BLEEDING;
		if (icon == FloatingText.TOXIC)             return TOXIC;
		if (icon == FloatingText.CORROSION)         return CORROSION;
		if (icon == FloatingText.POISON)            return POISON;
		if (icon == FloatingText.OOZE)              return OOZE;
		if (icon == FloatingText.DEFERRED)          return DEFERRED;
		if (icon == FloatingText.CORRUPTION)        return CORRUPTION;
		if (icon == FloatingText.AMULET)            return AMULET;
		if (icon == FloatingText.CRIT)              return CRIT;
		if (icon == FloatingText.CRIT_NO_BLOCK)     return CRIT_NO_BLOCK;
		return DEFAULT;
	}
}
