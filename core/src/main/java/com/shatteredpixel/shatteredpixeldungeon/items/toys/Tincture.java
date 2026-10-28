package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TinctureBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 酊剂 — 减少 25% debuff 的持续回合
 */
public class Tincture extends ItemArmorAttachable {

	{
		sndImageName = "tincture";
	}

	public static float modifyDebuffDuration(float duration) {
		return TinctureBuff.modifyDebuffDuration(duration);
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, TinctureBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		TinctureBuff buff = hero.buff(TinctureBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)(TinctureBuff.DEBUFF_REDUCTION * 100));
	}
}
