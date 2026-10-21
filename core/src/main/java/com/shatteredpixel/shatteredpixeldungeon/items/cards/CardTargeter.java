package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

public final class CardTargeter {

	private CardTargeter() {
	}

	public static void play(Hero hero, Card card) {
		if (hero == null || card == null || hero.cardDeck == null || hero.cardDeck.held != card) {
			return;
		}
		if (!hero.isAlive() || !hero.ready) return;

		switch (card.type.target) {
			case NONE:
			case SELF:
				CardEffects.play(hero, card, hero.pos);
				break;
			case ENEMY:
			case ANY:
				if (!hasValidTarget(hero, card)) {
					offerSafeReturn(hero, card);
				} else {
					GameScene.selectCell(new CellSelector.Listener() {
						@Override
						public void onSelect(Integer cell) {
							if (cell == null || !isValidTarget(hero, card, cell)) {
								if (cell != null) {
									com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
											Messages.get(CardTargeter.class, "bad_target"));
								}
								return;
							}
							CardEffects.play(hero, card, cell);
						}

						@Override
						public String prompt() {
							return Messages.get(CardTargeter.class, "prompt", card.name());
						}
					});
				}
				break;
		}
	}

	public static boolean isValidTarget(Hero hero, Card card, int cell) {
		Char ch = Actor.findChar(cell);
		switch (card.type.target) {
			case NONE:
				return true;
			case SELF:
				return ch == hero;
			case ENEMY:
				return ch != null && ch != hero && ch.alignment != Char.Alignment.ALLY;
			case ANY:
				return ch != null && (ch == hero || ch.alignment != Char.Alignment.ALLY);
			default:
				return false;
		}
	}

	public static boolean hasValidTarget(Hero hero, Card card) {
		if (card.type.target == CardType.Target.SELF || card.type.target == CardType.Target.NONE) {
			return true;
		}
		for (Char ch : Actor.chars()) {
			if (isValidTarget(hero, card, ch.pos)) return true;
		}
		return false;
	}

	public static void offerSafeReturn(Hero hero, Card card) {
		GameScene.show(new WndOptions(
				Messages.get(CardTargeter.class, "no_target_title"),
				Messages.get(CardTargeter.class, "no_target_desc"),
				Messages.get(CardTargeter.class, "safe_return"),
				Messages.get(CardTargeter.class, "cancel")) {
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					if (hero.cardDeck != null && hero.cardDeck.held == card) {
						hero.cardDeck.putHeldBack(card);
						HeldCardTracker.clear(hero);
						hero.spendAndNext(1f);
						com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.refresh();
					}
				}
			}
		});
	}

	public static void offerSafeReturnIfNone(Hero hero) {
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			Card card = hero.cardDeck.held;
			if (card.type.needsTarget() && !hasValidTarget(hero, card)) {
				offerSafeReturn(hero, card);
			}
		}
	}

	/** Convenience for the deck window. */
	public static void playHeld(Hero hero) {
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			play(hero, hero.cardDeck.held);
		}
	}

	public static int heldCount(Hero hero) {
		return hero.cardDeck == null || hero.cardDeck.held == null ? 0 : 1;
	}

	public static boolean inCombat(Hero hero) {
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]) return true;
		}
		return false;
	}
}
