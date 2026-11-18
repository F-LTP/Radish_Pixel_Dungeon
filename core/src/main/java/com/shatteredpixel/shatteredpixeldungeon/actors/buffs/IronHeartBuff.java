package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 铁心 Buff — 增加 50% 伤害，攻击速度下降 33%
 */
public class IronHeartBuff extends Buff {

	public static final float DAMAGE_MULTIPLIER = 1.5f;
	public static final float ATTACK_SPEED_PENALTY = 0.67f;

	{
		type = buffType.POSITIVE;
	}

	/**
	 * 在 Hero.damageRoll() 中调用，获取伤害倍率
	 */
	public static float getDamageMultiplier() {
		if (Dungeon.hero == null) return 1f;
		IronHeartBuff buff = Dungeon.hero.buff(IronHeartBuff.class);
		return buff != null ? DAMAGE_MULTIPLIER : 1f;
	}

	/**
	 * 在 Hero.attackDelay() 中调用，获取攻速倍率
	 */
	public static float getAttackSpeedMultiplier() {
		if (Dungeon.hero == null) return 1f;
		IronHeartBuff buff = Dungeon.hero.buff(IronHeartBuff.class);
		return buff != null ? ATTACK_SPEED_PENALTY : 1f;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE; // TODO
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((DAMAGE_MULTIPLIER - 1) * 100), (int)((1 - ATTACK_SPEED_PENALTY) * 100));
	}
}
