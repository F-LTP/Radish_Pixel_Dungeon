package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 对照组（基线上就应该 PASS）：冲击波法杖在平地上的击退是生效的。
 *  1. 射中怪：中心目标沿弹道方向被推 3 格（+0 法杖）；
 *  2. 射英雄身边的空地：站在爆心旁边的英雄自己也被向外推 1 格。
 * 用来证明 A1~A5 的 FAIL 不是「工具里击退根本不生效」造成的。
 */
public class A0_KnockbackControl implements Scenario {

	@Override
	public String describe() {
		return "对照：冲击波法杖在平地上把怪 / 英雄击退（没有深渊）";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat rat = Arena.dummy( h, Arena.c( 12, 10 ) );
		WandOfBlastWave wand = h.give( new WandOfBlastWave() );
		h.world( "fill wand charges", () -> wand.curCharges = wand.maxCharges );
		h.require( h.get( () -> wand.curCharges ) >= 2, "法杖至少有 2 发充能" );

		// 1. 推怪
		h.zap( wand, Arena.c( 12, 10 ) );
		h.frames( 30 );
		h.note( "after zap 1: " + Arena.describe( h, rat ) );
		h.check( h.get( () -> rat.pos ) == Arena.c( 15, 10 ), "怪从 (12,10) 被推到 (15,10)（实际 " + h.cellStr( h.get( () -> rat.pos ) ) + "）" );
		h.check( h.get( rat::isAlive ), "平地击退不会直接杀死 500 血的怪" );
		h.check( h.get( () -> rat.HP ) < 500, "法杖本身造成了伤害（HP " + h.get( () -> rat.HP ) + "/500）" );

		// 2. 推英雄自己：射西边相邻的空地 (9,10)，英雄在爆心东侧 → 被向东推 1 格
		h.require( h.heroPos() == Arena.c( 10, 10 ), "英雄还在 (10,10)" );
		h.zap( wand, Arena.c( 9, 10 ) );
		h.frames( 30 );
		h.check( h.heroPos() == Arena.c( 11, 10 ), "英雄被自己的冲击波从 (10,10) 推到 (11,10)（实际 " + h.cellStr( h.heroPos() ) + "）" );
	}
}
