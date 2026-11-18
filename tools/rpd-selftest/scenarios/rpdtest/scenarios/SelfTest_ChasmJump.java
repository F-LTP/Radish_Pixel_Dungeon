package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：确认窗口（H.chooseOption）+ 换层 + 正常关卡生成。
 * 英雄自己走进深渊：游戏弹「确定要跳下去吗」的确认窗，选「是」，英雄掉到第 2 层（一个正常生成的关卡）。
 * 顺带是 (a) 的对照：深渊和坠落本身是好的，坏的只是「被击退进深渊」。
 */
public class SelfTest_ChasmJump implements Scenario {

	@Override
	public String describe() {
		return "工具自测：英雄自己跳进深渊（确认窗选「是」），掉到正常生成的第 2 层";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.chasmEastOf( h, 12 );
		h.walkTo( Arena.c( 11, 10 ) );

		boolean changed = h.tryTap( Arena.c( 12, 10 ) );
		h.frames( 60 );
		h.note( "tapped the chasm cell: hero at " + h.cellStr( h.heroPos() ) + ", windows=" + h.windowNames() + ", anything happened=" + changed );
		h.require( h.findWindow( WndOptions.class ) != null, "走向深渊时游戏弹出了确认窗（WndOptions）" );
		h.check( h.heroPos() == Arena.c( 11, 10 ) && h.depth() == 1, "确认之前英雄还站在深渊边上" );

		h.chooseOption( 0 );
		H.Status st = h.waitReady();
		h.frames( 30 );
		h.note( "after confirming the jump: status=" + st + " depth=" + h.depth() + " level=" + h.get( () -> Dungeon.level.getClass().getSimpleName() )
				+ " hero HP=" + h.get( () -> Dungeon.hero.HP ) + "/" + h.get( () -> Dungeon.hero.HT ) );
		h.check( st == H.Status.READY && h.depth() == 2, "英雄掉到了第 2 层（实际第 " + h.depth() + " 层）" );
		h.check( !h.get( () -> Dungeon.level instanceof TestArenaLevel ), "第 2 层是正常生成的关卡，不是测试场" );
		h.check( h.heroAlive() && h.get( () -> Dungeon.hero.HP ) < 300, "英雄摔伤了但还活着" );
		h.check( h.rest( 2 ) == H.Status.READY, "在第 2 层游戏能继续（又过了 2 个回合）" );
	}
}
