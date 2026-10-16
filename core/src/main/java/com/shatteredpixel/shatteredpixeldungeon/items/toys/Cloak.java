package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CloakBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 斗篷 — 每隔一段时间获得隐身效果
 */
public class Cloak extends ItemArmorAttachable {

	{
		sndImageName = "cloak";
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, CloakBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		CloakBuff buff = hero.buff(CloakBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", CloakBuff.INVISIBILITY_INTERVAL, CloakBuff.INVISIBILITY_DURATION);
	}
}
