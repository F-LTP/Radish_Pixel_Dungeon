package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 用户报告 (d)：「只要人不进门就可以打 boss，还不启动 boss 战；建议和天狗一样进门后才出现 boss 和各个原型机」。
 *
 * 期望行为：英雄还没踏进战斗区（站在入口房 / 门格上）时，典狱长根本不在场，自然也打不到。
 * 做法：进关 → 走到门格 (6,12) → 用魔弹法杖射门里面 Boss 的位置。
 */
public class D1_WardenDoorSnipe implements Scenario {

	@Override
	public String describe() {
		return "典狱长关卡：英雄站在门格 / 门外时 Boss 不在场，从门口打不到 Boss";
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		WandOfMagicMissile wand = h.give( new WandOfMagicMissile() );
		h.world( "fill wand charges", () -> wand.curCharges = wand.maxCharges );

		// ---- 1. 刚进关，站在入口
		h.note( "at the entrance: " + h.get( WardenLab::field ) );
		h.check( h.get( () -> WardenLab.warden() == null ), "英雄还在入口房间时，典狱长不在场（关卡里没有 Warden）" );
		h.check( h.get( () -> WardenLab.prototypes().isEmpty() ), "英雄还在入口房间时，没有原型机在场" );

		// ---- 2. 走到门格上
		h.walkTo( WardenLab.OUTSIDE );
		h.walkTo( WardenLab.DOOR );
		h.frames( 20 );
		h.require( h.heroPos() == WardenLab.DOOR, "英雄站在门格 (6,12) 上" );
		final Warden w = h.get( WardenLab::warden );
		h.note( "on the door cell: " + h.get( WardenLab::field ) );
		h.check( w == null, "英雄站在门格上时，典狱长不在场" );
		h.check( !h.get( () -> Dungeon.level.locked ), "英雄站在门格上时还没有封层（没进战斗区）" );

		// ---- 3. 从门格往里射
		int target = w != null ? h.get( () -> w.pos ) : WardenLab.BOSS_HOME;
		int hpBefore = w != null ? h.get( () -> w.HP ) : -1;
		h.zap( wand, target );
		h.frames( 30 );
		if (w != null) {
			int hpAfter = h.get( () -> w.HP );
			boolean locked = h.get( () -> Dungeon.level.locked );
			h.note( "door snipe with WandOfMagicMissile from (6,12) at " + WardenLab.xy( target ) + ": Warden HP " + hpBefore + " -> " + hpAfter
					+ "; level.locked=" + locked + "; warden: " + h.get( () -> WardenLab.describe( w ) ) );
			h.check( hpAfter == hpBefore, "从门格用法杖射典狱长：打不到（Boss 生命不变；实际 " + hpBefore + " -> " + hpAfter + "）" );
			h.check( hpAfter == hpBefore || locked, "不允许「打到了 Boss 却没有开战」：Boss 掉血时关卡必须已封锁（实际 locked=" + locked + "）" );

			// 再等 3 回合：Boss 被打了之后有没有反应
			h.rest( 3 );
			h.note( "3 turns after the snipe: " + h.get( () -> WardenLab.describe( w ) ) + "; level.locked=" + h.get( () -> Dungeon.level.locked )
					+ "; prototypes=" + h.get( () -> WardenLab.prototypes().size() ) );
		} else {
			h.note( "door snipe: no Warden on the level, the bolt hit nothing" );
			h.check( h.get( () -> WardenLab.warden() == null ), "从门格往里射之后，场上仍然没有 Boss（没有被远程攻击引出来）" );
		}
	}
}
