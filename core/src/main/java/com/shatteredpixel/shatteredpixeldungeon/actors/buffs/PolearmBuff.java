package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 长杆兵器 Buff — 攻击距离 +1，攻击延迟增加 33%
 */
public class PolearmBuff extends Buff {

	public static final int REACH_BONUS = 1;
	public static final float ATTACK_DELAY_MULTIPLIER = 1.33f;

	{
		type = buffType.POSITIVE;
	}

	public static int getReachBonus() {
		if (Dungeon.hero == null) return 0;
		PolearmBuff buff = Dungeon.hero.buff(PolearmBuff.class);
		return buff != null ? REACH_BONUS : 0;
	}

	public static float getAttackDelayMultiplier() {
		if (Dungeon.hero == null) return 1f;
		PolearmBuff buff = Dungeon.hero.buff(PolearmBuff.class);
		return buff != null ? ATTACK_DELAY_MULTIPLIER : 1f;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", REACH_BONUS, (int)((ATTACK_DELAY_MULTIPLIER - 1) * 100));
	}
}
