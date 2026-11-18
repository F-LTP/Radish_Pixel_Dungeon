package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import rpdtest.H;

/**
 * 测试场（TestArenaLevel，21x21，四周是墙，内部全是空地，入口在正中 (10,10)）上的常用搭建动作。
 * 不是场景，只是场景们共用的辅助类。
 */
final class Arena {

	private Arena() {}

	static final int W = 21;
	static final String SEED = "RPDTEST";

	/** (x, y) → 测试场里的格子编号。 */
	static int c( int x, int y ) {
		return x + y * W;
	}

	/** 开新局进测试场，英雄在 (10,10)，生命调高（免得被场景里的意外伤害打死）。 */
	static void start( H h ) {
		start( h, HeroClasses.WARRIOR );
	}

	static void start( H h, HeroClass cls ) {
		h.newGameInArena( cls, SEED );
		h.require( h.heroPos() == c( 10, 10 ), "英雄出生在测试场正中 (10,10)" );
		h.setHeroHP( 300, 300 );
	}

	/** 把 x >= x0 的所有内部格子变成深渊（一直到东墙），整列都是，怪绕不过去。 */
	static void chasmEastOf( H h, int x0 ) {
		h.fillTerrain( x0, 1, W - 2, W - 2, Terrain.CHASM );
		h.require( h.get( () -> Dungeon.level.pit[c( x0, 10 )] && !Dungeon.level.passable[c( x0, 10 )] ),
				"(" + x0 + ",10) 是深渊格（pit，且不可通行）" );
	}

	/** 一只站着不动、血很厚的普通怪（老鼠），用来当被推的对象。 */
	static Rat dummy( H h, int cell ) {
		Rat rat = new Rat();
		rat.HP = rat.HT = 500;
		rat.state = rat.PASSIVE;
		return h.spawn( rat, cell );
	}

	static String describe( H h, Mob mob ) {
		return h.get( () -> mob.getClass().getSimpleName() + " alive=" + mob.isAlive() + " HP=" + mob.HP + "/" + mob.HT
				+ " pos=" + h.cellStr( mob.pos ) + " terrainAtPos=" + Dungeon.level.map[mob.pos]
				+ " pit=" + Dungeon.level.pit[mob.pos] + " inLevelMobs=" + Dungeon.level.mobs.contains( mob ) );
	}
}
