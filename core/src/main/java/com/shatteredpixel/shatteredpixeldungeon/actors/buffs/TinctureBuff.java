package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 酊剂 Buff — 减少 25% debuff 的持续回合
 */
public class TinctureBuff extends Buff {

	public static final float DEBUFF_REDUCTION = 0.25f;

	{
		type = buffType.POSITIVE;
	}

	/**
	 * 在 Buff.spend() 时调用，返回修正后的持续时间
	 */
	public static float modifyDebuffDuration(float duration) {
		if (Dungeon.hero == null) return duration;
		TinctureBuff buff = Dungeon.hero.buff(TinctureBuff.class);
		if (buff == null) return duration;
		return duration * (1 - DEBUFF_REDUCTION);
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)(DEBUFF_REDUCTION * 100));
	}
}
