/*
 * Radish Pixel Dungeon
 * 传奇重犯副本入口房间（位于 convict:1，向返回主线第 8 层）
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;

/**
 * 副本 convict:1 的入口房间，包含返回主线第 8 层的上楼梯。
 */
public class ConvictEntranceRoom extends StandardRoom {

	@Override
	public int minWidth() {
		return 5;
	}

	@Override
	public int minHeight() {
		return 5;
	}

	@Override
	public int maxWidth() {
		return 7;
	}

	@Override
	public int maxHeight() {
		return 7;
	}

	@Override
	public boolean isEntrance() {
		return true;
	}

	@Override
	public boolean canMerge(Level l, Room other, Point p, int mergeTerrain) {
		return false;
	}

	@Override
	public void paint(Level level) {
		Painter.fill(level, this, Terrain.WALL);
		Painter.fill(level, this, 1, Terrain.EMPTY);

		for (Room.Door door : connected.values()) {
			door.set(Room.Door.Type.REGULAR);
		}

		Point c = center();
		int cell = level.pointToCell(c);

		level.transitions.add(LevelTransition.branchUp(level, cell,
				"convict:main-8", Branches.MAIN, 8));
		Painter.set(level, cell, Terrain.ENTRANCE);
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}
}
