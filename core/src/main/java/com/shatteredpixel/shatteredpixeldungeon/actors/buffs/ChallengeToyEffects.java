/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToy;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class ChallengeToyEffects extends Buff {

	private static final String TOYS = "toys";
	private static final String REMAINING = "remaining";

	private final ArrayList<ItemArmorAttachable> toys = new ArrayList<>();
	private final ArrayList<Float> remaining = new ArrayList<>();
	private transient boolean restoreCrackedPlate;

	{
		type = buffType.POSITIVE;
	}

	public static void grant(Hero hero, ItemArmorAttachable toy, int duration) {
		if (hero == null || toy == null || duration <= 0) return;
		Buff.affect(hero, ChallengeToyEffects.class).addEffect(hero, toy, duration);
	}

	public static boolean hasEffect(Hero hero, Class<? extends ItemArmorAttachable> type) {
		ChallengeToyEffects effects = hero == null ? null : hero.buff(ChallengeToyEffects.class);
		return effects != null && effects.getToy(type) != null;
	}

	public static void removePermanentEffect(Hero hero, ItemArmorAttachable toy) {
		ChallengeToyEffects effects = hero == null ? null : hero.buff(ChallengeToyEffects.class);
		ItemArmorAttachable temporary = effects == null ? null : effects.getToy(toy.getClass());
		toy.removeEffect(hero);
		if (temporary != null) temporary.applyEffect(hero);
	}

	public void addEffect(Hero hero, ItemArmorAttachable toy, int duration) {
		toys.add(toy);
		remaining.add((float) duration);
		toy.applyEffect(hero);
	}

	@SuppressWarnings("unchecked")
	public <T extends ItemArmorAttachable> T getToy(Class<T> type) {
		for (ItemArmorAttachable toy : toys) {
			if (toy.getClass() == type) return (T) toy;
		}
		return null;
	}

	public ArrayList<ItemArmorAttachable> effects() {
		return new ArrayList<>(toys);
	}

	public int remainingTurns(int index) {
		if (index < 0 || index >= remaining.size()) return 0;
		return Math.max(0, (int) Math.ceil(remaining.get(index)));
	}

	@Override
	public boolean act() {
		Hero hero = target instanceof Hero ? (Hero) target : null;
		if (hero == null) {
			detach();
			return true;
		}

		if (restoreCrackedPlate) {
			restoreCrackedPlate = false;
			for (ItemArmorAttachable toy : toys) {
				if (toy instanceof TieredToy.CrackedPlate) toy.applyEffect(hero);
			}
		}

		for (int i = remaining.size() - 1; i >= 0; i--) {
			float turns = remaining.get(i) - TICK;
			if (turns <= 0) {
				ItemArmorAttachable expired = toys.remove(i);
				remaining.remove(i);
				removeEffectIfUnused(hero, expired);
				GLog.i(Messages.get(this, "expired", expired.name()));
			} else {
				remaining.set(i, turns);
			}
		}

		if (toys.isEmpty()) {
			detach();
		} else {
			spend(TICK);
		}
		return true;
	}

	private void removeEffectIfUnused(Hero hero, ItemArmorAttachable toy) {
		if (!ItemArmorAttachable.hasAttached(toy.getClass())) {
			toy.removeEffect(hero);
		}
		if (toy instanceof TieredToy.BagOfHolding && hero.belongings.armor != null) {
			hero.belongings.armor.dropExcessToysAfterCapacityChange(hero);
		}
	}

	@Override
	protected void onRemove() {
		if (target instanceof Hero && !toys.isEmpty()) {
			Hero hero = (Hero) target;
			ArrayList<ItemArmorAttachable> removed = new ArrayList<>(toys);
			toys.clear();
			remaining.clear();
			for (ItemArmorAttachable toy : removed) removeEffectIfUnused(hero, toy);
		}
		super.onRemove();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TOYS, toys);
		float[] durations = new float[remaining.size()];
		for (int i = 0; i < remaining.size(); i++) durations[i] = remaining.get(i);
		bundle.put(REMAINING, durations);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		toys.clear();
		for (Bundlable stored : bundle.getCollection(TOYS)) {
			if (stored instanceof ItemArmorAttachable) toys.add((ItemArmorAttachable) stored);
		}

		remaining.clear();
		float[] storedDurations = bundle.getFloatArray(REMAINING);
		if (storedDurations != null) {
			for (float duration : storedDurations) remaining.add(duration);
		}
		while (remaining.size() > toys.size()) remaining.remove(remaining.size() - 1);
		while (toys.size() > remaining.size()) toys.remove(toys.size() - 1);
		restoreCrackedPlate = true;
	}
}
