package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 树肤 Buff — 最大生命值 +40，但不再能获得护盾
 */
public class BarkskinToyBuff extends Buff {

	public static final int MAX_HP_BONUS = 40;

	{
		type = buffType.POSITIVE;
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.BARKSKIN;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", MAX_HP_BONUS);
	}
}
