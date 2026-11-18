package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PoemBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 诗 — 对名称与武器或护甲押韵的目标造成额外最终伤害
 */
public class Poem extends ItemArmorAttachable {

	{
		sndImageName = "poem";
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, PoemBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		PoemBuff buff = hero.buff(PoemBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((PoemBuff.RHYME_DAMAGE_MULTIPLIER - 1) * 100));
	}
}
