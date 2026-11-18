package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStairs;

/**
 * 测试支线的唯一关卡：三个小房间，各放一扇循环楼梯 A/B/C，踩上去在三个房间之间轮转。
 */
public class CycleTestLevel extends Level {

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	private static final int CELL_A = 2 * 15 + 3;
	private static final int CELL_B = 2 * 15 + 9;
	private static final int CELL_C = 6 * 15 + 6;
	private static final int BACK_CELL = 4 * 15 + 6;
	/** 出生格（和回程楼梯分开，避免一进来就被弹回去） */
	public static final int ENTRANCE_CELL = 8 * 15 + 6;
	public static final int RETURN_CELL = 9 * 15 + 9;

	/** 本次进入支线的主线层数（静态，供回程使用） */
	public static int pendingReturnDepth = 1;

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CAVES;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
	}

	@Override
	protected boolean build() {
		if (Dungeon.depth == 2) {
			return buildSecondFloor();
		}

		setSize(15, 11);

		for (int i = 0; i < length(); i++) {
			map[i] = Terrain.WALL;
		}

		carve(1, 1, 6, 4);
		carve(8, 1, 13, 4);
		carve(4, 6, 10, 9);
		carve(6, 8, 8, 8);
		carve(5, 4, 5, 6);
		carve(9, 4, 9, 6);

		map[ENTRANCE_CELL] = Terrain.EMPTY;
		map[RETURN_CELL] = Terrain.ENTRANCE;

		//占位 transition：位于角落墙里，永远不会被踩到，只为避免空 transitions 引发空指针
		transitions.add(LevelTransition.branchUp(this, 0, "cycletest:dummy", Branches.MAIN, 1));

		//三扇循环楼梯：A→B 同层，B→C 同层，C→第 2 层（跨层）
		SpecialStairs.addCycle(this, CELL_A, "cycletest", 0).destCell(CELL_B);
		SpecialStairs.addCycle(this, CELL_B, "cycletest", 1).destCell(CELL_C);
		SpecialStairs.addCycle(this, CELL_C, "cycletest", 2)
				.dest(Branches.TEST, 2, ENTRANCE_CELL);

		return true;
	}

	private void carve(int left, int top, int right, int bottom) {
		for (int y = top; y <= bottom; y++) {
			for (int x = left; x <= right; x++) {
				map[y * width() + x] = Terrain.EMPTY;
			}
		}
	}

	private boolean buildSecondFloor() {
		setSize(15, 11);

		for (int i = 0; i < length(); i++) {
			map[i] = Terrain.WALL;
		}
		carve(1, 1, 13, 9);

		map[ENTRANCE_CELL] = Terrain.EMPTY;
		map[RETURN_CELL] = Terrain.ENTRANCE;
		map[BACK_CELL] = Terrain.EXIT;

		SpecialStairs.addAt(this, BACK_CELL).dest(Branches.TEST, 1, ENTRANCE_CELL);
		SpecialStairs.addAt(this, RETURN_CELL).dest(Branches.MAIN, pendingReturnDepth, -1);

		transitions.add(LevelTransition.branchUp(this, 0, "cycletest:dummy", Branches.MAIN, 1));
		return true;
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
	}

	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return ENTRANCE_CELL;
	}
}
