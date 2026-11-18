package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：存档再读档（H.saveAndReload）。
 * 存档写在本次运行的沙盒目录里。读档之后游戏对象全部是新实例，场景手里的旧引用（怪、物品）不能再用，要重新找。
 */
public class SelfTest_SaveReload implements Scenario {

	@Override
	public String describe() {
		return "工具自测：存档再读档后，英雄位置 / 生命、怪、地形、背包物品都还在";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat rat = new Rat();
		rat.HP = 77;
		rat.HT = 500;
		rat.state = rat.PASSIVE;
		h.spawn( rat, Arena.c( 13, 10 ) );
		h.setTerrain( Arena.c( 10, 5 ), Terrain.CHASM );
		h.give( new WandOfBlastWave() );
		h.walkTo( Arena.c( 8, 10 ) );
		int hpBefore = h.get( () -> Dungeon.hero.HP );

		h.saveAndReload();

		h.check( h.get( () -> Dungeon.level instanceof TestArenaLevel ) && h.depth() == 1, "读档后还在第 1 层的测试场里" );
		h.check( h.heroPos() == Arena.c( 8, 10 ), "读档后英雄还在 (8,10)（实际 " + h.cellStr( h.heroPos() ) + "）" );
		h.check( h.get( () -> Dungeon.hero.HT ) == 300 && h.get( () -> Dungeon.hero.HP ) == hpBefore,
				"读档后英雄生命不变（" + hpBefore + "/300，实际 " + h.get( () -> Dungeon.hero.HP ) + "/" + h.get( () -> Dungeon.hero.HT ) + "）" );
		Char there = h.get( () -> Actor.findChar( Arena.c( 13, 10 ) ) );
		h.check( there instanceof Rat && there != rat, "读档后 (13,10) 上有一只老鼠，而且是新实例" );
		if (there != null) {
			h.check( h.get( () -> there.HP ) == 77 && h.get( () -> there.HT ) == 500, "老鼠的生命是存档时的 77/500（实际 " + h.get( () -> there.HP ) + "/" + h.get( () -> there.HT ) + "）" );
		}
		h.check( h.get( () -> Dungeon.level.map[Arena.c( 10, 5 )] ) == Terrain.CHASM, "改过的地形还在（(10,5) 是深渊）" );
		h.check( h.get( () -> Dungeon.hero.belongings.getItem( WandOfBlastWave.class ) != null ), "背包里的冲击波法杖还在" );
		h.check( h.rest( 2 ) == H.Status.READY, "读档后游戏能继续（又过了 2 个回合）" );
	}
}
