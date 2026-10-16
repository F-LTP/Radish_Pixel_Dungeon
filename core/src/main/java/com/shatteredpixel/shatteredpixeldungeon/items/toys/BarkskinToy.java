package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BarkskinToyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/**
 * 树肤 — 最大生命值 +40，但无法获得护盾。卸下时损失 40 点最大/当前生命值。
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
	public int detachHPLoss(Hero hero) {
		return BarkskinToyBuff.MAX_HP_BONUS;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", BarkskinToyBuff.MAX_HP_BONUS);
	}
}
