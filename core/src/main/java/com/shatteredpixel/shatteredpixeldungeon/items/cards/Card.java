package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

/** A card is a pure data object; it is never placed in the hero's belongings. */
public class Card implements Bundlable {

	public CardType type;
	public int power;
	public int gearTier;
	public int gearLevel;
	public Weapon.Enchantment enchantment;

	public Card() {
	}

	public Card(CardType type) {
		this(type, 1);
	}

	public Card(CardType type, int power) {
		this.type = type;
		this.power = power;
	}

	public String name() {
		return type.title();
	}

	public String desc() {
		return type.desc();
	}

	public Card copyPower(int newPower) {
		power = newPower;
		return this;
	}

	public Card copy() {
		Card c = new Card(type, power);
		c.gearTier = gearTier;
		c.gearLevel = gearLevel;
		c.enchantment = enchantment;
		return c;
	}

	private static final String TYPE = "type";
	private static final String POWER = "power";
	private static final String GEAR_TIER = "gear_tier";
	private static final String GEAR_LEVEL = "gear_level";
	private static final String ENCHANTMENT = "enchantment";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(TYPE, type.name());
		bundle.put(POWER, power);
		bundle.put(GEAR_TIER, gearTier);
		bundle.put(GEAR_LEVEL, gearLevel);
		bundle.put(ENCHANTMENT, enchantment);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		try {
			type = CardType.valueOf(bundle.getString(TYPE));
		} catch (Exception e) {
			type = CardType.BLANK;
		}
		power = bundle.getInt(POWER);
		gearTier = bundle.getInt(GEAR_TIER);
		gearLevel = bundle.getInt(GEAR_LEVEL);
		enchantment = (Weapon.Enchantment) bundle.get(ENCHANTMENT);
	}
}
