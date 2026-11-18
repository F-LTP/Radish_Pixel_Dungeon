package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 斗篷 Buff — 每隔一段时间获得隐身效果
 */
public class CloakBuff extends Buff {

	public static final int INVISIBILITY_INTERVAL = 10;
	public static final int INVISIBILITY_DURATION = 5;

	{
		type = buffType.POSITIVE;
	}

	private int turnCounter = 0;

	@Override
	public boolean act() {
		turnCounter++;
		if (turnCounter >= INVISIBILITY_INTERVAL) {
			turnCounter = 0;
			if (target instanceof Hero) {
				Buff.affect(target, Invisibility.class, INVISIBILITY_DURATION);
			}
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", INVISIBILITY_INTERVAL, INVISIBILITY_DURATION);
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
