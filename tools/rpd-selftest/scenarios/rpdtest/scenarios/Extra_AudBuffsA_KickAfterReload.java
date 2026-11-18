package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.KickTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * audit-buffs-a：踹飞状态拿到之后一直留在身上，直到按下按钮用掉为止；但它读档时不把自己的按钮放回去，
 * 而 checkKick 看到身上已经有这个状态就直接返回。于是读一次档，这次踹飞就再也用不出来，之后也不会重新获得。
 */
public class Extra_AudBuffsA_KickAfterReload implements Scenario {

	@Override
	public String describe() {
		return "小骑士拿到踹飞后存档读档：踹飞按钮还在";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		Arena.dummy( h, Arena.c( 12, 10 ) );
		Arena.dummy( h, Arena.c( 10, 8 ) );
		Arena.dummy( h, Arena.c( 8, 10 ) );

		h.world( "little knight, grant the kick action", () -> {
			Dungeon.hero.subClass = HeroSubClasses.LITTLE_KNIGHT;
			KickTracker.checkKick( Dungeon.hero );
		} );
		h.require( h.get( () -> Dungeon.hero.buff( KickTracker.class ) != null && ActionIndicator.action == Dungeon.hero.buff( KickTracker.class ) ),
				"周围 3 格内有 3 个敌人时获得踹飞，动作按钮是踹飞" );

		h.saveAndReload();

		String after = h.get( () -> ActionIndicator.action == null ? "null" : ActionIndicator.action.getClass().getSimpleName() );
		h.check( h.get( () -> Dungeon.hero.buff( KickTracker.class ) != null ), "读档后踹飞状态还在身上" );
		h.check( h.get( () -> Dungeon.hero.buff( KickTracker.class ) != null && ActionIndicator.action == Dungeon.hero.buff( KickTracker.class ) ),
				"读档后动作按钮仍然是踹飞（实际动作按钮：" + after + "）" );
	}
}
