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
	public static final float WAND_FREE_CAP = 0.75f;

	private float wandFreeChance = 0f;

	public void addWandFreeChance(float amount) {
		wandFreeChance = Math.min(WAND_FREE_CAP, wandFreeChance + amount);
	}

	public float wandFreeChance() {
		return wandFreeChance;
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
		return Messages.get(this, "desc", Math.round(wandFreeChance * 100));
	}

	private static final String WAND_FREE = "wand_free_chance";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(WAND_FREE, wandFreeChance);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		wandFreeChance = bundle.getFloat(WAND_FREE);
	}
}
