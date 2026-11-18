package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体（最接近真实操作的时序）：第一次读完后英雄失明，原地等到失明自然结束（约 10 回合，期间照常挨打），再读第二次。
 */
public class E7_PsiBlastTwice_WaitBlindness implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，读一次灵爆，等失明自然结束后再读一次：不应崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.waitOutBlindness = true;
		PsiBlastLab.run( h, o );
	}
}
