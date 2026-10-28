package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.IronHeartBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 铁心 — 增加 50% 伤害，攻击速度下降 33%
 */
public class IronHeart extends ItemArmorAttachable {

	{
		sndImageName = "iron-heart";
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, IronHeartBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		IronHeartBuff buff = hero.buff(IronHeartBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((IronHeartBuff.DAMAGE_MULTIPLIER - 1) * 100), (int)((1 - IronHeartBuff.ATTACK_SPEED_PENALTY) * 100));
	}
}
