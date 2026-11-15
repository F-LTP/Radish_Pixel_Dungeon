package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** 红包刷新计时器。 */
public class HongbaoClock extends Buff {

	private int turns = 0;

	@Override
	public boolean act() {
		// 红包改为每层开局生成，旧的计时器只保留兼容，不再刷新红包。
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
