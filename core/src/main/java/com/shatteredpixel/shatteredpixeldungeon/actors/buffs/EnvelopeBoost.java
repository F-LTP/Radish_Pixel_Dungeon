package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

/**
 * 旧版红包属性系统的兼容占位。
 * 目前不再提供任何属性加成；保留类名是为了旧存档能正常读取。
 */
public class EnvelopeBoost extends Buff {

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}
}
