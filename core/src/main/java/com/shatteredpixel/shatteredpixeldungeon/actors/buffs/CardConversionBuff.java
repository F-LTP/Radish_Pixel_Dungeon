package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** Food-to-card conversion charges granted by playing blank cards with Advanced Food. */
public class CardConversionBuff extends Buff {

	public static final int MAX_CHARGES = 3;

	public int charges = 1;

	{
		type = buffType.POSITIVE;
		announced = true;
		actPriority = HERO_PRIO + 4;
	}

	public static void gain(Hero hero, int amount) {
		if (hero == null || !hero.hasTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ADVANCED_FOOD)) {
			return;
		}
		CardConversionBuff buff = Buff.affect(hero, CardConversionBuff.class);
		buff.charges = Math.min(MAX_CHARGES, buff.charges + amount);
	}

	public boolean consume() {
		if (charges <= 0) return false;
		charges--;
		if (charges <= 0) detach();
		return true;
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.UPGRADE;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", charges);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(charges);
	}

	private static final String CHARGES = "charges";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHARGES, charges);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		charges = bundle.getInt(CHARGES);
		if (Dungeon.hero != null && Dungeon.hero.cardDeck == null && charges <= 0) detach();
	}
}
