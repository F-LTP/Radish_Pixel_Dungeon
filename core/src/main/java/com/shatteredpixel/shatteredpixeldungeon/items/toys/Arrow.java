package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArrowBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;

/**
 * 箭矢 — 直接击杀低于 10 血量的怪物
 */
public class Arrow extends ItemArmorAttachable {

	{
		sndImageName = "arrow";
	}

	/**
	 * 在 Hero.attack() 造成伤害后调用，尝试斩杀
	 */
	public static boolean tryExecute(Char enemy) {
		return ArrowBuff.tryExecute(enemy);
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, ArrowBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		ArrowBuff buff = hero.buff(ArrowBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", ArrowBuff.EXECUTE_THRESHOLD);
	}
}
