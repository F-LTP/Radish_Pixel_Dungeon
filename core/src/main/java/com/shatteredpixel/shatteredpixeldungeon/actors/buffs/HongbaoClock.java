package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** 红包刷新计时器。 */
public class HongbaoClock extends Buff {

	private int turns = 0;

	@Override
	public boolean act() {
		if (Hongbao.active()) {
			turns++;
			if (turns >= Hongbao.SPAWN_INTERVAL) {
				turns = 0;
				Hongbao.spawn();
			}
		}
		spend(TICK);
		return true;
	}

	public int progress() {
		return turns;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("turns", turns);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turns = bundle.getInt("turns");
	}
}
