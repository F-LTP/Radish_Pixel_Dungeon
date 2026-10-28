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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

/**
 * AI 行为钩子：挂在 Buff 或武器上，在 Mob 的既定决策点（如游荡）被询问。
 * 每个实现都应持有自身的状态（每实例/每怪独立）。
 */
public interface AIModifier {

	/**
	 * 游荡状态下的行为钩子。
	 * @return true 表示本回合已接管（Mob 不再执行默认游荡），false 表示透传回默认逻辑。
	 */
	boolean onWander( Mob mob );

}