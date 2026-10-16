package com.shatteredpixel.shatteredpixeldungeon.damage;

/**
 * 一次伤害管线执行的不可变结果摘要。
 * 用于浮动伤害数字、护盾反馈、战斗事件、统计成就、调试跟踪与死亡处理。
 *
 * 字段约束：
 * modifiedDamage - armorBlocked - resistanceBlocked - shieldBlocked = hpDamage
 */
public final class DamageResult {
	/** 基础伤害（未应用 modifier） */
	public final int baseDamage;

	/** 应用所有 modifier 后的伤害（进入减免前） */
	public final int modifiedDamage;

	/** 被护甲抵消的伤害 */
	public final int armorBlocked;

	/** 被 DamageType 抗性/免疫抵消的伤害 */
	public final int resistanceBlocked;

	/** 被护盾吸收的伤害 */
	public final int shieldBlocked;

	/** 实际扣除的 HP */
	public final int hpDamage;

	/** 是否被判定为免疫（伤害被完全抵消） */
	public final boolean immune;

	public DamageResult(int baseDamage, int modifiedDamage, int armorBlocked,
						int resistanceBlocked, int shieldBlocked, int hpDamage, boolean immune) {
		this.baseDamage = baseDamage;
		this.modifiedDamage = modifiedDamage;
		this.armorBlocked = armorBlocked;
		this.resistanceBlocked = resistanceBlocked;
		this.shieldBlocked = shieldBlocked;
		this.hpDamage = hpDamage;
		this.immune = immune;
	}
}
