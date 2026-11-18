package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Senbon;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 千本（Senbon）：投掷一次之后英雄应回到可行动状态，并且恰好花掉一次投掷的时间。
 *
 * 走查怀疑：Senbon.cast 用 playAnimationNoTime(null) 把英雄挂起，再用 VirtualActor.delay 排 11 个延时针，
 * 最后一针的回调里才 spendAndNext。但 VirtualActor.delay 是「往 Actor 队列里加一个 Actor，等它 act() 时才开始计时」，
 * 而此刻 Actor.current 仍是英雄、Actor 线程在 wait()，没有人调 next()，这些 Actor 永远轮不到。
 *
 * TIMEOUT-SECONDS: 90
 */
public class Extra_AudXTurns_SenbonThrow implements Scenario {

	@Override
	public String describe() {
		return "千本投掷一次后英雄回到可行动状态，且花掉的时间在 (0, 2] 回合之间";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat rat = Arena.dummy( h, Arena.c( 14, 10 ) );
		Senbon senbon = h.give( new Senbon() );

		float before = h.now();
		h.note( "before throw: now=" + before + " rat " + Arena.describe( h, rat ) );

		h.throwAt( senbon, Arena.c( 14, 10 ) );

		float after = h.now();
		h.note( "after throw: now=" + after + " animationBusy=" + h.get( () -> Dungeon.hero.animationBusy )
				+ " ready=" + h.get( () -> Dungeon.hero.ready ) + " rat " + Arena.describe( h, rat ) );

		h.check( !h.get( () -> Dungeon.hero.animationBusy ), "投掷结束后 animationBusy 已清除" );
		h.check( after - before > 0f && after - before <= 2f,
				"一次千本投掷花掉的游戏时间在 (0, 2] 回合之间（实际 " + ( after - before ) + "）" );

		h.rest( 1 );
		h.check( h.heroAlive(), "投掷后还能正常等待一回合" );
	}
}
