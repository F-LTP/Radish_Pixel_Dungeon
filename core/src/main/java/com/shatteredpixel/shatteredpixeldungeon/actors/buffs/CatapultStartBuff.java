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

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * 弹射起步效果Buff
 * 存在此效果时使用轮椅不消耗充能
 */
public class CatapultStartBuff extends FlavourBuff {

	/* 弹射起步给的是"一段时间内的免费轮椅机会"，不是只给一个回合。
	 * 旧值是 1f：冻伤触发时玩家正 paralysed 数回合，等能动时 buff 早到期，
	 * 于是表现成"看起来没发动"（再点轮椅正常扣充能）。 */
	public static final float DURATION = 10f;

	{
		type = buffType.POSITIVE;
	}

	@Override
	public String icon() {
		return BuffIndicator.HASTE;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.8f, 0f); // 金黄色调
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

}