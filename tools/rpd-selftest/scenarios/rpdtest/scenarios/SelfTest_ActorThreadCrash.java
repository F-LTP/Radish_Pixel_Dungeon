package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：Actor 线程上的未捕获异常。
 *
 * 放一只在自己的回合里抛异常的怪。真实游戏里 Actor 线程会带着这个异常死掉（= 闪退）。
 * 工具应该判 CRASH，并给出带线程名的完整栈。
 *
 * EXPECTED-VERDICT: CRASH
 * EXPECTED-REASON: uncaught exception in thread "SHPD Actor Thread": java.lang.IllegalStateException: deliberate exception on the actor thread
 * TIMEOUT-SECONDS: 90
 */
public class SelfTest_ActorThreadCrash implements Scenario {

	@Override
	public String describe() {
		return "工具自测：怪物回合里（Actor 线程）抛出的异常，应被判为 CRASH 并带完整栈";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat bomb = new Rat() {
			@Override
			protected boolean act() {
				throw new IllegalStateException( "deliberate exception on the actor thread (rpd-selftest self-test)" );
			}
		};
		h.spawn( bomb, Arena.c( 13, 10 ) );

		h.rest( 1 );
		h.check( false, "不应该走到这里：上一步应该已经被判为 CRASH" );
	}
}
