package rpdtest;

/**
 * 一个场景 = 一个实现本接口、带无参构造的类，放在 scenarios/rpdtest/scenarios/ 下。
 * 类名就是场景名（run.ps1 -Scenario <类名>）。每个场景在独立的游戏进程里运行。
 */
public interface Scenario {

	/** 一句话说明：这个场景验证的「期望行为」是什么。 */
	String describe();

	/**
	 * 在场景线程里执行。通过 {@link H} 驱动游戏：
	 * 断言用 h.check（期望行为，不成立 = FAIL）；前置条件用 h.require（不成立 = ERROR）。
	 */
	void run( H h ) throws Exception;
}
