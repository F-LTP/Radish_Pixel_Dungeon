package rpdtest.scenarios;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自检（与任何 bug 无关，基线和修复后都应该 PASS）：
 * 隐藏窗口启动真引擎 → 开新局进测试场 → 走路、等待、生成怪并打一下、改地形。
 */
public class Smoke implements Scenario {

	@Override
	public String describe() {
		return "工具自检：隐藏窗口 + 真引擎能开局、走路、等待、攻击";
	}

	@Override
	public void run( H h ) {
		h.newGameInArena( HeroClasses.WARRIOR, "RPDTEST" );

		h.check( h.get( () -> Dungeon.level instanceof TestArenaLevel ), "第 1 层是测试场 TestArenaLevel" );
		h.check( h.get( () -> Dungeon.depth ) == 1, "开局在第 1 层" );
		h.check( h.get( () -> Gdx.audio.getClass().getSimpleName() ).contains( "Mock" ), "音频是空实现（MockAudio）" );

		int start = h.heroPos();
		h.note( "hero starts at " + h.cellStr( start ) + ", level " + h.get( () -> Dungeon.level.width() + "x" + Dungeon.level.height() ) );

		// 走到 (5,10)：5 格直线
		int target = h.get( () -> h.xy( 5, 10 ) );
		float t0 = h.now();
		h.walkTo( target );
		float t1 = h.now();
		h.check( h.heroPos() == target, "英雄走到了 (5,10)" );
		h.check( t1 - t0 >= 4.5f && t1 - t0 <= 6f, "走 5 格花了约 5 回合（实际 " + (t1 - t0) + "）" );

		// 等 3 回合
		h.rest( 3 );
		float t2 = h.now();
		h.check( Math.abs( (t2 - t1) - 3f ) < 0.01f, "等待 3 次过了 3 回合（实际 " + (t2 - t1) + "）" );

		// 生成一只老鼠并攻击它
		Rat rat = new Rat();
		rat.state = rat.PASSIVE;
		h.spawn( rat, h.get( () -> h.xy( 4, 10 ) ) );
		int hp0 = h.get( () -> rat.HP );
		for (int i = 0; i < 6 && h.get( rat::isAlive ); i++) {
			h.attack( h.get( () -> rat.pos ) );
		}
		h.check( !h.get( rat::isAlive ) || h.get( () -> rat.HP ) < hp0, "攻击老鼠造成了伤害或击杀（HP " + hp0 + " → " + h.get( () -> rat.HP ) + "）" );

		// 改地形：墙挡路
		int wall = h.get( () -> h.xy( 6, 10 ) );
		h.setTerrain( wall, Terrain.WALL );
		h.check( h.get( () -> !Dungeon.level.passable[wall] ), "改成墙的格子不可通行" );

		h.check( h.heroAlive(), "英雄还活着" );
	}
}
