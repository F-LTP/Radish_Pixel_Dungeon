package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardEffects;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 戏卡者的抽卡驱动器：没有手牌且冷却结束时自动抽卡（不消耗回合）。
 * 常驻 buff，由 CardDeck.getOrCreate 挂载。
 */
public class CardAutoDraw extends Buff {

	{
		type = buffType.NEUTRAL;
		actPriority = HERO_PRIO + 6;
	}

	/** 空转大师每 50 回合空转一次。 */
	private int spinClock = 0;
	private static final String SPIN_CLOCK = "spin_clock";

	@Override
	public boolean act() {
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			if (hero.heroClass == HeroClasses.CARDTRICKSTER && hero.isAlive()) {
				if (hero.ready) {
					CardDrawer.autoDraw(hero);
				}
				if (hero.subClass == HeroSubClasses.SPIN_MASTER && hero.cardDeck != null) {
					spinClock++;
					if (spinClock >= 50) {
						spinClock = 0;
						int amount = hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 2 ? 2 : 1;
						CardEffects.fancySpin(hero, hero.cardDeck.spinBlank(amount));
					}
				}
			}
		}
		spend(TICK);
		return true;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPIN_CLOCK, spinClock);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spinClock = bundle.getInt(SPIN_CLOCK);
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}
}
