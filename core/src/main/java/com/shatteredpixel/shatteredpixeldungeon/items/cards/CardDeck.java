package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 单一牌堆模型：牌堆顶 = drawPile 末尾，牌堆底 = drawPile 开头。
 *
 * 打出的卡置于牌堆底；通过天赋/变换获得的卡置于牌堆顶。
 * 不再使用弃牌堆（discardPile 仅保留用于兼容旧存档，读入后并入牌堆）。
 */
public class CardDeck implements Bundlable {

	public final ArrayList<Card> drawPile = new ArrayList<>();
	/** 旧存档兼容字段，新逻辑不再写入。 */
	public final ArrayList<Card> discardPile = new ArrayList<>();
	public Card held;
	/** 「紧急避险」临时替换下来的原卡。 */
	public Card stashedCard;

	public int deletesLeft;
	public int freeDraws;
	/** 数据清洗次数（恶魔之力 T4）。 */
	public int cleansesLeft;
	/** 轮换计数：抽到/空转到底的牌数，达到牌堆大小时记一次轮换。 */
	public int drawCountThisCycle;
	public int cycleSize;
	public int cycles;

	/** 临时存储槽（护甲技能「临时管理」）。 */
	public final ArrayList<Card> stored = new ArrayList<>();

	public static CardDeck starterDeck() {
		CardDeck deck = new CardDeck();
		for (int i = 0; i < 20; i++) {
			deck.drawPile.add(CardFactory.blank());
		}
		deck.cycleSize = deck.drawPile.size();
		return deck;
	}

	public int totalSize() {
		return drawPile.size() + discardPile.size() + stored.size() + (held == null ? 0 : 1);
	}

	/** 牌堆（不含手牌与存储槽）的大小。 */
	public int deckSize() {
		return drawPile.size() + discardPile.size();
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
		all.addAll(stored);
		if (held != null) all.add(held);
		return all;
	}

	public boolean canDraw() {
		return held == null && totalSize() > 0;
	}

	public Card draw() {
		if (held != null || totalSize() == 0) return null;

		if (drawPile.isEmpty()) {
			if (!reshuffle()) return null;
		}

		Card c = drawPile.remove(drawPile.size() - 1);
		held = c;
		noteCycle(1);
		return c;
	}

	/** 旧存档的弃牌堆并入牌堆。 */
	public boolean reshuffle() {
		if (discardPile.isEmpty()) return false;
		drawPile.addAll(discardPile);
		discardPile.clear();
		com.watabou.utils.Random.shuffle(drawPile);
		return true;
	}

	public void playDone(Card card) {
		if (card == null) return;
		held = null;
		addToBottom(card);
	}

	/** 获得/变换得到的卡置于牌堆顶。 */
	public void addToTop(Card card) {
		if (card != null) drawPile.add(card);
	}

	/** 置于牌堆底。 */
	public void addToBottom(Card card) {
		if (card != null) drawPile.add(0, card);
	}

	/** 兼容旧调用：新逻辑中一律视为置于牌堆顶。 */
	public void addToDiscard(Card card) {
		addToTop(card);
	}

	public void putHeldBack(Card card) {
		if (held == card) held = null;
		addToBottom(card);
	}

	/**
	 * 空转 n：将牌堆顶 n 张牌中的空白卡移到牌堆底。
	 * 被空转的空白卡会增加轮换计数，但不会被“打出”。
	 */
	public int spinBlank(int n) {
		if (n <= 0 || drawPile.isEmpty()) return 0;
		n = Math.min(n, drawPile.size());
		ArrayList<Card> spun = new ArrayList<>();
		for (int i = drawPile.size() - 1; i >= drawPile.size() - n; i--) {
			if (drawPile.get(i).type == CardType.BLANK) spun.add(drawPile.get(i));
		}
		for (Card c : spun) {
			drawPile.remove(c);
			addToBottom(c);
		}
		noteCycle(spun.size());
		return spun.size();
	}

	/** 检视顶 n 张（从顶到底）。 */
	public ArrayList<Card> peekTop(int n) {
		ArrayList<Card> top = new ArrayList<>();
		for (int i = drawPile.size() - 1; i >= Math.max(0, drawPile.size() - n); i--) {
			top.add(drawPile.get(i));
		}
		return top;
	}

