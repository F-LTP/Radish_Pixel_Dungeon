/*
 * 装备融合：融合机会与解锁状态。
 *
 * 原先融合机会来自「红包」挑战的开包奖励；现在融合作为独立系统存在，
 * 机会改为每下一层 +1（开局也给 1 次，方便尝试）。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class FusionCharges extends Buff {

	private boolean unlocked = false;
	private int charges = 0;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	public static FusionCharges get(Char ch) {
		if (ch == null) return null;
		return ch.buff(FusionCharges.class);
	}

	public static int fusionCharges(Char ch) {
		FusionCharges b = get(ch);
		return b == null ? 0 : b.charges;
	}

	public static boolean hasFusion(Char ch) {
		FusionCharges b = get(ch);
		return b != null && b.unlocked;
	}

	public static void grantFusion(Char ch) {
		if (ch == null) return;
		FusionCharges b = ch.buff(FusionCharges.class);
		if (b == null) b = Buff.affect(ch, FusionCharges.class);
		b.unlocked = true;
		b.charges++;
	}

	public static void grantCharge(Char ch, int amount) {
		if (ch == null || amount <= 0) return;
		FusionCharges b = ch.buff(FusionCharges.class);
		if (b == null) b = Buff.affect(ch, FusionCharges.class);
		b.unlocked = true;
		b.charges += amount;
	}

	public static boolean consumeFusionCharge() {
		Char hero = Dungeon.hero;
		if (hero == null) return false;
		FusionCharges b = hero.buff(FusionCharges.class);
		if (b == null || b.charges <= 0) return false;
		b.charges--;
		return true;
	}

	private static final String UNLOCKED = "unlocked";
	private static final String CHARGES = "charges";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(UNLOCKED, unlocked);
		bundle.put(CHARGES, charges);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		unlocked = bundle.getBoolean(UNLOCKED);
		charges = bundle.getInt(CHARGES);
	}
}
