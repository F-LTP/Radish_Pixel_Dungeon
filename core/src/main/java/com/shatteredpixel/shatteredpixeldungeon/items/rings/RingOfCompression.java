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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 挤压之戒 - 增强击退效果
 * 当佩戴时，所有敌人受到推力的效果增加，碰撞伤害增加
 */
public class RingOfCompression extends Ring {
	{
		icon = ItemSpriteSheet.Icons.RING_COMPRESSION; // 临时占位，待替换
	}

	@Override
	protected RingBuff buff() {
		return new Compression();
	}

	/**
	 * 获取击退距离加成
	 * @param ch 目标角色（攻击者）
	 * @return 额外的击退距离
	 */
	public static int knockbackDistanceBonus(Char ch) {
		return getBuffedBonus(ch, Compression.class);
	}

	/**
	 * 获取碰撞伤害倍率
	 * 公式: 1.25^(效果等级)
	 * @param ch 目标角色（攻击者）
	 * @return 伤害倍率
	 */
	public static float collisionDamageMultiplier(Char ch) {
		int level = getBuffedBonus(ch, Compression.class);
		if (level <= 0) return 1f;
		return (float) Math.pow(1.25, level);
	}

	@Override
	public String statsInfo() {
		if (isIdentified()) {
			int level = soloBuffedBonus();
			String info = Messages.get(this, "stats", level, level);
			if (isEquippedDungeonHero() && soloBuffedBonus() != combinedBuffedBonusDungeonHero()) {
				level = combinedBuffedBonusDungeonHero();
				info += "\n\n" + Messages.get(this, "combined_stats", level, level);
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats");
		}
	}

	// 辅助方法，避免空指针
	private boolean isEquippedDungeonHero() {
		return isEquipped((Hero) getHeroIfAvailable());
	}

	private int combinedBuffedBonusDungeonHero() {
		return combinedBuffedBonus((Hero) getHeroIfAvailable());
	}

	private Char getHeroIfAvailable() {
		try {
			return com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String upgradeStat1(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level - 3);
		return "+" + (level + 1);
	}

	@Override
	public String upgradeStat2(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level - 3);
		return "x" + String.format("%.2f", Math.pow(1.25, level + 1));
	}

	public class Compression extends RingBuff {
	}
}