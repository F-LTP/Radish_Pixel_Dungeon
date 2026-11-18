package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体：典狱长在视野外（所以第一次灵爆不会让它掉血、不会补刷新的原型机），
 * 第二次读灵爆时英雄只剩 1 点生命，被自伤杀死。
 * 这个变体专门走「英雄被自己的灵爆炸死」这条路：期望正常死亡（游戏结束），不崩溃、不卡死。
 */
public class E8_PsiBlastTwice_HeroDies_NoWarden implements Scenario {

	@Override
	public String describe() {
		return "典狱长战（典狱长在视野外），第二次读灵爆的自伤杀死英雄：应正常死亡，不崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.wardenInView = false;
		o.heroDiesOnSecond = true;
		PsiBlastLab.run( h, o );
	}
}
