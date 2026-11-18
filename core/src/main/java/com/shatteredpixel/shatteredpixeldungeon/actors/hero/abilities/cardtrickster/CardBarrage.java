package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardFactory;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/** 紧急避险：把当前手牌临时变成传送卡。 */
public class CardBarrage extends ArmorAbility {

	{
		baseChargeUse = 25f;
	}

	/** 0 = 无折扣；否则为 80/70/60/50。 */
	private int repeatDiscountPct = 0;

	public void grantRepeat(int talentPoints) {
		if (talentPoints > 0) {
			repeatDiscountPct = 90 - 10 * talentPoints;
		}
	}

	@Override
	public float chargeUse(Hero hero) {
		float use = super.chargeUse(hero);
		if (repeatDiscountPct > 0) use *= repeatDiscountPct / 100f;
		return use;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		CardDeck deck = hero.cardDeck;
		if (deck == null || deck.held == null) {
			GLog.w(Messages.get(this, "no_card"));
			return;
		}
		//手里已经是上一次变出来的临时传送卡：再变一次会把这张传送卡当成「原卡」暂存，真正的原卡被覆盖后永久丢失
		if (deck.held.type == CardType.TELEPORT) {
			GLog.w(Messages.get(this, "no_card"));
			return;
		}
		float use = chargeUse(hero);
		if (armor.charge < use) {
			GLog.w(Messages.get(this, "no_charge"));
			return;
		}
		armor.charge -= use;
		repeatDiscountPct = 0;
		armor.updateQuickslot();

		deck.stashedCard = deck.held;
		int range = 2 + hero.pointsInTalent(Talent.MOMENTUM_TP);
		deck.held = CardFactory.teleport(range);
		HeldCardTracker.sync(hero);
		ActionIndicator.refresh();
		GLog.i(Messages.get(this, "ready", range));
	}

	@Override
	public String icon() {
		return HeroIcon.CARD_BARRAGE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.SWIFT_STEP, Talent.EMERGENCY_REPEAT, Talent.MOMENTUM_TP, Talent.HEROIC_ENERGY };
	}

	private static final String REPEAT = "repeat";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(REPEAT, repeatDiscountPct);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		repeatDiscountPct = bundle.getInt(REPEAT);
	}
}
