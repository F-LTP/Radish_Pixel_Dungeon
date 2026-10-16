package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 沉重的鞋 Buff — 伤害提升 33%，移动速度下降为 0.8
 */
public class ClumsyShoesBuff extends Buff {

	public static final float DAMAGE_MULTIPLIER = 1.33f;
	public static final float SPEED_MULTIPLIER = 0.8f;

	{
		type = buffType.POSITIVE;
	}

	public static float getDamageMultiplier() {
		if (Dungeon.hero == null) return 1f;
		ClumsyShoesBuff buff = Dungeon.hero.buff(ClumsyShoesBuff.class);
		return buff != null ? DAMAGE_MULTIPLIER : 1f;
	}

	public static float getSpeedMultiplier() {
		if (Dungeon.hero == null) return 1f;
		ClumsyShoesBuff buff = Dungeon.hero.buff(ClumsyShoesBuff.class);
		return buff != null ? SPEED_MULTIPLIER : 1f;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((DAMAGE_MULTIPLIER - 1) * 100), (int)(SPEED_MULTIPLIER * 100));
	}
}
