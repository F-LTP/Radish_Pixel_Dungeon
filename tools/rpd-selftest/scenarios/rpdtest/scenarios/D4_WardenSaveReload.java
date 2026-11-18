package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (d) 的存读档回归保护（基线上应该 PASS）：
 * 「Boss 进门后才出现」这类改法最容易在存读档上出错——读档后 Boss 不见了、又刷出一个、或者原型机又刷一遍。
 *
 *  1. 站在门格上（还没开战）存档读档：读档前后「Boss 在不在场、原型机数量、是否封层」必须一样；
 *  2. 走进战斗区开战（封层、1 个典狱长、11 台原型机）；
 *  3. 开战后存档读档：仍然是 1 个典狱长、11 台原型机、已封层，精灵都在；
 *  4. 读档后在战斗区里再走一步、再过 2 回合：数量不变（没有因为「又进了一次战斗区」而重新开战）。
 *
 * 这个场景不判断「门外时 Boss 该不该在场」（那是 D1 的事），只判断存读档前后一致。
 */
public class D4_WardenSaveReload implements Scenario {

	@Override
	public String describe() {
		return "典狱长关卡：开战前 / 开战后存档读档，Boss 与原型机的状态保持一致，不会重复开战";
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

	private static int protos( H h ) {
		return h.get( () -> WardenLab.prototypes().size() );
	}

	private static boolean locked( H h ) {
		return h.get( () -> Dungeon.level.locked );
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		h.setHeroHP( 2000, 2000 );   // 免得在后面几个回合里被激活的原型机打死

		// ---- 1. 门格上（开战前）存档读档
		h.walkTo( WardenLab.OUTSIDE );
		h.walkTo( WardenLab.DOOR );
		h.frames( 20 );
		int w0 = wardens( h ), p0 = protos( h );
		boolean l0 = locked( h );
		h.saveAndReload();
		int w1 = wardens( h ), p1 = protos( h );
		boolean l1 = locked( h );
		h.note( "on the door cell, before the fight: wardens/prototypes/locked = " + w0 + "/" + p0 + "/" + l0 + " before saving, " + w1 + "/" + p1 + "/" + l1 + " after the reload" );
		h.check( h.heroPos() == WardenLab.DOOR, "开战前读档：英雄还在门格 (6,12) 上" );
		h.check( w1 == w0 && p1 == p0 && l1 == l0, "开战前读档：典狱长数 / 原型机数 / 是否封层 与存档前一致（存档前 " + w0 + "/" + p0 + "/" + l0 + "，读档后 " + w1 + "/" + p1 + "/" + l1 + "）" );

		// ---- 2. 走进去开战
		h.walkTo( WardenLab.INSIDE );
		for (int i = 0; i < 3 && !h.get( () -> Dungeon.level.locked && WardenLab.prototypes().size() >= 11 ); i++) {
			h.rest( 1 );
		}
		h.note( "after stepping in: " + h.get( WardenLab::field ) );
		h.check( locked( h ) && wardens( h ) == 1 && protos( h ) == 11,
				"读档后走进战斗区：正常开战（封层、1 个典狱长、11 台原型机；实际 locked=" + locked( h ) + "，" + wardens( h ) + " 个典狱长，" + protos( h ) + " 台原型机）" );
		int wardenHp = h.get( () -> WardenLab.warden() == null ? -1 : WardenLab.warden().HP );

		// ---- 3. 开战后存档读档
		h.saveAndReload();
		h.frames( 20 );
		h.note( "after the mid-fight reload: " + h.get( WardenLab::field ) );
		h.check( locked( h ), "开战后读档：关卡仍然封锁" );
		h.check( wardens( h ) == 1, "开战后读档：场上恰好 1 个活着的典狱长（实际 " + wardens( h ) + "）" );
		h.check( protos( h ) == 11, "开战后读档：场上仍然是 11 台原型机（实际 " + protos( h ) + "）" );
		final Warden w = h.get( WardenLab::warden );
		if (w != null) {
			h.check( h.get( () -> w.HP ) == wardenHp, "开战后读档：典狱长的生命不变（存档前 " + wardenHp + "，读档后 " + h.get( () -> w.HP ) + "）" );
			h.check( h.get( () -> w.sprite != null && w.sprite.parent != null ), "开战后读档：典狱长的精灵挂在场景里" );
			// Boss 血条读档后在不在，是另一个独立的问题（基线上就不在），单独放在 Extra_WardenBossBarAfterReload 里
			h.note( "boss health bar assigned to the reloaded Warden: " + h.get( () -> BossHealthBar.isAssigned( w ) ) );
		}
		h.check( h.get( () -> {
			for (WardenPrototype p : WardenLab.prototypes()) {
				if (p.sprite == null || p.sprite.parent == null) return false;
			}
			return true;
		} ), "开战后读档：所有原型机的精灵都挂在场景里" );
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "开战后读档：没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );

		// ---- 4. 读档后在战斗区里再走一步，再过 2 回合
		int step = h.get( () -> {
			for (int cell : new int[]{ WardenLab.c( 5, 11 ), WardenLab.c( 7, 11 ), WardenLab.c( 6, 10 ), WardenLab.c( 5, 10 ), WardenLab.c( 7, 10 ) }) {
				if (Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) return cell;
			}
			return -1;
		} );
		h.require( step != -1, "英雄旁边有一个空格可以走" );
		boolean moved = h.tryTap( step );
		h.rest( 2 );
		h.note( "after one more step inside the arena (moved=" + moved + ", hero at " + WardenLab.xy( h.heroPos() ) + ") and 2 turns: " + h.get( WardenLab::field ) );
		h.check( wardens( h ) == 1, "读档后在战斗区里再走动：仍然恰好 1 个典狱长（实际 " + wardens( h ) + "）" );
		h.check( protos( h ) == 11, "读档后在战斗区里再走动：仍然是 11 台原型机（实际 " + protos( h ) + "）" );
		h.check( locked( h ), "读档后在战斗区里再走动：关卡仍然封锁" );
		if (w != null) {
			h.check( h.get( () -> WardenLab.warden() ) == w, "读档后在战斗区里再走动：典狱长还是同一个（没有被换成新的）" );
		}
	}
}
