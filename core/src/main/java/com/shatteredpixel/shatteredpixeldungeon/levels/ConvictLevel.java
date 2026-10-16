/*
 * Radish Pixel Dungeon
 * 传奇重犯副本楼层（开阔竞技场，类似天狗二阶段）
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LegendaryFelon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.noosa.audio.Music;

import java.util.Arrays;

/**
 * 传奇重犯副本：单层开阔椭圆形竞技场，从主线第 8 层进入。
 * 入口位于竞技场下方，中心放置传奇重犯（独立血条）。
 */
public class ConvictLevel extends Level {

	private static final int LEFT = 3, TOP = 3, RIGHT = 16, BOTTOM = 16;

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_PRISON;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_PRISON;
	}

	@Override
	protected boolean build() {

		setSize( 20, 20 );
		Arrays.fill( map, Terrain.WALL );

		int cx2 = LEFT + RIGHT;
		int cy2 = TOP + BOTTOM;
		double rx = RIGHT - LEFT;
		double ry = BOTTOM - TOP;

		for (int x = LEFT; x <= RIGHT; x++) {
			for (int y = TOP; y <= BOTTOM; y++) {
				double dx = (2.0 * x - cx2) / rx;
				double dy = (2.0 * y - cy2) / ry;
				if (dx * dx + dy * dy <= 1.0) {
					map[x + y * width()] = Terrain.EMPTY;
				}
			}
		}

		int entrance = ((LEFT + RIGHT) / 2) + (BOTTOM - 2) * width();
		map[entrance] = Terrain.ENTRANCE;
		transitions.add( LevelTransition.branchUp( this, entrance,
				"convict:main-8", Branches.MAIN, 8 ) );

		return true;
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
		LegendaryFelon felon = new LegendaryFelon();
		felon.pos = ((LEFT + RIGHT) / 2) + ((TOP + BOTTOM) / 2) * width();
		mobs.add( felon );
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
		// 无
	}

	@Override
	public void playLevelMusic() {
		Music.INSTANCE.play( Assets.Music.PRISON_TENSE, true );
	}
}
