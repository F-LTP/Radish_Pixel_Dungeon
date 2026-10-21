package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** Placeholder armor skill; safe to select and activate. */
public class WildCard extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		GLog.i(Messages.get(this, "placeholder"));
	}

	@Override
	public String icon() {
		return HeroIcon.WILD_CARD;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.PLACEHOLDER_T4_1, Talent.PLACEHOLDER_T4_2 };
	}
}
