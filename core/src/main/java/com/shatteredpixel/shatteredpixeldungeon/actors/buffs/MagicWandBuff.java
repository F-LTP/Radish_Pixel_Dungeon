package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 魔法手杖 Buff — 法杖充能修复效率增加 20%
 */
public class MagicWandBuff extends Buff {

	public static final float CHARGE_EFFICIENCY_MULTIPLIER = 1.2f;

	{
		type = buffType.POSITIVE;
	}

	public static float getChargeEfficiency() {
		if (Dungeon.hero == null) return 1f;
		MagicWandBuff buff = Dungeon.hero.buff(MagicWandBuff.class);
		return buff != null ? CHARGE_EFFICIENCY_MULTIPLIER : 1f;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((CHARGE_EFFICIENCY_MULTIPLIER - 1) * 100));
	}
}
