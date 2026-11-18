package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体：读灵爆时 DM-175 身上没有护盾（每次读之前把护盾拿掉），灵爆直接打在它们的生命上。
 */
public class E6_PsiBlastTwice_NoShield implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，DM-175 没有护盾时连续读两次灵爆秘卷：不应崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.keepShield = false;
		PsiBlastLab.run( h, o );
	}
}
