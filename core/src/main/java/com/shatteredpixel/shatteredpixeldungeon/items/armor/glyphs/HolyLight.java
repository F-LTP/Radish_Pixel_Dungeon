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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * 圣光刻印（护甲）。
 * <p>
 * 受到攻击时，有 10% + 1%*等级 的概率放出爆闪，致盲贴身敌人 3 回合，
 * 亡灵与恶魔还会被击退 2 格。概率最终乘以奥术倍率，
 * 超过 100% 的部分会转化为失明时间与击退距离。
 */
public class HolyLight extends Armor.Glyph {

	private static ItemSprite.Glowing GOLD = new ItemSprite.Glowing( 0xFFD700 );

	private static final float BASE_CHANCE = 0.10f;
	private static final float CHANCE_PER_LEVEL = 0.01f;
	private static final float BASE_BLIND = 3f;
	private static final int BASE_KNOCKBACK = 2;

	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max(0, armor.procLvl());

		//奥术戒指最终乘算触发概率
		float procChance = (BASE_CHANCE + CHANCE_PER_LEVEL * level) * procChanceMultiplier(defender);
		if (Random.Float() >= procChance) {
			return damage;
		}

		//超过 100% 的部分开始增强失明时间与击退距离
		float powerMulti = Math.max(1f, procChance);
		float blindDuration = BASE_BLIND * powerMulti;
		int knockback = Math.round(BASE_KNOCKBACK * powerMulti);

		CellEmitter.get(defender.pos).burst( Speck.factory( Speck.LIGHT ), 8 );

		for (Char ch : Actor.chars()) {
			if (ch == defender || ch.alignment == defender.alignment) continue;
			if (!Dungeon.level.adjacent(defender.pos, ch.pos)) continue;

			Buff.affect(ch, Blindness.class, blindDuration);

			if (Char.hasProp(ch, Char.Property.UNDEAD) || Char.hasProp(ch, Char.Property.DEMONIC)) {
				int opposite = ch.pos + (ch.pos - defender.pos);
				Ballistica trajectory = new Ballistica(ch.pos, opposite, Ballistica.MAGIC_BOLT);
				WandOfBlastWave.throwChar(ch, trajectory, knockback, true, true, this);
			}
		}

		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GOLD;
	}
}
