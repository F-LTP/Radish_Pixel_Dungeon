package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Scenario;

/**
 * 工具自测：英雄在怪物的回合里（Actor 线程上）被打死。
 * 工具应该把「英雄已死」识别成一个正常的结束状态（Status.DEAD），而不是当成卡回合。
 */
public class SelfTest_HeroKilledByMob implements Scenario {

	@Override
	public String describe() {
		return "工具自测：英雄被旁边的怪打死，等待应返回 DEAD 而不是判卡回合";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat rat = new Rat();
		rat.HP = rat.HT = 500;
		rat.state = rat.HUNTING;
		h.spawn( rat, Arena.c( 11, 10 ) );
		h.world( "the rat hunts the hero", () -> rat.aggro( Dungeon.hero ) );
		h.setHeroHP( 1, 300 );

		H.Status st = H.Status.READY;
		int turns = 0;
		while (turns < 60 && st == H.Status.READY) {
			st = h.rest( 1 );
			turns++;
		}
		h.note( "hero status after " + turns + " turn(s): " + st + "; attacks by the rat: " + Probe.count( rat, null, 0 )
				+ "; hero HP=" + h.get( () -> Dungeon.hero.HP ) );
		h.check( st == H.Status.DEAD, "英雄被老鼠打死后，等待返回 DEAD（实际 " + st + "）" );
		h.check( !h.heroAlive(), "英雄确实死了" );
		h.check( Probe.count( rat, Dungeon.hero, 0 ) >= 1, "事件探针记到了老鼠对英雄的攻击" );
		h.frames( 60 );
		h.check( h.get( () -> Dungeon.hero != null && !Dungeon.hero.isAlive() ), "死亡之后游戏画面继续正常运行（又过了 60 帧没有异常）" );
	}
}
