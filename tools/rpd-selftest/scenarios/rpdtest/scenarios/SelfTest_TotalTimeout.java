package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：场景总超时。
 * 场景一直在正常推进回合（不是卡回合），但永远不结束。进程内的看门狗应该在超时后判 STUCK 并退出。
 *
 * EXPECTED-VERDICT: STUCK
 * EXPECTED-REASON: total timeout: the scenario did not finish within 12 s
 * TIMEOUT-SECONDS: 12
 */
public class SelfTest_TotalTimeout implements Scenario {

	@Override
	public String describe() {
		return "工具自测：永远不结束的场景，应在总超时（这里设成 12 秒）后被判为 STUCK";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		while (true) {
			h.rest( 1 );
		}
	}
}
