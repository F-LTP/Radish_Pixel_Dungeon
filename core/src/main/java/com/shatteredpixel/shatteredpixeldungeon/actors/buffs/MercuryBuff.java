package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 水银 Buff — 护盾不会自然减少，每 N 回合获得 1 点护盾
 */
public class MercuryBuff extends Buff {

	public static final int SHIELD_INTERVAL = 5;

	{
		type = buffType.POSITIVE;
	}

	private int turnCounter = 0;

	public static int getShieldInterval() {
		return SHIELD_INTERVAL;
	}

	@Override
	public boolean act() {
		turnCounter++;
		if (turnCounter >= getShieldInterval()) {
			turnCounter = 0;
			if (target instanceof Hero) {
				boolean hasShield = false;
				for (ShieldBuff sb : target.buffs(ShieldBuff.class)) {
					sb.incShield(1);
					hasShield = true;
				}
				if (!hasShield) {
					Buff.affect(target, Barrier.class).incShield(1);
				}
			}
		}
		spend(TICK);
		return true;
	}

	/**
	 * 在 ShieldBuff.act() 中调用，检查是否阻止护盾衰减
	 */
	public static boolean preventsNaturalDecay() {
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero == null) return false;
		return com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.buff(MercuryBuff.class) != null;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", getShieldInterval());
	}

	private static final String TURN_COUNTER = "turn_counter";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURN_COUNTER, turnCounter);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnCounter = bundle.getInt(TURN_COUNTER);
	}
}
