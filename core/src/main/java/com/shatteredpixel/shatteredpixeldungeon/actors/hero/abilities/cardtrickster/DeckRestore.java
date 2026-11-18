package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardEffects;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 临时管理：把手牌放入临时存储槽。 */
public class DeckRestore extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		CardDeck deck = hero.cardDeck;
		if (deck == null || deck.held == null) {
			GLog.w(Messages.get(this, "no_card"));
			return;
		}
		int max = 1 + hero.pointsInTalent(Talent.BAG_EXPANSION);
		if (deck.stored.size() >= max) {
			GLog.w(Messages.get(this, "storage_full", max));
			return;
		}
		float use = chargeUse(hero);
		if (armor.charge < use) {
			GLog.w(Messages.get(this, "no_charge"));
			return;
		}
		armor.charge -= use;
		armor.updateQuickslot();

		deck.stored.add(deck.held);
		deck.held = null;
		HeldCardTracker.clear(hero);

		int dur = 20 + 5 * hero.pointsInTalent(Talent.LONG_STORAGE);
		Buff.affect(hero, CardEffects.CardStorage.class, dur);
		ActionIndicator.refresh();
		GLog.i(Messages.get(this, "stored", dur));
	}

	@Override
	public String icon() {
		return HeroIcon.DECK_RESTORE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.LONG_STORAGE, Talent.BAG_EXPANSION, Talent.EFFECTIVE_USE, Talent.HEROIC_ENERGY };
	}
}
