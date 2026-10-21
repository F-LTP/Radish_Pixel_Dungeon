package com.shatteredpixel.shatteredpixeldungeon.items.cards;

public final class CardFactory {

	private CardFactory() {
	}

	public static Card blank() {
		return new Card(CardType.BLANK);
	}

	public static Card smallRation() {
		return new Card(CardType.SMALL_RATION);
	}

	public static Card ration() {
		return new Card(CardType.RATION);
	}

	public static Card identify() {
		return new Card(CardType.IDENTIFY);
	}

	public static Card rigid() {
		return new Card(CardType.RIGID);
	}

	public static Card hormone() {
		return new Card(CardType.HORMONE);
	}

	public static Card spin() {
		return new Card(CardType.SPIN);
	}

	public static Card grandFinale() {
		return new Card(CardType.GRAND_FINALE);
	}

	public static Card prepared() {
		return new Card(CardType.PREPARED);
	}

	public static Card attack() {
		return new Card(CardType.ATTACK);
	}

	public static Card defense() {
		return new Card(CardType.DEFENSE);
	}

	public static Card randomAttackOrDefense() {
		return com.watabou.utils.Random.Int(2) == 0 ? attack() : defense();
	}
}