	/** 将一张牌移到牌堆底，并视作一次空转（有备无患）。 */
	public void moveToBottomAsSpin(Card c) {
		if (c == null) return;
		if (held == c) held = null;
		if (drawPile.remove(c) || discardPile.remove(c)) {
			addToBottom(c);
		} else {
			addToBottom(c);
		}
		noteCycle(1);
	}

	/** 把一张牌插入牌堆随机位置（花式切牌）。 */
	public void insertRandom(Card c) {
		if (c == null) return;
		int idx = com.watabou.utils.Random.Int(drawPile.size() + 1);
		drawPile.add(idx, c);
	}

	public void noteCycle(int amount) {
		if (amount <= 0) return;
		drawCountThisCycle += amount;
		int size = Math.max(1, deckSize());
		while (drawCountThisCycle >= size && size > 0) {
			drawCountThisCycle -= size;
			cycles++;
			if (Dungeon.hero != null) CardEffects.onDeckCycled(Dungeon.hero);
			size = Math.max(1, deckSize());
		}
	}

	public boolean delete(Card card) {
		if (card == null) return false;
		if (card == held) {
			held = null;
			return true;
		}
		return drawPile.remove(card) || discardPile.remove(card) || stored.remove(card);
	}

	public int blankCount() {
		return count(CardType.BLANK);
	}

	private static final String DRAW_PILE = "draw_pile";
	private static final String DISCARD_PILE = "discard_pile";
	private static final String HELD = "held";
	private static final String STASHED = "stashed";
	private static final String DELETES = "deletes";
	private static final String CLEANSES = "cleanses";
	private static final String FREE_DRAWS = "free_draws";
	private static final String DRAW_COUNT = "draw_count";
	private static final String CYCLE_SIZE = "cycle_size";
	private static final String CYCLES = "cycles";
	private static final String STORED = "stored";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(DRAW_PILE, new ArrayList<Bundlable>(drawPile));
		bundle.put(DISCARD_PILE, new ArrayList<Bundlable>(discardPile));
		bundle.put(STORED, new ArrayList<Bundlable>(stored));
		bundle.put(HELD, held);
		bundle.put(STASHED, stashedCard);
		bundle.put(DELETES, deletesLeft);
		bundle.put(CLEANSES, cleansesLeft);
		bundle.put(FREE_DRAWS, freeDraws);
		bundle.put(DRAW_COUNT, drawCountThisCycle);
		bundle.put(CYCLE_SIZE, cycleSize);
		bundle.put(CYCLES, cycles);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		drawPile.clear();
		discardPile.clear();
		stored.clear();
		for (Bundlable b : bundle.getCollection(DRAW_PILE)) {
			if (b instanceof Card) drawPile.add((Card) b);
		}
		for (Bundlable b : bundle.getCollection(DISCARD_PILE)) {
			if (b instanceof Card) discardPile.add((Card) b);
		}
		for (Bundlable b : bundle.getCollection(STORED)) {
			if (b instanceof Card) stored.add((Card) b);
		}
		// 旧存档兼容：弃牌堆已经移除，读入后并入牌堆底。
		// 留在 discardPile 里的卡只有牌堆抽空时才会洗回来，而现在打出的卡回牌堆底，牌堆基本不会空
		if (!discardPile.isEmpty()) {
			drawPile.addAll(0, discardPile);
			discardPile.clear();
		}
		held = (Card) bundle.get(HELD);
		stashedCard = (Card) bundle.get(STASHED);
		deletesLeft = bundle.getInt(DELETES);
		cleansesLeft = bundle.getInt(CLEANSES);
		freeDraws = bundle.getInt(FREE_DRAWS);
		drawCountThisCycle = bundle.getInt(DRAW_COUNT);
		cycleSize = bundle.getInt(CYCLE_SIZE);
		cycles = bundle.getInt(CYCLES);
	}

	public static CardDeck getOrCreate(Hero hero) {
		if (hero.cardDeck == null) {
			hero.cardDeck = starterDeck();
			if (hero.belongings.getItem(CardDrawer.class) == null) {
				new CardDrawer().collect(hero.belongings.backpack);
			}
		}
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CardAutoDraw.class);
		return hero.cardDeck;
	}

	public String summary() {
		return Messages.get(CardDeck.class, "summary", drawPile.size(), discardPile.size(), held == null ? 0 : 1);
	}
}
