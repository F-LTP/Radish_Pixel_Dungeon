package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体：除了报告里那 4 台激活的机体，开战时刷出来的另外 7 台未激活 DM-100 还躺在场上
 * （刚开战不久就读灵爆的情形）。
 */
public class E3_PsiBlastTwice_InactiveLeft implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，场上还躺着 7 台未激活的 DM-100 时连续读两次灵爆秘卷：不应崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.removeInactive = false;
		PsiBlastLab.run( h, o );
	}
}
