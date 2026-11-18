package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户报告 (f) 的另一种「盾消失」：没有人打它，护盾随时间自然衰减到 0。
 *
 * 做法（测试场）：一台被玩家激活的（友方）DM-175 原地不动，英雄原地等待，每回合看一次护盾值，最多 220 回合。
 * 期望：护盾只减不增，减到 0 之后保持 0。
 */
public class F2_Dm175ShieldAfterDecay implements Scenario {

	@Override
	public String describe() {
		return "DM-175 原型机的护盾自然衰减完之后，不应重新获得护盾";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		WardenPrototype dm = new WardenPrototype( 175 );
		h.spawn( dm, Arena.c( 13, 10 ) );
		h.world( "activate the DM-175 as the hero's ally and make it stand still", () -> {
			dm.activateByHero();
			dm.state = dm.PASSIVE;
		} );
		h.rest( 2 );

		int shield0 = h.get( dm::shielding );
		h.note( "2 turns after activation: shield=" + shield0 );
		h.check( shield0 > 0, "激活后的 DM-175 带有护盾（实际 " + shield0 + "）" );

		List<Integer> samples = new ArrayList<>();
		samples.add( shield0 );
		int prev = shield0;
		int zeroStreak = shield0 == 0 ? 1 : 0;
		int grewAtTurn = -1;
		int turns = 0;
		while (turns < 220 && grewAtTurn < 0 && zeroStreak < 12) {
			h.rest( 1 );
			turns++;
			int cur = h.get( dm::shielding );
			samples.add( cur );
			if (cur > prev) grewAtTurn = turns;
			zeroStreak = cur == 0 ? zeroStreak + 1 : 0;
			prev = cur;
		}
		h.note( "shield value per turn (" + turns + " turns): " + samples );
		h.require( grewAtTurn >= 0 || zeroStreak >= 12, "220 回合内护盾要么衰减到 0 并保持，要么出现了回涨" );
		h.check( grewAtTurn < 0, "护盾只减不增，衰减到 0 之后保持为 0"
				+ (grewAtTurn < 0 ? "" : "（实际第 " + grewAtTurn + " 回合护盾从 " + samples.get( grewAtTurn - 1 ) + " 涨回 " + samples.get( grewAtTurn ) + "）") );
	}
}
