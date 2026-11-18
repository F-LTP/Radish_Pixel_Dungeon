package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

/** A card is a pure data object; it is never placed in the hero's belongings. */
public class Card implements Bundlable {

	public CardType type;
	public int power;
	public int gearTier;
	public int gearLevel;
	public Weapon.Enchantment enchantment;

	/** 陷阱卡记录的陷阱类（Trap 子类）。 */
	public Class<?> trapClass;
	/** 传送卡的传送距离。 */
	public int teleportRange;

	/** 运行时标记：这张卡是从临时存储槽打出的（不入档）。 */
	public boolean fromStorage;

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
		return type == null ? Messages.get(CardType.class, "blank.name") : type.title();
	}

	public String desc() {
		return type == null ? Messages.get(CardType.class, "blank.desc") : type.desc();
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
		c.trapClass = trapClass;
		c.teleportRange = teleportRange;
		return c;
	}

	private static final String TYPE = "type";
	private static final String POWER = "power";
	private static final String GEAR_TIER = "gear_tier";
	private static final String GEAR_LEVEL = "gear_level";
	private static final String ENCHANTMENT = "enchantment";
	private static final String TRAP_CLASS = "trap_class";
	private static final String TELEPORT_RANGE = "teleport_range";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(TYPE, type.name());
		bundle.put(POWER, power);
		bundle.put(GEAR_TIER, gearTier);
		bundle.put(GEAR_LEVEL, gearLevel);
		bundle.put(ENCHANTMENT, enchantment);
		bundle.put(TRAP_CLASS, trapClass == null ? "" : trapClass.getName());
		bundle.put(TELEPORT_RANGE, teleportRange);
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
		String tc = bundle.getString(TRAP_CLASS);
		if (tc != null && !tc.isEmpty()) {
			try {
				trapClass = Class.forName(tc);
			} catch (ClassNotFoundException e) {
				trapClass = null;
			}
		}
		teleportRange = bundle.getInt(TELEPORT_RANGE);
	}
}
