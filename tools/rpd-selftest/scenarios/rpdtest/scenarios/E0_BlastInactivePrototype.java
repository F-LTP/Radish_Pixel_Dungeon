package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * (e) 的最小复现：测试场里只有一台「未激活」的 DM-100 原型机，英雄读一张灵爆秘卷。
 * 期望：不崩溃，游戏继续；原型机要么被干净地炸死（从关卡移除、精灵消失），要么完好无损（如果修复方案是让未激活的原型机免疫伤害）。
 */
public class E0_BlastInactivePrototype implements Scenario {

	@Override
	public String describe() {
		return "最小复现：视野里有一台未激活的 DM-100 原型机时读一张灵爆秘卷，不应崩溃";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.setHeroHP( 1000, 1000 );
		WardenPrototype proto = h.spawn( new WardenPrototype( 100 ), Arena.c( 13, 10 ) );
		h.require( h.get( proto::isInactive ), "原型机处于未激活状态" );
		h.require( h.get( () -> Dungeon.level.heroFOV[proto.pos] ), "原型机在英雄视野内" );
		ScrollOfPsionicBlast scroll = h.give( new ScrollOfPsionicBlast() );

		H.Status st = h.read( scroll );
		h.frames( 60 );

		h.check( st == H.Status.READY && h.heroAlive(), "读完灵爆后英雄存活，游戏在等下一步输入" );
		InactiveDeath.checkOutcome( h, proto, "灵爆" );
	}
}
