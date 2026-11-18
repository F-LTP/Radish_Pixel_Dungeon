package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Goblin;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Reflect;
import rpdtest.Scenario;

import java.util.List;

/**
 * 用户报告 (c)：带「索敌」精英词条（ChampionEnemy.Projecting，攻击距离 4 格）的哥布林，
 * 把 2 块石头丢完之后，隔着几格应该仍然能攻击到英雄。
 *
 * 布局：英雄 (10,10)；x=11、x=12 两整列是深渊（哥布林走不过来）；哥布林 (13,10)，距离 3。
 * 用事件探针数「哥布林 → 英雄」的命中判定次数（打没打中都算出手）：石头丢完后的 10 个回合里还得有。
 *
 * 哥布林的石头数读的是它的私有字段 StoneRemain（同时读 canZap）。
 */
public class C1_ProjectingGoblin implements Scenario {

	@Override
	public String describe() {
		return "索敌精英哥布林丢完石头后，隔着 2 格深渊仍应能攻击到英雄";
	}

	private static int stones( H h, Goblin goblin ) {
		return h.get( () -> (int) Reflect.get( goblin, "StoneRemain" ) );
	}

	/** 英雄脚下那一格地上的投石数量（哥布林丢石头没打中时石头会掉在目标脚下）。 */
	private static int stonesOnFloor( H h ) {
		return h.get( () -> {
			int n = 0;
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				for (Item it : heap.items) {
					if (it instanceof ThrowingStone) n += it.quantity();
				}
			}
			return n;
		} );
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.fillTerrain( 11, 1, 12, 19, Terrain.CHASM );

		Goblin goblin = new Goblin();
		goblin.HP = goblin.HT = 500;
		goblin.state = goblin.HUNTING;
		h.spawn( goblin, Arena.c( 13, 10 ) );
		h.affect( goblin, ChampionEnemy.Projecting.class );
		h.world( "make the goblin hunt the hero", () -> goblin.aggro( Dungeon.hero ) );
		h.require( h.get( () -> goblin.buff( ChampionEnemy.Projecting.class ) != null ), "哥布林带有 Projecting 词条" );
		h.require( stones( h, goblin ) == 2, "哥布林开始有 2 块石头" );

		// ---- 1. 等到石头丢完（最多 15 回合）
		int turns = 0;
		while (turns < 15 && stones( h, goblin ) > 0) {
			h.rest( 1 );
			turns++;
		}
		h.frames( 20 );
		int attacksSoFar = Probe.count( goblin, null, 0 );
		int left = stones( h, goblin );
		h.note( "after " + turns + " turn(s): StoneRemain=" + left + ", attacks by the goblin so far: " + attacksSoFar
				+ " [" + kinds( goblin, 0 ) + "]; goblin at " + h.cellStr( h.get( () -> goblin.pos ) ) + ", hero at " + h.cellStr( h.heroPos() ) );
		if (left > 0) {
			// 这个版本的哥布林 15 回合都没把石头丢完（比如索敌时优先用普通攻击）：直接把石头收走，继续测「没石头之后」
			h.note( "the goblin still has " + left + " stone(s) after 15 turns; taking them away to test the no-stones case" );
			h.world( "take the goblin's remaining stones away", () -> {
				Reflect.set( goblin, "StoneRemain", 0 );
				if (Reflect.hasField( Goblin.class, "canZap" )) Reflect.set( goblin, "canZap", false );
			} );
		}
		h.require( stones( h, goblin ) == 0, "哥布林的石头丢完了（StoneRemain == 0）" );
		h.require( h.get( () -> Dungeon.level.distance( goblin.pos, Dungeon.hero.pos ) ) == 3, "哥布林仍然隔着 2 格深渊（距离 3）" );
		h.require( h.get( () -> goblin.buff( ChampionEnemy.Projecting.class ).canAttackWithExtraReach( Dungeon.hero ) ),
				"按 Projecting 词条自己的判定，英雄在它的攻击范围内" );

		// ---- 2. 再等 10 回合：应该继续挨打
		int mark = Probe.hitCount();
		int floorStonesBefore = stonesOnFloor( h );
		h.rest( 10 );
		h.frames( 20 );
		int more = Probe.count( goblin, null, mark );
		int floorStonesAfter = stonesOnFloor( h );
		h.note( "10 turns after the stones ran out: " + more + " more attack(s) by the goblin [" + kinds( goblin, mark ) + "]"
				+ "; goblin at " + h.cellStr( h.get( () -> goblin.pos ) ) + " StoneRemain=" + stones( h, goblin )
				+ "; throwing stones lying on the floor: " + floorStonesBefore + " -> " + floorStonesAfter );
		h.check( more >= 1, "石头丢完后的 10 回合里，哥布林隔着深渊又攻击了英雄（实际 " + more + " 次）" );
		h.check( more >= 5, "索敌精英应该几乎每回合都出手（10 回合里 >= 5 次，实际 " + more + " 次）" );
		h.check( floorStonesAfter <= floorStonesBefore,
				"石头丢完之后不应再凭空丢出新的石头（地上的投石数量 " + floorStonesBefore + " -> " + floorStonesAfter + "）" );
	}

	/** 从第 from 条探针记录起，哥布林每次出手的类型：stone = 丢石头（按法术命中判定），melee = 普通攻击。 */
	private static String kinds( Goblin goblin, int from ) {
		List<Probe.Hit> all = Probe.hits();
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < all.size(); i++) {
			if (all.get( i ).attacker == goblin) sb.append( all.get( i ).magic ? "stone " : "melee " );
		}
		return sb.toString().trim();
	}
}
