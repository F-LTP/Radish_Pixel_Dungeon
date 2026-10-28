package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicWandBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 魔法手杖 — 法杖充能修复效率增加 20%
 */
public class MagicWand extends ItemArmorAttachable {

	{
		sndImageName = "wand-grips";
	}

	public static float getChargeEfficiency() {
		return MagicWandBuff.getChargeEfficiency();
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, MagicWandBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		MagicWandBuff buff = hero.buff(MagicWandBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((MagicWandBuff.CHARGE_EFFICIENCY_MULTIPLIER - 1) * 100));
	}
}
