package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：玩家输入的那一下（点击 / 用物品的回调，跑在渲染线程上）里抛出的未捕获异常。
 * 工具应该判 CRASH，并在结论里写明是哪一步输入。
 *
 * EXPECTED-VERDICT: CRASH
 * EXPECTED-REASON: uncaught exception in render thread, during player input [deliberately failing input]
 * TIMEOUT-SECONDS: 90
 */
public class SelfTest_InputCrash implements Scenario {

	@Override
	public String describe() {
		return "工具自测：玩家输入的回调里抛出的异常，应被判为 CRASH，并写明是哪一步输入";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.input( "deliberately failing input", () -> {
			throw new IllegalStateException( "deliberate exception inside a player-input callback (rpd-selftest self-test)" );
		} );
		h.check( false, "不应该走到这里：上一步应该已经被判为 CRASH" );
	}
}
