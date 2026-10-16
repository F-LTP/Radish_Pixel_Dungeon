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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Grudge;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;

/**
 * 灵魂余烬 (Soul Ember)
 * 怨灵/缠怨灵 35%/70% 概率掉落 1～3 个；诅咒玫瑰和尸尘生成的怨灵不掉落。
 * 可分解为2点炼金能量。
 */
public class SoulEmber extends Item {
	{
		image = ItemSpriteSheet.SOUL_EMBER;

		stackable = true;
		defaultAction = AC_THROW;
		usesTargeting = true;

		bones = true;
	}

	@Override
	protected void onThrow(int cell) {
		if (Dungeon.level.pit[cell]) {
			super.onThrow(cell);
		} else {
			Splash.at(cell, 0x777777FF, 3);
			if (canBurn(cell)) {
				Level.set(cell, Terrain.EMBERS);
				GameScene.updateMap(cell);
			}
			for (int i : PathFinder.NEIGHBOURS9) {
				int p = cell + i;
				if (p >= 0 && p < Dungeon.level.length()) {
					Char ch = Actor.findChar(p);
					if (ch != null && ch != curUser && ch.isAlive())
						Buff.affect(ch, Grudge.Haunted.class, Grudge.Haunted.DURATION);
				}
			}
		}
	}

	private boolean canBurn(int cell) {
		Point p = Dungeon.level.cellToPoint(cell);
		for (CustomTilemap cust : Dungeon.level.customTiles) {
			Point custPoint = new Point(p);
			custPoint.x -= cust.tileX;
			custPoint.y -= cust.tileY;
			if (custPoint.x >= 0 && custPoint.y >= 0
					&& custPoint.x < cust.tileW && custPoint.y < cust.tileH) {
				if (cust.image(custPoint.x, custPoint.y) != null) {
					return false;
				}
			}
		}
		int t = Dungeon.level.map[cell];
		return t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.WATER;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 5 * quantity;
	}

	@Override
	public int energyVal() {
		return 2 * quantity;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
        return super.desc();
    }
}
