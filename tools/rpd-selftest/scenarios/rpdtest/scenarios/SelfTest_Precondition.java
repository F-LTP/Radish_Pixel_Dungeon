package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：前置条件不成立（场景没搭对）。结论应该是 ERROR（工具 / 场景的问题），而不是 FAIL（游戏的问题）。
 *
 * EXPECTED-VERDICT: ERROR
 * EXPECTED-REASON: scenario precondition not met: deliberately unmet precondition
 * TIMEOUT-SECONDS: 90
 */
public class SelfTest_Precondition implements Scenario {

	@Override
	public String describe() {
		return "工具自测：前置条件不成立的场景，结论应为 ERROR 而不是 FAIL";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.check( h.heroAlive(), "英雄活着" );
		h.require( h.depth() == 99, "deliberately unmet precondition (depth == 99)" );
		h.check( false, "不应该走到这里" );
	}
}
