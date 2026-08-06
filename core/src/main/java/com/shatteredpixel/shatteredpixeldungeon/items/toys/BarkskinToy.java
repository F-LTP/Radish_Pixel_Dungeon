package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BarkskinToyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 树肤 — 降低最大生命值 10 点，获得常驻树肤效果
 */
public class BarkskinToy extends ItemArmorAttachable {

	{
		sndImageName = "barkskin";
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, BarkskinToyBuff.class);
		hero.updateHT(false);
	}

	@Override
	public void removeEffect(Hero hero) {
		BarkskinToyBuff buff = hero.buff(BarkskinToyBuff.class);
		if (buff != null) buff.detach();
		hero.updateHT(false);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", BarkskinToyBuff.MAX_HP_PENALTY, BarkskinToyBuff.BARKSKIN_LEVEL);
	}
}
