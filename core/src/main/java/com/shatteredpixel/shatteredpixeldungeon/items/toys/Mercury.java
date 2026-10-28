package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MercuryBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 水银 — 护盾不会自然减少，每 N 回合获得 1 点护盾
 */
public class Mercury extends ItemArmorAttachable {

	{
		sndImageName = "mercury";
	}

	public static boolean preventsNaturalDecay() {
		return MercuryBuff.preventsNaturalDecay();
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, MercuryBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		MercuryBuff buff = hero.buff(MercuryBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", MercuryBuff.getShieldInterval());
	}
}
