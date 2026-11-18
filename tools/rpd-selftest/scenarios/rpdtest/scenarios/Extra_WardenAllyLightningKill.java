package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import rpdtest.H;
import rpdtest.HarnessError;
import rpdtest.Probe;
import rpdtest.Scenario;

import java.util.List;

/**
 * 顺带发现（不在用户报告里，所以不在 all 里，用 -Scenario extra 跑）。
 * 这一条是 warden 工作包靠代码走查提出来的（它的报告里叫「崩溃 C」，没有运行期验证），这里补上运行期验证：
 * 归玩家的 DM-100 原型机隔着距离用闪电打出最后一击、把典狱长打死。
 *
 * 基线代码的问题：典狱长一死，WardenBossLevel.onBossDeath() 立刻把所有原型机（包括正在放电的这一台）清场，
 * 精灵被 killAndErase()；回到这台机体的 doAttack() 里接着播闪电动画时，精灵已经不在场景里了。
 *
 * 做法：进门开战；把一台未激活的 DM-100 交给玩家（activateByHero()，与电击符石的效果相同），放到离典狱长 2～3 格、
 * 弹道通畅的格子上，让它盯上典狱长；典狱长生命设为 1；其余未激活的机体拿走（典狱长没有可激活的目标，就留在英雄身边）。
 * 然后英雄原地等，最后一击由这台机体在它自己的回合里打出。
 *
 * 期望：不崩溃；典狱长死亡，原型机清场，解除封锁，出口打开，游戏继续。
 */
public class Extra_WardenAllyLightningKill implements Scenario {

	@Override
	public String describe() {
		return "顺带发现：归玩家的 DM-100 原型机隔空用闪电打死典狱长，不应崩溃，Boss 战应正常结束";
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		h.setHeroHP( 2000, 2000 );
		WardenLab.startFight( h );

		final WardenPrototype[] allyRef = { null };
		final Warden[] wardenRef = { null };
		final String[] placed = { null };
		h.world( "give one DM-100 to the hero, put it 2-3 cells from the Warden, set the Warden to 1 HP", () -> {
			WardenBossLevel level = (WardenBossLevel) Dungeon.level;
			Warden w = WardenLab.warden();
			WardenPrototype p = null;
			for (WardenPrototype q : WardenLab.inactivePrototypes()) {
				if (q.model == 100) {
					p = q;
					break;
				}
			}
			if (p == null) throw new HarnessError( "no inactive DM-100 on the field" );
			for (WardenPrototype q : WardenLab.inactivePrototypes()) {
				if (q != p) WardenLab.remove( q );
			}
			p.activateByHero();

			int best = -1;
			for (int c = 0; c < level.length() && best == -1; c++) {
				if (!level.passable[c] || !level.heroInBossArena( c ) || !level.heroFOV[c]) continue;
				if (Actor.findChar( c ) != null && c != p.pos) continue;
				int d = level.distance( c, w.pos );
				if (d < 2 || d > 3) continue;
				if (new Ballistica( c, w.pos, Ballistica.MAGIC_BOLT ).collisionPos != w.pos) continue;
				best = c;
			}
			if (best == -1) throw new HarnessError( "no free visible cell 2-3 cells from the Warden with a clear bolt path" );
			int from = p.pos;
			if (best != p.pos) WardenLab.move( p, best );
			//基线上刚被激活的机体还停在 SLEEPING（另一个顺带发现，见 Extra_ActivatedPrototypeAsleep）：直接让它盯上典狱长
			p.aggro( w );
			w.HP = 1;
			allyRef[0] = p;
			wardenRef[0] = w;
			placed[0] = "ally DM-100 " + WardenLab.xy( from ) + " -> " + WardenLab.xy( best ) + ", Warden at " + WardenLab.xy( w.pos )
					+ " (distance " + level.distance( best, w.pos ) + "), hero at " + WardenLab.xy( Dungeon.hero.pos );
		} );
		final WardenPrototype ally = allyRef[0];
		final Warden warden = wardenRef[0];
		h.require( h.get( () -> ally.isHeroAlly() && ally.isAlive() ), "一台 DM-100 原型机已经归玩家" );
		h.note( placed[0] + "; " + h.get( WardenLab::field ) );

		int hitsBefore = Probe.hitCount();
		int turns = 0;
		while (turns < 30 && h.heroAlive() && h.get( warden::isAlive )) {
			h.rest( 1 );
			turns++;
		}
		h.frames( 60 );

		Probe.Hit last = null;
		List<Probe.Hit> hits = Probe.hits();
		int allyAttacks = 0;
		for (int i = hitsBefore; i < hits.size(); i++) {
			Probe.Hit x = hits.get( i );
			if (x.attacker == ally && x.defender == warden) {
				last = x;
				allyAttacks++;
			}
		}
		final Probe.Hit lastHit = last;
		h.note( "after " + turns + " turn(s): Warden alive=" + h.get( warden::isAlive ) + "; the ally attacked the Warden " + allyAttacks
				+ " time(s); last one: " + (lastHit == null ? "none" : lastHit.toString()) + "; " + h.get( WardenLab::field ) );
		h.require( !h.get( warden::isAlive ), "典狱长（1 点生命）在 30 回合内被打死" );
		h.require( lastHit != null && lastHit.magic
						&& !h.get( () -> Dungeon.level.adjacent( lastHit.attackerPos, lastHit.defenderPos ) ),
				"打死典狱长的最后一击是友军 DM-100 隔空放出的闪电（不是近战）" );

		h.check( h.heroAlive() && h.rest( 2 ) == H.Status.READY, "典狱长被友军机体的闪电打死之后游戏还能继续（又过了 2 个回合）" );
		h.check( h.get( () -> WardenLab.warden() == null ), "典狱长已经从关卡的怪物列表里移除" );
		h.check( h.get( () -> WardenLab.prototypes().isEmpty() ), "典狱长死后原型机全部清场（实际还剩 " + h.get( () -> WardenLab.prototypes().size() ) + " 台）" );
		h.check( !h.get( () -> Dungeon.level.locked ), "典狱长死后关卡解除封锁" );
		h.check( h.get( () -> Dungeon.level.map[WardenLab.c( 6, 2 )] ) == Terrain.EXIT, "典狱长死后出口 (6,2) 打开" );
		h.check( h.get( () -> {
			for (Mob m : Dungeon.level.mobs) {
				if (!m.isAlive()) return false;
			}
			return true;
		} ), "关卡的怪物列表里没有残留的死怪" );

		// 死亡动画 + 淡出按游戏时间走：最多等 1500 帧
		h.waitUntil( () -> gone( ally ) && gone( warden ), 1500 );
		h.check( h.get( () -> gone( ally ) ), "被清场的友军机体的精灵已经从地图上消失" );
		h.check( h.get( () -> gone( warden ) ), "典狱长的精灵已经从地图上消失" );
	}

	/** 只能在渲染线程调用。 */
	private static boolean gone( Mob m ) {
		return m.sprite == null || m.sprite.parent == null || !m.sprite.exists;
	}
}
