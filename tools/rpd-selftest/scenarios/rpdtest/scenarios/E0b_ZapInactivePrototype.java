package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * (e) 的旁证：不用灵爆，用魔弹法杖打一台「未激活」的 DM-100 原型机（22 血），最多 30 发。
 * 用来区分「灵爆秘卷本身有问题」和「未激活的原型机一死就出事」。
 * 期望：不崩溃；原型机要么被干净地打死，要么完好无损（如果修复方案是让未激活的原型机免疫伤害）。
 */
public class E0b_ZapInactivePrototype implements Scenario {

	@Override
	public String describe() {
		return "旁证：用魔弹法杖打一台未激活的 DM-100 原型机直到打死（最多 30 发），不应崩溃";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		WardenPrototype proto = h.spawn( new WardenPrototype( 100 ), Arena.c( 13, 10 ) );
		h.require( h.get( proto::isInactive ), "原型机处于未激活状态" );
		WandOfMagicMissile wand = h.give( new WandOfMagicMissile() );
		h.world( "give the wand 40 charges", () -> wand.curCharges = 40 );

		int zaps = 0;
		while (zaps < 30 && h.get( proto::isAlive )) {
			h.zap( wand, Arena.c( 13, 10 ) );
			zaps++;
		}
		h.frames( 60 );
		h.note( "zapped " + zaps + " time(s); prototype alive=" + h.get( proto::isAlive ) + " HP=" + h.get( () -> proto.HP ) + "/" + h.get( () -> proto.HT ) );

		InactiveDeath.checkOutcome( h, proto, "魔弹法杖" );
		h.check( h.rest( 1 ) == H.Status.READY, "之后游戏还能继续（又过了 1 个回合）" );
	}
}
