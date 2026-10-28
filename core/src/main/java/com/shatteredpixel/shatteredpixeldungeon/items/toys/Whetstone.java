package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WhetstoneBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 磨刀石 — 攻击伤害增加 12.5%
 */
public class Whetstone extends ItemArmorAttachable {

	{
		sndImageName = "whetstone";
	}

	public static float getDamageMultiplier() {
		return WhetstoneBuff.getDamageMultiplier();
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, WhetstoneBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		WhetstoneBuff buff = hero.buff(WhetstoneBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((WhetstoneBuff.DAMAGE_MULTIPLIER - 1) * 100));
	}
}
