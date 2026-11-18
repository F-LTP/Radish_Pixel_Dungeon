package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * audit-buffs-a：角斗士的连击按钮在游戏里的出现条件是「连击数 > 0 且还有战技次数」（Combo.hit），
 * 读档时应当按同一个条件恢复：有次数就有按钮，次数用完就没有按钮。
 */
public class Extra_AudBuffsA_ComboAfterReload implements Scenario {

	@Override
	public String describe() {
		return "角斗士连击按钮读档后的有无，与读档前一致（有战技次数才有按钮）";
	}

	private static String action() {
		return ActionIndicator.action == null ? "null" : ActionIndicator.action.getClass().getSimpleName();
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.world( "gladiator with 1 combo (1 move available)", () -> {
			Dungeon.hero.subClass = HeroSubClasses.GLADIATOR;
			Combo c = Buff.affect( Dungeon.hero, Combo.class );
			c.hit();
			Reflect.set( c, "comboTime", 500f );
		} );
		h.require( h.get( () -> ActionIndicator.action instanceof Combo ), "1 连击、1 次战技次数时，存档前有连击按钮" );

		h.saveAndReload();

		h.check( h.get( () -> Dungeon.hero.buff( Combo.class ) != null && ActionIndicator.action == Dungeon.hero.buff( Combo.class ) ),
				"1 连击、还有 1 次战技次数：读档后连击按钮还在（实际动作按钮：" + h.get( Extra_AudBuffsA_ComboAfterReload::action ) + "）" );

		h.world( "4 combo, no moves left, button hidden", () -> {
			Combo c = Dungeon.hero.buff( Combo.class );
			Reflect.set( c, "count", 4 );
			c.couldUseTime = 0;
			ActionIndicator.clearAction( c );
		} );
		h.require( h.get( () -> ActionIndicator.action == null ), "4 连击、战技次数用完时，存档前没有连击按钮" );

		h.saveAndReload();

		h.check( h.get( () -> !(ActionIndicator.action instanceof Combo) ),
				"4 连击、战技次数已用完：读档后不会凭空多出连击按钮（实际动作按钮：" + h.get( Extra_AudBuffsA_ComboAfterReload::action ) + "）" );
	}
}
