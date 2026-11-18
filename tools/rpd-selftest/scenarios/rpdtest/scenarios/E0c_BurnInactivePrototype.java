package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * (e) 的旁证：让一台「未激活」的 DM-100 原型机死在怪物 / buff 的回合里（Actor 线程上），而不是死在玩家输入的那一下。
 * 做法：把它的生命调到 3，扔一瓶液火药水过去，让它被烧死（燃烧每回合掉血，是在 Actor 线程上结算的）。
 * 用来确认修复是修在根上（原型机的死亡本身），而不是只包住了灵爆秘卷或渲染线程那一条路。
 * 期望：不崩溃；原型机要么被干净地烧死，要么完好无损（如果修复方案是让未激活的原型机免疫伤害）。
 */
public class E0c_BurnInactivePrototype implements Scenario {

	@Override
	public String describe() {
		return "旁证：未激活的 DM-100 原型机被火烧死（死在 Actor 线程上），不应崩溃";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		WardenPrototype proto = h.spawn( new WardenPrototype( 100 ), Arena.c( 14, 10 ) );
		h.world( "leave the prototype with 3 HP", () -> proto.HP = 3 );
		h.require( h.get( proto::isInactive ), "原型机处于未激活状态" );
		PotionOfLiquidFlame flame = h.give( new PotionOfLiquidFlame() );

		h.throwAt( flame, Arena.c( 14, 10 ) );
		int turns = 0;
		boolean everBurning = false;
		while (turns < 12 && h.get( proto::isAlive )) {
			everBurning |= h.get( () -> proto.buff( Burning.class ) != null );
			h.rest( 1 );
			turns++;
		}
		h.frames( 60 );
		h.note( "liquid flame thrown at the inactive prototype (3 HP); waited " + turns + " turn(s); it was seen burning: " + everBurning
				+ "; alive=" + h.get( proto::isAlive ) + " HP=" + h.get( () -> proto.HP ) + "/" + h.get( () -> proto.HT ) );

		InactiveDeath.checkOutcome( h, proto, "火" );
		h.check( h.rest( 1 ) == H.Status.READY, "之后游戏还能继续（又过了 1 个回合）" );
	}
}
