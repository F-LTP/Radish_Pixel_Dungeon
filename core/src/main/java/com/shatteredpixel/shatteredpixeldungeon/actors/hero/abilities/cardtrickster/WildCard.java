package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardEffects;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 点骨成金：一段时间内用卡牌击杀敌人会掉落额外战利品。 */
public class WildCard extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		float use = chargeUse(hero);
		if (armor.charge < use) {
			GLog.w(Messages.get(this, "no_charge"));
			return;
		}
		armor.charge -= use;
		armor.updateQuickslot();

		int dur = 5 + hero.pointsInTalent(Talent.LINGERING);
		Buff.affect(hero, CardEffects.BoneToGold.class, dur);
		GLog.i(Messages.get(this, "active", dur));
	}

	@Override
	public String icon() {
		return HeroIcon.WILD_CARD;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.HEAVY_REWARD, Talent.LINGERING, Talent.EXTRA_BLESSING, Talent.HEROIC_ENERGY };
	}
}
