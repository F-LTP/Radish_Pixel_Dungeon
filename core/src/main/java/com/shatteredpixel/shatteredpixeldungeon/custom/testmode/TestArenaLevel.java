package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

/**
 * 测试场：一张干净的大空房间，四面是墙，没有怪物、没有掉落、没有楼梯。
 * 只由测试模式的「楼层跳转器 → 测试场」生成，用于安全地试武器/技能。
 *
 * 生成方式见 FloorTester，Dungeon.newLevel() 会读取 requested 标记并返回本类。
 */
public class TestArenaLevel extends DeadEndLevel {

	/** 下一次生成楼层时是否要出测试场（由 FloorTester 置位，newLevel 消费后清除）。 */
	public static boolean requested = false;

	private static final int SIZE = 21;

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

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

		setSize( SIZE, SIZE );

		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				boolean border = (x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1);
				map[y * width() + x] = border ? Terrain.WALL : Terrain.EMPTY;
			}
		}

		int entrance = (SIZE / 2) * width() + SIZE / 2;
		map[entrance] = Terrain.ENTRANCE;
		// 支线的第 1 层上面没有楼层，照常按「通往上一层」来建会因为目标是第 0 层直接抛异常。
		// 这时入口只当落脚点用，和主线第 1 层一样记成「通往地表」，再由 activateTransition 拦下
		LevelTransition t = Dungeon.depth <= 1
				? LevelTransition.surface( this, entrance )
				: LevelTransition.regularEntrance( this, entrance );
		t.type = LevelTransition.Type.REGULAR_ENTRANCE;
		transitions.add( t );

		return true;
	}

	@Override
	public boolean activateTransition( Hero hero, LevelTransition transition ) {
		// 第 1 层的测试场没有上一层可去。不拦的话会按「上楼」去读第 0 层，直接闪退
		if (transition.direction == LevelTransition.Direction.SURFACE) {
			Game.runOnRenderThread( new Callback() {
				@Override
				public void call() {
					GameScene.show( new WndMessage( M.L( TestArenaLevel.class, "no_way_up" ) ) );
				}
			} );
			return false;
		}
		return super.activateTransition( hero, transition );
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		return entrance();
	}
}
