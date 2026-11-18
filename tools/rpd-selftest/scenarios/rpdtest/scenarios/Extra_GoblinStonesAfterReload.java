package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Goblin;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Reflect;
import rpdtest.Scenario;

import java.util.List;

/**
 * 顺带发现（不在用户的报告里）：哥布林「还能不能丢石头」的标记 canZap 不进存档，读档后变回初始值 true，
 * 于是石头已经丢完的哥布林读档（或英雄离开这一层再回来）之后还能再丢一块。
 *
 * 布局同 C1，但哥布林是普通的（没有精英词条）：英雄 (10,10)；x=11、x=12 两整列是深渊；哥布林 (13,10)，距离 3。
 *  1. 等它把 2 块石头丢完；
 *  2. 对照：再等 3 回合，它不应再出手（没石头、隔着深渊、也没有索敌词条）；
 *  3. 存档再读档；
 *  4. 再等 6 回合：仍然不应出手。
 *
 * 石头数读的是哥布林的私有字段 StoneRemain。
 */
public class Extra_GoblinStonesAfterReload implements Scenario {

	@Override
	public String describe() {
		return "石头丢完的哥布林，存档读档之后不应该又能丢石头";
	}

	private static int stones( H h, Goblin goblin ) {
		return h.get( () -> (int) Reflect.get( goblin, "StoneRemain" ) );
	}

	/** 从第 from 条探针记录起，任意哥布林出手的次数与类型。 */
	private static String goblinAttacks( int from ) {
		List<Probe.Hit> all = Probe.hits();
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < all.size(); i++) {
			if (all.get( i ).attacker instanceof Goblin) sb.append( all.get( i ).magic ? "stone " : "melee " );
		}
		return sb.toString().trim();
	}

	private static int count( String kinds ) {
		return kinds.isEmpty() ? 0 : kinds.split( " " ).length;
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.fillTerrain( 11, 1, 12, 19, Terrain.CHASM );

		Goblin goblin = new Goblin();
		goblin.HP = goblin.HT = 500;
		goblin.state = goblin.HUNTING;
		h.spawn( goblin, Arena.c( 13, 10 ) );
		h.world( "make the goblin hunt the hero", () -> goblin.aggro( Dungeon.hero ) );
		h.require( stones( h, goblin ) == 2, "哥布林开始有 2 块石头" );

		// ---- 1. 等到石头丢完
		int turns = 0;
		while (turns < 15 && stones( h, goblin ) > 0) {
			h.rest( 1 );
			turns++;
		}
		h.frames( 20 );
		h.require( stones( h, goblin ) == 0, "哥布林在 15 回合内把石头丢完了（StoneRemain == 0）" );
		h.note( "stones ran out after " + turns + " turn(s); attacks so far: [" + goblinAttacks( 0 ) + "]" );

		// ---- 2. 对照：不读档，再等 3 回合
		int mark = Probe.hitCount();
		h.rest( 3 );
		h.frames( 20 );
		String control = goblinAttacks( mark );
		h.check( count( control ) == 0, "对照（不读档）：石头丢完后的 3 回合里哥布林没有再出手（实际 " + count( control ) + " 次 [" + control + "]）" );
		h.require( h.get( () -> goblin.pos ) == Arena.c( 13, 10 ), "哥布林还站在 (13,10)（隔着深渊过不来）" );

		// ---- 3. 存档再读档
		h.saveAndReload();
		Goblin reloaded = h.get( () -> {
			for (Mob m : Dungeon.level.mobs) {
				if (m instanceof Goblin) return (Goblin) m;
			}
			return null;
		} );
		h.require( reloaded != null && reloaded != goblin, "读档后关卡里有一只哥布林（新实例）" );
		h.require( stones( h, reloaded ) == 0, "读档后哥布林的石头数仍然是 0" );
		String stateAfterLoad = h.get( () -> reloaded.state == reloaded.HUNTING ? "HUNTING" : reloaded.state == reloaded.WANDERING ? "WANDERING" : "other" );
		Object canZap = Reflect.hasField( Goblin.class, "canZap" ) ? h.get( () -> Reflect.get( reloaded, "canZap" ) ) : "(no such field)";
		h.note( "after reload: goblin at " + h.cellStr( h.get( () -> reloaded.pos ) ) + " StoneRemain=0 canZap=" + canZap + " state=" + stateAfterLoad );
		if (!"HUNTING".equals( stateAfterLoad )) {
			h.world( "make the reloaded goblin hunt the hero again", () -> reloaded.aggro( Dungeon.hero ) );
		}

		// ---- 4. 再等 6 回合
		int mark2 = Probe.hitCount();
		h.rest( 6 );
		h.frames( 20 );
		String after = goblinAttacks( mark2 );
		h.note( "6 turns after the reload: " + count( after ) + " attack(s) by the goblin [" + after + "]; StoneRemain=" + stones( h, reloaded ) );
		h.check( count( after ) == 0, "读档后的 6 回合里，石头已经丢完的哥布林没有再出手（实际 " + count( after ) + " 次 [" + after + "]）" );
	}
}
