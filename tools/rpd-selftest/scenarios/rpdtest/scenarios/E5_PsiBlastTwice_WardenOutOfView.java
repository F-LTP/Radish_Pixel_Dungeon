package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体：读灵爆时典狱长不在英雄视野内（被挪到了战斗区最远的角落），灵爆炸不到它。
 */
public class E5_PsiBlastTwice_WardenOutOfView implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，典狱长在视野外时连续读两次灵爆秘卷：不应崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.wardenInView = false;
		PsiBlastLab.run( h, o );
	}
}
