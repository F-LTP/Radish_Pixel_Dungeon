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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ai;

/**
 * 武器声明的"怪物 AI 标签"：怪物持有带该标签的武器时，会获得对应的 AI 行为。
 * 每种标签对应一个 {@link AIModifier} 行为类，create() 为每个武器实例新建独立行为实例。
 */
public enum WeaponAITag {

	NONE( null ),
	AMBUSH( AmbushAI.class );

	private final Class<? extends AIModifier> aiClass;

	WeaponAITag( Class<? extends AIModifier> aiClass ) {
		this.aiClass = aiClass;
	}

	/** 创建一个全新的行为实例（每武器/每怪独立，避免状态共享）。 */
	public AIModifier create() {
		if ( aiClass == null ) return null;
		try {
			return aiClass.newInstance();
		} catch ( Exception e ) {
			return null;
		}
	}

}