/*
 * Radish Pixel Dungeon
 * 副本（分支）上下楼梯的黄边贴图覆盖
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;

/**
 * 在分支楼梯（BRANCH_ENTRANCE / BRANCH_EXIT）格子上覆盖带黄边的楼梯贴图。
 * 素材：environment/dungeon_stairs.png（32x16，左格=下行、右格=上行）。
 */
public class BranchStairsVisual extends CustomTilemap {

	private final boolean up;

	public BranchStairsVisual( boolean up ) {
		this.up = up;
		texture = Assets.Environment.DUNGEON_STAIRS;
		tileW = tileH = 1;
	}

	@Override
	public Tilemap create() {
		Tilemap v = super.create();
		// txX 为贴图内的格子索引：0=左格，1=右格（修复上下颠倒）
		v.map( mapSimpleImage( up ? 0 : 1, 0, 32 ), 1 );
		return v;
	}
}
