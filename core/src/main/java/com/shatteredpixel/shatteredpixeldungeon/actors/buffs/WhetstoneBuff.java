package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 磨刀石 Buff — 攻击伤害增加 12.5%
 */
public class WhetstoneBuff extends Buff {

	public static final float DAMAGE_MULTIPLIER = 1.125f;

	{
		type = buffType.POSITIVE;
	}

	public static float getDamageMultiplier() {
		if (Dungeon.hero == null) return 1f;
		WhetstoneBuff buff = Dungeon.hero.buff(WhetstoneBuff.class);
		return buff != null ? DAMAGE_MULTIPLIER : 1f;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((DAMAGE_MULTIPLIER - 1) * 100));
	}
}
