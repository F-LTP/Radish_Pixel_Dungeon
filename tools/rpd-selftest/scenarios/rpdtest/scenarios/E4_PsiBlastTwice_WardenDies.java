package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体：第二次读灵爆之前典狱长只剩 40 点生命，第二次把它炸死。
 * 期望：Boss 正常死亡，原型机清场，解除封锁，出口打开，不崩溃。
 */
public class E4_PsiBlastTwice_WardenDies implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，连续读两次灵爆秘卷，第二次把典狱长炸死：应正常结束 Boss 战，不崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.wardenHpBeforeSecond = 40;
		PsiBlastLab.run( h, o );
	}
}
