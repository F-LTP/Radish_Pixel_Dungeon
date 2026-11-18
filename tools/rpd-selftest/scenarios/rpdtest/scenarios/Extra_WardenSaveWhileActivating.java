package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * 附带发现（不在用户报告里）：典狱长「正在激活某台原型机」的那几个回合里存档，这个档还读得回来吗？
 *
 * Warden.storeInBundle 在 activating != null 时会写一个 "activating_cell" 键；
 * Warden.restoreFromBundle 看到这个键就去遍历 Dungeon.level.mobs —— 而读档阶段 Dungeon.level 是 null
 * （Dungeon.loadLevel 第一步就把它置空）。
 * 典狱长开战后的头十来个回合几乎一直在激活原型机，之后每掉一台友军又会去激活，
 * 所以「恰好在激活途中退出游戏 / 切到后台自动存档」并不罕见。
 *
 * 步骤：开战 → 原地等待，直到典狱长开始激活某台原型机 → 存档并读档 → 再等几个回合。
 * 期望：读档不崩溃；读档后仍是 1 个典狱长、11 台原型机、已封层；之后典狱长照常把原型机激活。
 *
 * 用反射读 Warden 的私有字段 activating；修复方案如果改了字段名，这个场景会以 ERROR 结束（需要跟着改）。
 */
public class Extra_WardenSaveWhileActivating implements Scenario {

	@Override
	public String describe() {
		return "附带发现：典狱长正在激活原型机时存档，读档不应崩溃，读档后战斗照常继续";
	}

	/** 典狱长此刻正在激活的原型机（没有则 null）。只能在渲染线程调用。 */
	private static WardenPrototype activating() {
		Warden w = WardenLab.warden();
		return w == null ? null : (WardenPrototype) Reflect.get( w, "activating" );
	}

	private static int wardenAllies( H h ) {
		return h.get( () -> {
			int n = 0;
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (p.isWardenAlly()) n++;
			}
			return n;
		} );
	}

	private static int wardens( H h ) {
		return h.get( () -> {
			int n = 0;
			for (Mob m : Dungeon.level.mobs) {
				if (m instanceof Warden && m.isAlive()) n++;
			}
			return n;
		} );
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		h.setHeroHP( 2000, 2000 );   // 免得在等待的回合里被激活的原型机打死
		WardenLab.startFight( h );

		int turns = 0;
		while (turns < 40 && h.get( () -> activating() == null )) {
			h.rest( 1 );
			turns++;
		}
		h.require( h.get( () -> activating() != null ), "典狱长在 40 个回合内开始激活某台原型机（Warden.activating != null）" );
		final int targetCell = h.get( () -> activating().pos );
		final int alliesBefore = wardenAllies( h );
		h.note( "after " + turns + " turn(s) the Warden is activating " + h.get( () -> WardenLab.describe( activating() ) )
				+ ", turns left=" + h.get( () -> (Object) Reflect.get( WardenLab.warden(), "activatingTurnsLeft" ) )
				+ "; prototypes already activated by the Warden: " + alliesBefore + "; " + h.get( WardenLab::field ) );

		// ---- 存档并读档（读档失败时游戏会在渲染线程抛异常 = 闪退，由测试工具记为 CRASH）
		h.saveAndReload();
		h.frames( 20 );
		h.note( "after the reload: " + h.get( WardenLab::field ) );

		h.check( h.get( () -> Dungeon.level.locked ), "激活途中存档再读档：关卡仍然封锁" );
		h.check( wardens( h ) == 1, "激活途中存档再读档：场上恰好 1 个活着的典狱长（实际 " + wardens( h ) + "）" );
		int protos = h.get( () -> WardenLab.prototypes().size() );
		h.check( protos == 11, "激活途中存档再读档：场上仍然是 11 台原型机（实际 " + protos + "）" );
		h.check( wardenAllies( h ) == alliesBefore, "读档本身没有改变已激活的原型机数量（存档前 " + alliesBefore + "，读档后 " + wardenAllies( h ) + "）" );

		// ---- 读档后战斗照常继续：典狱长在 10 个回合内又激活出一台原型机
		int waited = 0;
		while (waited < 10 && wardenAllies( h ) <= alliesBefore) {
			if (h.rest( 1 ) != H.Status.READY) break;
			waited++;
		}
		final int alliesAfter = wardenAllies( h );
		h.note( waited + " turn(s) after the reload: prototypes activated by the Warden " + alliesBefore + " -> " + alliesAfter
				+ "; the prototype that was being activated when saving (at " + WardenLab.xy( targetCell ) + ") is now: "
				+ h.get( () -> {
					for (WardenPrototype p : WardenLab.prototypes()) {
						if (p.pos == targetCell) return WardenLab.describe( p );
					}
					return "no longer on that cell";
				} ) );
		h.check( alliesAfter > alliesBefore, "读档后 10 个回合内，典狱长又激活出了一台原型机（敌方原型机 " + alliesBefore + " -> " + alliesAfter + " 台）" );
		h.check( h.heroAlive() && h.rest( 1 ) == H.Status.READY, "读档后游戏能继续（英雄存活，又过了 1 个回合）" );
	}
}
