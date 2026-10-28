/*
 * Radish Pixel Dungeon
 * 传奇重犯副本入口房间（位于主线第 8 层）
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;

/**
 * 主线第 8 层的副本入口：向下通往 convict:1。
 */
public class ConvictEnterRoom extends SpecialRoom {

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
	public boolean canMerge(Level level, Room other, Point point, int mergeTerrain) {
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

		level.transitions.add(LevelTransition.branchDown(level, cell,
				"convict:main-8", Branches.CONVICT, 1));
		Painter.set(level, cell, Terrain.EXIT);
	}

	@Override
	public boolean connect(Room room) {
		if (room.isExit()) return false;
		return super.connect(room);
	}
}
