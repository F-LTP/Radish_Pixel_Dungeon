package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户报告 (f) 的存读档版本：DM-175 的护盾被打掉之后存档再读档（英雄离开这一层再回来走的也是同一条读档路径），
 * 它仍然不应该重新获得护盾。
 *
 * 用来防这一类回归：修了 (f)，但「护盾已经发过了」这个状态没有写进存档，读档后又发一次。
 * 基线上这个场景和 F1 一样失败（护盾在存档之前就已经长回来了）。
 */
public class F3_Dm175ShieldAfterReload implements Scenario {

	@Override
	public String describe() {
		return "DM-175 的护盾被打掉后存档读档，读档后也不应重新获得护盾";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.setHeroHP( 5000, 5000 );
		WardenPrototype dm = new WardenPrototype( 175 );
		dm.HP = dm.HT = 5000;
		h.spawn( dm, Arena.c( 11, 10 ) );
		h.world( "activate the DM-175 as the Warden's ally (hostile)", dm::activateByWarden );
		h.rest( 2 );

		int shield0 = h.get( dm::shielding );
		h.note( "2 turns after activation: shield=" + shield0 );
		h.check( shield0 > 0, "激活后的 DM-175 带有护盾（实际 " + shield0 + "）" );

		// 把护盾打掉
		int attacks = 0;
		int prev = shield0;
		boolean broken = shield0 == 0;
		boolean grewBack = false;
		while (attacks < 250 && !broken) {
			h.attack( h.get( () -> dm.pos ) );
			attacks++;
			int cur = h.get( dm::shielding );
			if (cur > prev) grewBack = true;
			broken = Probe.shieldBreaks( dm ) > 0 || cur == 0 || grewBack;
			prev = cur;
		}
		h.require( broken, "英雄把 DM-175 的护盾打掉了（打了 " + attacks + " 下）" );
		h.rest( 1 );
		int beforeSave = h.get( dm::shielding );
		h.check( beforeSave == 0, "存档之前：护盾被打掉 1 回合后仍然是 0（实际 " + beforeSave + "）" );

		// 存档再读档
		h.saveAndReload();
		WardenPrototype reloaded = h.get( () -> {
			for (Mob m : Dungeon.level.mobs) {
				if (m instanceof WardenPrototype) return (WardenPrototype) m;
			}
			return null;
		} );
		h.require( reloaded != null && reloaded != dm, "读档后关卡里有一台原型机（新实例）" );
		h.require( h.get( () -> reloaded.model == 175 && !reloaded.isInactive() && reloaded.isWardenAlly() ),
				"读档后它仍然是一台已被典狱长激活的 DM-175" );

		List<Integer> samples = new ArrayList<>();
		samples.add( h.get( reloaded::shielding ) );
		for (int i = 0; i < 10; i++) {
			h.rest( 1 );
			samples.add( h.get( reloaded::shielding ) );
		}
		h.note( "shield before saving: " + beforeSave + "; right after the reload and after each of the next 10 turns: " + samples );
		boolean grew = false;
		for (int i = 1; i < samples.size(); i++) {
			if (samples.get( i ) > samples.get( i - 1 )) grew = true;
		}
		h.check( samples.get( 0 ) == beforeSave, "读档没有改变护盾值（存档前 " + beforeSave + "，读档后 " + samples.get( 0 ) + "）" );
		h.check( !grew, "读档后的 10 回合里护盾没有涨回来（实际观察到的护盾值：" + samples + "）" );
		h.check( h.get( reloaded::isAlive ) && h.heroAlive(), "DM-175 和英雄都还活着（场景没有因为别的原因失效）" );
	}
}
