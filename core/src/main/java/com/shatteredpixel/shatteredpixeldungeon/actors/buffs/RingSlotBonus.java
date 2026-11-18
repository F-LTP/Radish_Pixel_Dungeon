package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 红包第一个属性：戒指栏位 +1。
 * 用 Buff 作为一次性标记，避免读档/换层后重复发放。
 */
public class RingSlotBonus extends Buff {

	@Override
	public boolean act() {
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.HONGBAO;
	}
}
