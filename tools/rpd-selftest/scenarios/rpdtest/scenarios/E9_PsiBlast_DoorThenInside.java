package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (e) 变体，把 (d) 的「站在门口就能打 Boss」也串进来：
 *  第 1 张灵爆在门格上读（还没开战，场上只有典狱长，没有任何原型机）；
 *  然后走进去开战，按报告摆场（另外 7 台未激活的 DM-100 还躺在场上），读第 2 张。
 * 期望：不崩溃。
 */
public class E9_PsiBlast_DoorThenInside implements Scenario {

	@Override
	public String describe() {
		return "典狱长战，第 1 张灵爆在门口读、进门开战后读第 2 张：不应崩溃";
	}

	@Override
	public void run( H h ) {
		PsiBlastLab.Opt o = new PsiBlastLab.Opt();
		o.removeInactive = false;

		ScrollOfPsionicBlast scroll = PsiBlastLab.prepare( h );
		h.walkTo( WardenLab.OUTSIDE );
		h.walkTo( WardenLab.DOOR );
		h.require( h.heroPos() == WardenLab.DOOR, "英雄站在门格 (6,12) 上" );

		H.Status st = PsiBlastLab.read( h, scroll, 1, o );
		if (st == H.Status.READY) {
			h.world( "cure blindness before walking in", () -> Buff.detach( Dungeon.hero, Blindness.class ) );
			WardenLab.startFight( h );
			PsiBlastLab.arrange( h, o );
			PsiBlastLab.read( h, scroll, 2, o );
		}
		PsiBlastLab.finish( h, o );
	}
}
