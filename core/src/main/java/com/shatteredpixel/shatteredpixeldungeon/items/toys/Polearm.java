package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PolearmBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 长杆兵器 — 攻击距离 +1，攻击延迟增加 33%
 */
public class Polearm extends ItemArmorAttachable {

	{
		sndImageName = "polearm";
	}

	public static int getReachBonus() {
		return PolearmBuff.getReachBonus();
	}

	public static float getAttackDelayMultiplier() {
		return PolearmBuff.getAttackDelayMultiplier();
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, PolearmBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		PolearmBuff buff = hero.buff(PolearmBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", PolearmBuff.REACH_BONUS, (int)((PolearmBuff.ATTACK_DELAY_MULTIPLIER - 1) * 100));
	}
}
