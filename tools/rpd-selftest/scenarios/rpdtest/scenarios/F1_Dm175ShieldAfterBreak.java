package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户报告 (f)：「dm175 仍然会在盾消失后获得盾」。
 *
 * 做法（测试场）：一台被典狱长激活的（敌对）DM-175 站在英雄旁边，英雄一刀一刀把它的护盾（Barrier）打掉，
 * 之后再过 10 回合，每回合看一次护盾值。期望：护盾打掉之后一直是 0。
 * （DM-175 的生命调到 5000，免得被打死；英雄 5000 血，免得被它打死。）
 */
public class F1_Dm175ShieldAfterBreak implements Scenario {

	@Override
	public String describe() {
		return "DM-175 原型机激活后护盾被打掉，之后 10 回合不应重新获得护盾";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.setHeroHP( 5000, 5000 );
		WardenPrototype dm = new WardenPrototype( 175 );
		dm.HP = dm.HT = 5000;
		h.spawn( dm, Arena.c( 11, 10 ) );
		h.world( "activate the DM-175 as the Warden's ally (hostile)", dm::activateByWarden );
		h.rest( 2 );

		int shield0 = h.get( dm::shielding );
		h.note( "2 turns after activation: shield=" + shield0 );
		h.check( shield0 > 0, "激活后的 DM-175 带有护盾（实际 " + shield0 + "）" );

		// 把护盾打掉
		int attacks = 0;
		int prev = shield0;
		boolean broken = shield0 == 0;
		boolean grewBack = false;
		List<Integer> trace = new ArrayList<>();
		while (attacks < 250 && !broken) {
			h.attack( h.get( () -> dm.pos ) );
			attacks++;
			int cur = h.get( dm::shielding );
			trace.add( cur );
			if (cur > prev) grewBack = true;
			broken = Probe.shieldBreaks( dm ) > 0 || cur == 0 || grewBack;
			prev = cur;
		}
		h.note( "hero attacked " + attacks + " time(s); shield after each attack: " + trace
				+ "; shield-break events: " + Probe.shieldBreaks( dm ) + (grewBack ? "; the shield value went UP between two attacks" : "") );
		h.require( broken, "英雄把 DM-175 的护盾打掉了" );

		// 之后 10 回合，每回合看一次
		List<Integer> samples = new ArrayList<>();
		samples.add( h.get( dm::shielding ) );
		for (int i = 0; i < 10; i++) {
			h.rest( 1 );
			samples.add( h.get( dm::shielding ) );
		}
		int max = 0;
		for (int s : samples) max = Math.max( max, s );
		h.note( "shield right after the break and after each of the next 10 turns: " + samples );
		h.check( max == 0, "护盾被打掉之后的 10 回合里 DM-175 一直没有护盾（实际观察到的护盾值：" + samples + "）" );
		h.check( h.get( dm::isAlive ) && h.heroAlive(), "DM-175 和英雄都还活着（场景没有因为别的原因失效）" );
	}
}
