/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.damage;

/**
 * 移植武器的伤害类型统一映射。
 *
 * 移植说明包里的「精神 / 侵蚀 / 灵魂」是原项目自研伤害框架的枚举，本项目并不存在。
 * 本项目只认 {@link DamageType} 里的已有类型，因此统一在这里做一次映射，
 * 三把武器与蝴蝶场地一律引用本类，不再散落硬编码，也不新增任何伤害类型。
 */
public final class WeaponDamageTypes {

	/** 原「精神」（白）→ 本项目神圣：白色跳字、按魔法口径结算。 */
	public static final DamageType PSYCHIC = DamageType.HOLY;

	/** 原「侵蚀」（黑）→ 本项目腐蚀：暗色跳字、元素抗性口径。 */
	public static final DamageType CORROSION = DamageType.CORROSIVE;

	/** 原「灵魂」→ 本项目魔法：走 AntiMagic 抗性口径。 */
	public static final DamageType SOUL = DamageType.MAGICAL;

	private WeaponDamageTypes() {}
}
