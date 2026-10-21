package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardTargeter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Visual;

/** Keeps the single held card visible on the main HUD and lets the player play it there. */
public class HeldCardTracker extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		actPriority = HERO_PRIO + 5;
	}

	public static void sync(Hero hero) {
		if (hero == null) return;
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			HeldCardTracker tracker = Buff.affect(hero, HeldCardTracker.class);
			if (ActionIndicator.action != tracker) {
				ActionIndicator.setAction(tracker);
			}
		} else {
			clear(hero);
		}
	}

	public static void clear(Hero hero) {
		HeldCardTracker tracker = hero == null ? null : hero.buff(HeldCardTracker.class);
		if (tracker != null) tracker.detach();
		ActionIndicator.clearAction();
	}

	private Card card() {
		if (!(target instanceof Hero)) return null;
		Hero hero = (Hero) target;
		return hero.cardDeck == null ? null : hero.cardDeck.held;
	}

	@Override
	public boolean act() {
		spend(TICK);
		if (card() == null) {
			detach();
		}
		return true;
	}

	@Override
	public void detach() {
		if (ActionIndicator.action == this) {
			ActionIndicator.clearAction(this);
		}
		super.detach();
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String actionName() {
		Card c = card();
		return c == null ? Messages.get(this, "name") : Messages.get(this, "play", c.name());
	}

	@Override
	public Visual primaryVisual() {
		Card c = card();
		return new ItemSprite(c == null ? com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.CARD_BLANK : c.type.image);
	}

	@Override
	public int indicatorColor() {
		return 0x8A3BB8;
	}

	@Override
	public void doAction() {
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		Card c = card();
		if (c == null) {
			detach();
			return;
		}
		CardTargeter.play(hero, c);
	}
}
