package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：卡回合检测。
 *
 * 放一只「act() 返回 false 却永远不调 next()」的怪——这正是游戏里卡回合 bug 的标准形态：
 * Actor 线程停在这只怪身上等它「演完」，英雄永远等不到自己的回合。
 * 工具应该在几秒内判定 STUCK，并在报告里写出当前 actor 和所有线程的栈。
 *
 * EXPECTED-VERDICT: STUCK
 * EXPECTED-REASON: stuck turn: current actor = rpdtest.scenarios.SelfTest_StuckTurn$1
 * TIMEOUT-SECONDS: 90
 */
public class SelfTest_StuckTurn implements Scenario {

	@Override
	public String describe() {
		return "工具自测：一只永远不结束自己回合的怪，应被判为 STUCK（卡回合）";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		// 自测不想等默认的 15 秒
		h.stuckMs = 3000;
		h.stuckFrames = 100;

		Rat frozen = new Rat() {
			@Override
			protected boolean act() {
				// 「我的动作还没演完」，但永远不会有人来调 next()
				return false;
			}
		};
		h.spawn( frozen, Arena.c( 13, 10 ) );

		h.rest( 1 );
		h.check( false, "不应该走到这里：上一步的等待应该已经被判为卡回合" );
	}
}
