package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Senbon;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 千本：对着敌人、对着空地各投一次，每次投完英雄都应回到可行动状态，花掉一次投掷的时间，并扣一次耐久。
 *
 * TIMEOUT-SECONDS: 120
 */
public class Extra_AudWeaponsEnch_SenbonThrow implements Scenario {

	@Override
	public String describe() {
		return "千本对敌人、对空地各投一次：英雄回到可行动状态，每次花 (0, 2] 回合，耐久各扣一次";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Rat rat = Arena.dummy( h, Arena.c( 14, 10 ) );
		Senbon senbon = h.give( new Senbon() );

		float t0 = h.now();
		float d0 = h.get( senbon::durabilityLeft );
		h.throwAt( senbon, Arena.c( 14, 10 ) );
		h.frames( 30 );
		h.waitReady();
		float t1 = h.now();
		float d1 = h.get( senbon::durabilityLeft );
		h.note( "throw at rat: time " + t0 + " -> " + t1 + ", durability " + d0 + " -> " + d1
				+ ", rat HP " + h.get( () -> rat.HP ) + "/" + h.get( () -> rat.HT )
				+ ", animationBusy=" + h.get( () -> Dungeon.hero.animationBusy ) );
		h.check( !h.get( () -> Dungeon.hero.animationBusy ), "对敌人投完后 animationBusy 已清除" );
		h.check( t1 - t0 > 0f && t1 - t0 <= 2f, "对敌人投一次花掉的游戏时间在 (0, 2] 回合之间（实际 " + ( t1 - t0 ) + "）" );
		h.check( d1 < d0, "对敌人投一次扣了耐久（" + d0 + " -> " + d1 + "）" );

		h.throwAt( senbon, Arena.c( 10, 5 ) );
		h.frames( 30 );
		h.waitReady();
		float t2 = h.now();
		float d2 = h.get( senbon::durabilityLeft );
		h.note( "throw at empty floor: time " + t1 + " -> " + t2 + ", durability " + d1 + " -> " + d2 );
		h.check( t2 - t1 > 0f && t2 - t1 <= 2f, "对空地投一次花掉的游戏时间在 (0, 2] 回合之间（实际 " + ( t2 - t1 ) + "）" );
		h.check( d2 < d1, "对空地投一次也扣了耐久（" + d1 + " -> " + d2 + "）" );

		h.rest( 1 );
		h.check( h.heroAlive(), "投掷后还能正常等待一回合" );
	}
}
