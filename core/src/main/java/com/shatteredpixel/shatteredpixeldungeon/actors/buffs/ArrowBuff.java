package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 箭矢 Buff — 直接击杀低于 10 血量的怪物
 */
public class ArrowBuff extends Buff {

	public static final int EXECUTE_THRESHOLD = 10;

	{
		type = buffType.POSITIVE;
	}

	/**
	 * 在 Hero.attack() 造成伤害后调用，尝试斩杀
	 */
	public static boolean tryExecute(Char enemy) {
		if (enemy instanceof Hero) return false;
		if (Dungeon.hero == null) return false;
		ArrowBuff buff = Dungeon.hero.buff(ArrowBuff.class);
		return buff != null && enemy.HP <= EXECUTE_THRESHOLD;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", EXECUTE_THRESHOLD);
	}
}
