package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 坚盾 Buff — 防御增加 3 点
 */
public class ShieldToyBuff extends Buff {

	public static final int DR_BONUS = 3;

	{
		type = buffType.POSITIVE;
	}

	public static int getDRBonus() {
		if (Dungeon.hero == null) return 0;
		ShieldToyBuff buff = Dungeon.hero.buff(ShieldToyBuff.class);
		return buff != null ? DR_BONUS : 0;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", DR_BONUS);
	}
}
