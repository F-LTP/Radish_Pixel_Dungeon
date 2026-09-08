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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.PathFinder;

/**
 * 门后伏击 AI：游荡时寻找附近关着的门，钻过去并等在门后最多若干回合，
 * 等着偷袭路过的玩家。整个伏击只触发一次，结束后转回正常 AI。
 * 状态保存在本实例上（每武器实例/每怪独立），不参与序列化。
 */
public class AmbushAI implements AIModifier {

	private static final int AMBUSH_WAIT_TURNS = 5;   // 门后最多等待回合数
	private static final int AMBUSH_SEARCH_RADIUS = 3;// 寻找门的范围

	private boolean triggered = false;  // 已触发过一次伏击，之后转正常 AI
	private int phase = 0;              // 0=无 1=钻门中 2=门后等待
	private int door = -1;              // 要伏击的门格子
	private int dest = -1;              // 门另一侧的落点
	private int count = 0;              // 门后剩余等待回合

	@Override
	public boolean onWander( Mob mob ) {
		switch (phase) {
			case 0:
				// 已触发过一次伏击就不再寻找新门，转化回正常 AI
				if (triggered) return false;
				return seekAmbushDoor( mob );
			case 1: return crossDoor( mob );
			case 2: return waitBehindDoor( mob );
		}
		return false;
	}

	// 寻找附近关着的门，准备钻过去伏击
	private boolean seekAmbushDoor( Mob mob ) {
		int bestDoor = -1, bestDest = -1, bestDist = Integer.MAX_VALUE;
		int pos = mob.pos;
		int w = Dungeon.level.width(), h = Dungeon.level.height();
		int x = pos % w, y = pos / w;
		for (int ny = Math.max(0, y - AMBUSH_SEARCH_RADIUS);
		     ny <= Math.min(h - 1, y + AMBUSH_SEARCH_RADIUS); ny++) {
			for (int nx = Math.max(0, x - AMBUSH_SEARCH_RADIUS);
			     nx <= Math.min(w - 1, x + AMBUSH_SEARCH_RADIUS); nx++) {
				int d = nx + ny * w;
				if (d == pos || Dungeon.level.map[d] != Terrain.DOOR) continue;
				// 找门另一侧的可走落点（离怪最远的邻居）
				int dest = -1, destDist = -1;
				for (int n : PathFinder.NEIGHBOURS4) {
					int b = d + n;
					if (b == pos) continue;
					if (!Dungeon.level.insideMap(b)) continue;
					if (Dungeon.level.map[b] == Terrain.DOOR
							|| Dungeon.level.map[b] == Terrain.OPEN_DOOR
							|| Dungeon.level.map[b] == Terrain.LOCKED_DOOR
							|| Dungeon.level.map[b] == Terrain.CRYSTAL_DOOR) continue;
					if (!Dungeon.level.passable[b]) continue;
					if (Actor.findChar(b) != null) continue;
					int dist = Dungeon.level.distance(pos, b);
					if (dist > destDist) {
						destDist = dist;
						dest = b;
					}
				}
				if (dest == -1) continue;
				int dist = Dungeon.level.distance(pos, d);
				if (dist < bestDist) {
					bestDist = dist;
					bestDoor = d;
					bestDest = dest;
				}
			}
		}
		if (bestDoor == -1) {
			phase = 0;
			return false; // 附近没门，正常游荡
		}
		triggered = true; // 触发伏击：跑完这次后不再钻门
		phase = 1;
		door = bestDoor;
		dest = bestDest;
		return crossDoor( mob );
	}

	// 钻门：先踩上门（自动开门），再穿到门另一侧（离开时自动关门）
	private boolean crossDoor( Mob mob ) {
		if (mob.pos == dest) {
			// 已到门后，开始等待
			phase = 2;
			count = AMBUSH_WAIT_TURNS;
			return waitBehindDoor( mob );
		}
		int step = (mob.pos == door) ? dest : door;
		if (!mob.aiMoveTo( step )) {
			// 走不动，放弃伏击（已触发过，转正常 AI）
			phase = 0;
			return false;
		}
		return true;
	}

	// 门后等待：最多等 AMBUSH_WAIT_TURNS 回合，等不到就恢复游荡
	private boolean waitBehindDoor( Mob mob ) {
		if (mob.pos != dest
				|| Dungeon.level.map[door] != Terrain.DOOR
				|| !Dungeon.level.adjacent( mob.pos, door )) {
			// 门被打开/移除了（如玩家压住门使其保持开启），放弃伏击并转正常 AI
			phase = 0;
			return false;
		}
		if (count <= 0) {
			phase = 0;
			return false; // 等够了，恢复正常游荡（不再二次伏击）
		}
		count--;
		mob.aiWait( 1f );
		return true;
	}

}