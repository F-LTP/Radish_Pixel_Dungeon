/*
 * Radish Pixel Dungeon
 * 副本（分支）上下楼梯的黄边贴图覆盖
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;

/**
 * 在分支楼梯（BRANCH_ENTRANCE / BRANCH_EXIT）格子上覆盖带黄边的楼梯贴图。
 * 素材：environment/dungeon_stairs.png（32x16，左格=下行、右格=上行）。
 */
public class BranchStairsVisual extends CustomTilemap {

	private boolean up;

	//读档时 Bundle 靠反射调用无参构造函数来创建实例：没有它的话这块贴图读不回来，
	//每读一次这一层还会上报一次「无法实例化」的异常。朝向随后在 restoreFromBundle 里读回
	public BranchStairsVisual() {
		this( false );
	}

	public BranchStairsVisual( boolean up ) {
		this.up = up;
		texture = Assets.Environment.DUNGEON_STAIRS;
		tileW = tileH = 1;
	}

	private static final String UP = "up";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( UP, up );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		//旧存档里没有这个键（那时这块贴图根本读不回来），朝向由关卡读档后按过渡点重建时给出
		if (bundle.contains( UP )) up = bundle.getBoolean( UP );
	}

	@Override
	public Tilemap create() {
		Tilemap v = super.create();
		// txX 为贴图内的格子索引：0=左格，1=右格（修复上下颠倒）
		v.map( mapSimpleImage( up ? 0 : 1, 0, 32 ), 1 );
		return v;
	}
}
