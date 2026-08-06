package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ClumsyShoesBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/**
 * 沉重的鞋 — 伤害提升 33%，移动速度下降为 0.8
 */
public class ClumsyShoes extends ItemArmorAttachable {

	{
		sndImageName = "clumsy-shoes";
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, ClumsyShoesBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		ClumsyShoesBuff buff = hero.buff(ClumsyShoesBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((ClumsyShoesBuff.DAMAGE_MULTIPLIER - 1) * 100), (int)(ClumsyShoesBuff.SPEED_MULTIPLIER * 100));
	}
}
