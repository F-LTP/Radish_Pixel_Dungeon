package com.shatteredpixel.shatteredpixeldungeon.events;

/** Char.heal() 实际回血且目标为玩家时发布（healed>0，已按 HT 截断）；不区分治疗来源 */
public class HeroHealEvent extends GameEvent {

	private final int healAmount;

	public HeroHealEvent(int healAmount) {
		this.healAmount = healAmount;
	}

	public int getHealAmount() { return healAmount; }
}
