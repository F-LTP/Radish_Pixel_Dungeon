package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * 顺带发现（不在用户报告里，所以不在 all 里，用 -Scenario extra 跑）：
 * 原型机被激活时只改了阵营，没有改 AI 状态，于是它还停在怪物默认的 SLEEPING（睡觉）状态，
 * 要等「睡着的怪发现英雄」的潜行判定通过才会醒（英雄越远越难醒），或者挨一下打才醒。
 * 结果就是典狱长刚激活的机器往往原地再「睡」上好几个回合。
 *
 * 做法（测试场）：一台 DM-100 原型机放在英雄正东 3 格（DM-100 能隔空放电，不用走过来），由典狱长激活，
 * 然后英雄原地等 12 回合，记录它每回合的 AI 状态和第一次出手是第几回合。
 * 期望：激活后 2 回合内出手。
 */
public class Extra_ActivatedPrototypeAsleep implements Scenario {

	@Override
	public String describe() {
		return "顺带发现：被典狱长激活的 DM-100 原型机应立刻投入战斗（2 回合内出手），而不是继续睡觉";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.setHeroHP( 2000, 2000 );
		WardenPrototype dm = new WardenPrototype( 100 );
		dm.HP = dm.HT = 500;
		h.spawn( dm, Arena.c( 13, 10 ) );
		h.world( "activate the DM-100 as the Warden's ally (hostile)", dm::activateByWarden );
		h.require( h.get( dm::isWardenAlly ), "原型机已被典狱长激活（敌对）" );
		h.note( "AI state right after activation: " + h.get( () -> WardenLab.stateName( dm ) ) );

		int firstAttack = -1;
		List<String> states = new ArrayList<>();
		for (int t = 1; t <= 12 && h.heroAlive(); t++) {
			h.rest( 1 );
			states.add( h.get( () -> WardenLab.stateName( dm ) ) );
			if (firstAttack < 0 && Probe.count( dm, Dungeon.hero, 0 ) > 0) firstAttack = t;
		}
		h.note( "AI state after each of the 12 turns: " + states + "; first attack on the hero: "
				+ (firstAttack < 0 ? "none within 12 turns" : "turn " + firstAttack) + "; total attacks: " + Probe.count( dm, Dungeon.hero, 0 ) );
		h.check( firstAttack >= 1 && firstAttack <= 2,
				"激活后 2 回合内出手（实际：" + (firstAttack < 0 ? "12 回合内一次都没出手" : "第 " + firstAttack + " 回合才第一次出手") + "）" );
	}
}
