package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体：第二次读灵爆时英雄只剩 1 点生命，被自伤杀死。期望：正常死亡（游戏结束），不崩溃、不卡死。
 */
public class E2_PsiBlastTwice_HeroDies implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，连续读两次灵爆秘卷，第二次的自伤杀死英雄：应正常死亡，不崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.heroDiesOnSecond = true;
		PsiBlastLab.run( h, o );
	}
}
