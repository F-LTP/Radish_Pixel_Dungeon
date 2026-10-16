package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldToyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 坚盾 — 防御增加 3 点
 */
public class ShieldToy extends ItemArmorAttachable {

	{
		sndImageName = "big-shield";
	}

	public static int getDRBonus() {
		return ShieldToyBuff.getDRBonus();
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, ShieldToyBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		ShieldToyBuff buff = hero.buff(ShieldToyBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", ShieldToyBuff.DR_BONUS);
	}
}
