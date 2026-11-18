package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：断言失败。一条通过的断言 + 一条不成立的断言，结论应该是 FAIL，理由是那条不成立的断言。
 *
 * EXPECTED-VERDICT: FAIL
 * EXPECTED-REASON: deliberately failing check
 * TIMEOUT-SECONDS: 90
 */
public class SelfTest_FailedCheck implements Scenario {

	@Override
	public String describe() {
		return "工具自测：有一条断言不成立的场景，结论应为 FAIL";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.check( h.heroAlive(), "英雄活着" );
		h.check( h.depth() == 99, "deliberately failing check: depth == 99 (actual " + h.depth() + ")" );
		h.check( h.rest( 1 ) == H.Status.READY, "断言失败后场景继续往下跑（check 不会中断场景）" );
	}
}
