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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.TextureFilm;

/**
 * 禁疗 Buff - 阻止任何形式的生命恢复
 * 时长4回合，可叠加
 */
public class HealingBlocked extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
	}

	public static final float DURATION = 4f;

	@Override
	public String icon() {
		return BuffIndicator.HEALING;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}

	/**
	 * 检查目标是否被禁疗
	 */
	public static boolean isBlocked(Char target) {
		return target.buff(HealingBlocked.class) != null;
	}

	/**
	 * 对目标附加禁疗效果
	 */
	public static void block(Char target, float duration) {
		HealingBlocked buff = target.buff(HealingBlocked.class);
		if (buff != null) {
			// 如果已有禁疗buff，延长持续时间（叠加）
			buff.spend(duration);
		} else {
			Buff.affect(target, HealingBlocked.class, duration);
		}
	}
}
