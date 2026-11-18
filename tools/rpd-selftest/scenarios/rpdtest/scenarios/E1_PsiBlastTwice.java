package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 的基本形态：典狱长战，场上 2 台敌对 DM-175（带盾）、1 台敌对 DM-100、1 台友方 DM-175（带盾），
 * 其余未激活的原型机都已不在场；典狱长在视野内。连续读两次灵爆秘卷，英雄两次都存活。
 */
public class E1_PsiBlastTwice implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，按报告摆场，连续读两次灵爆秘卷（英雄存活）：不应崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.run( h, new PsiBlastLab.Opt() );
	}
}
