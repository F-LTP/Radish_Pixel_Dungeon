package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToy;
import com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToyEffects;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class TieredToyBuff extends Buff {

	private int floorTurns;
	private int floorDepth = -1;
	private int periodicTurns;
	private int spongeTurns;
	private int chainTarget = -1;
	private int chainHits;
	private int terrariumGrowth;
	private boolean determinationUsed;
	private boolean spinachUsed;

	{
		type = buffType.POSITIVE;
	}

	@Override
	public boolean act() {
		if (floorDepth != Dungeon.depth) {
			floorDepth = Dungeon.depth;
			floorTurns = 0;
			determinationUsed = false;
			spinachUsed = false;
			terrariumGrowth = 0;
		}
		floorTurns++;
		periodicTurns++;
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			if (TieredToyEffects.has(TieredToy.EnchantedShield.class)) Buff.affect(hero, Barrier.class).incShield(3);
			if (periodicTurns % 10 == 0) {
				if (TieredToyEffects.has(TieredToy.GhostShield.class)) Buff.affect(hero, Barrier.class).incShield(3);
				if (TieredToyEffects.has(TieredToy.CrackedPlate.class)) Buff.affect(hero, Barrier.class).incShield(12);
			}
			if (spongeTurns > 0) spongeTurns--;
		}
		spend(TICK);
		return true;
	}

	public int floorTurns() { return floorTurns; }
	public void startSponge() { spongeTurns = 3; }
	public int spongeTurns() { return spongeTurns; }
	public float chainMultiplier(com.shatteredpixel.shatteredpixeldungeon.actors.Char enemy) {
		if (chainTarget != enemy.id()) {
			chainTarget = enemy.id();
			chainHits = 0;
		}
		float multiplier = 1f + Math.min(4, chainHits) * 0.25f;
		chainHits++;
		return multiplier;
	}
	public int growTerrariumShield() {
		int bonus = terrariumGrowth;
		terrariumGrowth = Math.min(10, terrariumGrowth + 1);
		return bonus;
	}
	public boolean determinationUsed() { return determinationUsed; }
	public void useDetermination() { determinationUsed = true; }
	public boolean spinachUsed() { return spinachUsed; }
	public void useSpinach() { spinachUsed = true; }

	@Override public String icon() { return BuffIndicator.NONE; }

	private static final String FLOOR_TURNS = "floor_turns";
	private static final String FLOOR_DEPTH = "floor_depth";
	private static final String PERIODIC_TURNS = "periodic_turns";
	private static final String SPONGE_TURNS = "sponge_turns";
	private static final String CHAIN_TARGET = "chain_target";
	private static final String CHAIN_HITS = "chain_hits";
	private static final String TERRARIUM_GROWTH = "terrarium_growth";
	private static final String DETERMINATION = "determination_used";
	private static final String SPINACH = "spinach_used";

	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(FLOOR_TURNS, floorTurns);
		bundle.put(FLOOR_DEPTH, floorDepth);
		bundle.put(PERIODIC_TURNS, periodicTurns);
		bundle.put(SPONGE_TURNS, spongeTurns);
		bundle.put(CHAIN_TARGET, chainTarget);
		bundle.put(CHAIN_HITS, chainHits);
		bundle.put(TERRARIUM_GROWTH, terrariumGrowth);
		bundle.put(DETERMINATION, determinationUsed);
		bundle.put(SPINACH, spinachUsed);
	}

	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		floorTurns = bundle.getInt(FLOOR_TURNS);
		floorDepth = bundle.getInt(FLOOR_DEPTH);
		periodicTurns = bundle.getInt(PERIODIC_TURNS);
		spongeTurns = bundle.getInt(SPONGE_TURNS);
		chainTarget = bundle.getInt(CHAIN_TARGET);
		chainHits = bundle.getInt(CHAIN_HITS);
		terrariumGrowth = bundle.getInt(TERRARIUM_GROWTH);
		determinationUsed = bundle.getBoolean(DETERMINATION);
		spinachUsed = bundle.getBoolean(SPINACH);
	}
}
