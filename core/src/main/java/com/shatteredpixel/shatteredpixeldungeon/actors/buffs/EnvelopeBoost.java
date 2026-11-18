package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 红包祝福：目前承载“法杖不消耗充能概率”。
 * 保留 EnvelopeBoost 类名用于兼容旧存档。
 */
public class EnvelopeBoost extends Buff {

	public static final float WAND_FREE_PER_PICKUP = 0.05f;
	public static final float WAND_FREE_CAP = 1.0f;

	private float wandFreeChance = 0f;
	private int comboHits = 0;
	private int reachBonus = 0;
	private float missileReturnChance = 0f;
	private float enchantPower = 0f;

	public void addWandFreeChance(float amount) {
		wandFreeChance = Math.min(WAND_FREE_CAP, wandFreeChance + amount);
	}

	public float wandFreeChance() {
		return wandFreeChance;
	}

	public void addComboHits(int amount) {
		comboHits += amount;
	}

	public int comboHits() {
		return comboHits;
	}

	public static int comboHits(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.comboHits;
	}

	public void addReachBonus(int amount) {
		reachBonus += amount;
	}

	public int reachBonus() {
		return reachBonus;
	}

	public static int reachBonus(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.reachBonus;
	}

	public void addMissileReturnChance(float amount) {
		missileReturnChance = Math.min(1.0f, missileReturnChance + amount);
	}

	public float missileReturnChance() {
		return missileReturnChance;
	}

	public static boolean rollMissileReturn(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b != null && b.missileReturnChance > 0f && Random.Float() < b.missileReturnChance;
	}

	public void addEnchantPower(float amount) {
		enchantPower += amount;
	}

	public float enchantPower() {
		return enchantPower;
	}

	public static float enchantPower(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0f : b.enchantPower;
	}

	public static float wandFreeChance(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0f : b.wandFreeChance;
	}

	public static boolean rollFreeWandCast(Char ch) {
		float chance = wandFreeChance(ch);
		return chance > 0f && Random.Float() < chance;
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.HONGBAO;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round(wandFreeChance * 100), comboHits, reachBonus,
				Math.round(missileReturnChance * 100), Math.round(enchantPower * 100));
	}

	private static final String WAND_FREE = "wand_free_chance";
	private static final String COMBO_HITS = "combo_hits";
	private static final String REACH_BONUS = "reach_bonus";
	private static final String MISSILE_RETURN = "missile_return_chance";
	private static final String ENCHANT_POWER = "enchant_power";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(WAND_FREE, wandFreeChance);
		bundle.put(COMBO_HITS, comboHits);
		bundle.put(REACH_BONUS, reachBonus);
		bundle.put(MISSILE_RETURN, missileReturnChance);
		bundle.put(ENCHANT_POWER, enchantPower);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		wandFreeChance = bundle.getFloat(WAND_FREE);
		comboHits = bundle.getInt(COMBO_HITS);
		reachBonus = bundle.getInt(REACH_BONUS);
		missileReturnChance = bundle.getFloat(MISSILE_RETURN);
		enchantPower = bundle.getFloat(ENCHANT_POWER);
	}
}
