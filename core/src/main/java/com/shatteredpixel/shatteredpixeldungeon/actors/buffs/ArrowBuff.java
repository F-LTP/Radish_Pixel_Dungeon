package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
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
	 * 在近战命中后调用，尝试斩杀：攻击者持有箭矢增益且目标血量不高于阈值。
	 * 攻击者可能是玩家，也可能是持有该增益的怪物。
	 */
	public static boolean tryExecute(Char attacker, Char enemy) {
		if (enemy instanceof Hero) return false;
		if (attacker == null) return false;
		ArrowBuff buff = attacker.buff(ArrowBuff.class);
		return buff != null && enemy.HP <= EXECUTE_THRESHOLD;
	}

	/** 命中后由攻击命中事件驱动：持箭矢增益者斩杀血量不高于阈值的目标。 */
	@SubscribeEvent(event = AttackHitEvent.class, priority = -300)
	public static void onAttackHit(AttackHitEvent event) {
		Char attacker = event.getAttacker();
		Char defender = event.getDefender();
		if (attacker == null || defender == null) return;
		if (defender.isAlive() && tryExecute(attacker, defender)) {
			defender.HP = 0;
			defender.die(attacker);
		}
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
