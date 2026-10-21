package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/** Draw pile, discard pile and the single held card. Cards never enter the inventory. */
public class CardDeck implements Bundlable {

	public final ArrayList<Card> drawPile = new ArrayList<>();
	public final ArrayList<Card> discardPile = new ArrayList<>();
	public Card held;

	public int deletesLeft;
	public int freeDraws;
	public int drawCountThisCycle;
	public int cycleSize;

	public static CardDeck starterDeck() {
		CardDeck deck = new CardDeck();
		for (int i = 0; i < 20; i++) {
			deck.drawPile.add(CardFactory.blank());
		}
		deck.cycleSize = deck.drawPile.size();
		return deck;
	}

	public int totalSize() {
		return drawPile.size() + discardPile.size() + (held == null ? 0 : 1);
	}

	public int count(CardType type) {
		int count = 0;
		for (Card c : allCards()) {
			if (c.type == type) count++;
		}
		return count;
	}

	public ArrayList<Card> allCards() {
		ArrayList<Card> all = new ArrayList<>();
		all.addAll(drawPile);
		all.addAll(discardPile);
		if (held != null) all.add(held);
		return all;
	}

	public boolean canDraw() {
		return held == null && totalSize() > 0;
	}

	public Card draw() {
		if (held != null || totalSize() == 0) return null;

		boolean cycled = false;
		if (drawPile.isEmpty()) {
			cycled = reshuffle();
			if (!cycled) return null;
		}

		Card c = drawPile.remove(drawPile.size() - 1);
		held = c;
		drawCountThisCycle++;

		if (cycled && Dungeon.hero != null) {
			CardEffects.onDeckCycled(Dungeon.hero);
		}
		return c;
	}

	/** Moves the discard pile back under the draw pile. Returns true if anything moved. */
	public boolean reshuffle() {
		if (discardPile.isEmpty()) return false;
		drawPile.addAll(discardPile);
		discardPile.clear();
		com.watabou.utils.Random.shuffle(drawPile);
		cycleSize = totalSize();
		drawCountThisCycle = 0;
		return true;
	}

	public void playDone(Card card) {
		if (card == null) return;
		held = null;
		discardPile.add(card);
	}

	public void addToDiscard(Card card) {
		if (card != null) discardPile.add(card);
	}

	public void addToBottom(Card card) {
		if (card != null) drawPile.add(0, card);
	}

	public void putHeldBack(Card card) {
		if (held == card) held = null;
		addToBottom(card);
	}

	public void spin(int amount) {
		if (amount <= 0 || drawPile.isEmpty()) return;
		amount = Math.min(amount, drawPile.size());
		ArrayList<Card> top = new ArrayList<>(drawPile.subList(drawPile.size() - amount, drawPile.size()));
		drawPile.subList(drawPile.size() - amount, drawPile.size()).clear();
		drawPile.addAll(0, top);
	}

	public boolean delete(Card card) {
		if (card == null) return false;
		if (card == held) {
			held = null;
			return true;
		}
		return drawPile.remove(card) || discardPile.remove(card);
	}

	/** All cards, used for "each card in the deck" scaling effects. */
	public int blankCount() {
		return count(CardType.BLANK);
	}

	private static final String DRAW_PILE = "draw_pile";
	private static final String DISCARD_PILE = "discard_pile";
	private static final String HELD = "held";
	private static final String DELETES = "deletes";
	private static final String FREE_DRAWS = "free_draws";
	private static final String DRAW_COUNT = "draw_count";
	private static final String CYCLE_SIZE = "cycle_size";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(DRAW_PILE, new ArrayList<Bundlable>(drawPile));
		bundle.put(DISCARD_PILE, new ArrayList<Bundlable>(discardPile));
		bundle.put(HELD, held);
		bundle.put(DELETES, deletesLeft);
		bundle.put(FREE_DRAWS, freeDraws);
		bundle.put(DRAW_COUNT, drawCountThisCycle);
		bundle.put(CYCLE_SIZE, cycleSize);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		drawPile.clear();
		discardPile.clear();
		for (Bundlable b : bundle.getCollection(DRAW_PILE)) {
			if (b instanceof Card) drawPile.add((Card) b);
		}
		for (Bundlable b : bundle.getCollection(DISCARD_PILE)) {
			if (b instanceof Card) discardPile.add((Card) b);
		}
		held = (Card) bundle.get(HELD);
		deletesLeft = bundle.getInt(DELETES);
		freeDraws = bundle.getInt(FREE_DRAWS);
		drawCountThisCycle = bundle.getInt(DRAW_COUNT);
		cycleSize = bundle.getInt(CYCLE_SIZE);
	}

	public static CardDeck getOrCreate(Hero hero) {
		if (hero.cardDeck == null) {
			hero.cardDeck = starterDeck();
			if (hero.belongings.getItem(CardDrawer.class) == null) {
				new CardDrawer().collect(hero.belongings.backpack);
			}
		}
		return hero.cardDeck;
	}

	public String summary() {
		return Messages.get(CardDeck.class, "summary", drawPile.size(), discardPile.size(), held == null ? 0 : 1);
	}
}
