/*
 * Radish Pixel Dungeon
 * Cross Level Challenge - Modifier for cross-region monster spawning in Snake Bite challenge
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundle;

/**
 * Applied to monsters that spawn from cross-region spawning in Snake Bite challenge.
 * Reduces stats by 30% for next-region monsters, increases by 30% for previous-region monsters.
 */
public class CrossLevelChallenge extends Buff {

	private static final String DIRECTION = "direction";

	public int direction = 0; // 0: NONE, 1: NEXT, 2: PREVIOUS

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String toString() {
		if (direction == 1) {
			return "跨区域怪物（弱）";
		} else if (direction == 2) {
			return "跨区域怪物（强）";
		}
		return super.toString();
	}

	@Override
	public String desc() {
		if (direction == 1) {
			return "这是一个从下一区域跨层而来的怪物，强度降低了30%。";
		} else if (direction == 2) {
			return "这是一个从上一区域跨层而来的怪物，强度提高了30%。";
		}
		return super.desc();
	}

	public static void setDirection(Mob mob, int dir) {
		CrossLevelChallenge buff = Buff.affect(mob, CrossLevelChallenge.class);
		buff.direction = dir;
	}

	public static float statModifier(Mob mob) {
		CrossLevelChallenge buff = mob.buff(CrossLevelChallenge.class);
		if (buff == null || buff.direction == 0) {
			return 1f;
		}

		if (buff.direction == 1) {
			// Next region monsters: 30% weaker
			return 0.7f;
		} else if (buff.direction == 2) {
			// Previous region monsters: 30% stronger
			return 1.3f;
		}

		return 1f;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DIRECTION, direction);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		direction = bundle.getInt(DIRECTION);
	}
}
