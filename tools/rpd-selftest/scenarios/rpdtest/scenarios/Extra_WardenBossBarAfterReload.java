package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 顺带发现（不在用户的报告里）：和典狱长开战之后存档再读档，Boss 血条不见了。
 *
 * 天狗 / 粘咕 / DM-300 / 矮人国王都在 restoreFromBundle 里重新 BossHealthBar.assignBoss(this)，
 * 典狱长没有；它只在 notice() 里登记血条，而读档后 awake 已经是 true，act() 不会再调 notice()，
 * 要等到它下一次受伤（damage() 里会调 notice()）血条才回来。
 *
 * 做法：进关 → 走进战斗区开战（此时血条应已登记）→ 存档读档 → 立刻看、过 3 回合再看 → 打它一下再看（仅记录）。
 */
public class Extra_WardenBossBarAfterReload implements Scenario {

	@Override
	public String describe() {
		return "典狱长战斗中存档读档后，Boss 血条仍然应该指向典狱长";
	}

	private static boolean assigned( H h ) {
		return h.get( () -> WardenLab.warden() != null && BossHealthBar.isAssigned( WardenLab.warden() ) );
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		h.setHeroHP( 2000, 2000 );
		WandOfMagicMissile wand = h.give( new WandOfMagicMissile() );
		h.world( "fill wand charges", () -> wand.curCharges = wand.maxCharges );
		WardenLab.startFight( h );
		h.require( assigned( h ), "开战后（存档前）Boss 血条已经指向典狱长" );

		h.saveAndReload();
		h.frames( 20 );
		boolean rightAfter = assigned( h );
		h.rest( 3 );
		boolean threeTurnsLater = assigned( h );

		// 仅记录：打它一下之后血条回不回来
		String afterHit = "(not tried: the Warden is not in the hero's line of fire)";
		final Warden w = h.get( WardenLab::warden );
		if (w != null && h.get( () -> new Ballistica( Dungeon.hero.pos, w.pos, Ballistica.MAGIC_BOLT ).collisionPos == w.pos )) {
			int hp = h.get( () -> w.HP );
			// 读档后背包里的法杖是新实例，旧引用不能再用
			WandOfMagicMissile reloadedWand = h.get( () -> Dungeon.hero.belongings.getItem( WandOfMagicMissile.class ) );
			h.require( reloadedWand != null && reloadedWand != wand, "读档后背包里还有魔弹法杖（新实例）" );
			h.zap( reloadedWand, h.get( () -> w.pos ) );
			h.frames( 30 );
			afterHit = "Warden HP " + hp + " -> " + h.get( () -> w.HP ) + ", bar assigned=" + assigned( h );
		}
		h.note( "boss health bar assigned to the Warden: before saving=true, right after the reload=" + rightAfter
				+ ", 3 turns later=" + threeTurnsLater + "; after zapping the Warden once: " + afterHit );

		h.check( rightAfter, "开战后读档：Boss 血条立刻指向读档后的典狱长（实际 " + rightAfter + "）" );
		h.check( threeTurnsLater, "开战后读档再过 3 回合：Boss 血条指向典狱长（实际 " + threeTurnsLater + "）" );
	}
}
