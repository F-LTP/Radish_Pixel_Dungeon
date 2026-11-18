package rpdtest;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.events.HitCheckEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.ShieldBreakEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 纯旁观的事件订阅者：记录每一次「命中判定」（Char.hit，近战 / 远程 / 法术都会走到）。
 * 用它来断言「某个怪物有没有出手攻击」，比看 HP 可靠（打不中 / 被护甲吃掉时 HP 不变）。
 * 另外记录「护盾被打破」事件。不修改事件、不取消事件。
 */
public final class Probe {

	private Probe() {}

	public static final class Hit {
		public final Char attacker;
		public final Char defender;
		public final boolean magic;
		public final float time;
		public final int attackerPos;
		public final int defenderPos;

		Hit( Char attacker, Char defender, boolean magic ) {
			this.attacker = attacker;
			this.defender = defender;
			this.magic = magic;
			this.time = Actor.now();
			this.attackerPos = attacker.pos;
			this.defenderPos = defender.pos;
		}

		@Override
		public String toString() {
			return String.format( java.util.Locale.ROOT, "t=%.1f %s@%d -> %s@%d%s", time,
					attacker.getClass().getSimpleName(), attackerPos,
					defender.getClass().getSimpleName(), defenderPos, magic ? " (magic)" : "" );
		}
	}

	private static final List<Hit> hits = new ArrayList<>();
	private static boolean installed = false;

	/** 被 EventManager 反射调用。 */
	public static void onHitCheck( HitCheckEvent e ) {
		Hit h = new Hit( e.getAttacker(), e.getDefender(), e.isMagic() );
		synchronized (hits) {
			hits.add( h );
		}
		Log.line( "PROBE", "hit-check " + h );
	}

	// ---- 护盾被打破（ShieldBreakEvent：受击前护盾 > 0、受击后护盾归零）

	private static final List<Char> shieldBreaks = new ArrayList<>();

	/** 被 EventManager 反射调用。 */
	public static void onShieldBreak( ShieldBreakEvent e ) {
		synchronized (shieldBreaks) {
			shieldBreaks.add( e.getTarget() );
		}
		Log.line( "PROBE", String.format( java.util.Locale.ROOT, "shield-break t=%.1f %s@%d absorbed=%d", Actor.now(),
				e.getTarget().getClass().getSimpleName(), e.getTarget().pos, e.getAbsorbed() ) );
	}

	/** target 的护盾到目前为止被「打破」过几次（自然衰减到 0 不算）。 */
	public static int shieldBreaks( Char target ) {
		int n = 0;
		synchronized (shieldBreaks) {
			for (Char c : shieldBreaks) {
				if (c == target) n++;
			}
		}
		return n;
	}

	/** 在渲染线程、英雄可行动时调用（此时 Actor 线程在等待，不会和 emit 抢）。 */
	static synchronized void install() {
		if (installed) return;
		EventManager.registerSubscriber( HitCheckEvent.class, Probe.class, "onHitCheck", Integer.MAX_VALUE );
		EventManager.registerSubscriber( ShieldBreakEvent.class, Probe.class, "onShieldBreak", Integer.MAX_VALUE );
		installed = true;
	}

	public static List<Hit> hits() {
		synchronized (hits) {
			return new ArrayList<>( hits );
		}
	}

	public static int hitCount() {
		synchronized (hits) {
			return hits.size();
		}
	}

	/** 从第 fromIndex 条记录起，attacker 对 defender 发起的命中判定次数（任一参数为 null 表示不限）。 */
	public static int count( Char attacker, Char defender, int fromIndex ) {
		int n = 0;
		synchronized (hits) {
			for (int i = Math.max( 0, fromIndex ); i < hits.size(); i++) {
				Hit h = hits.get( i );
				if ((attacker == null || h.attacker == attacker) && (defender == null || h.defender == defender)) {
					n++;
				}
			}
		}
		return n;
	}
}
