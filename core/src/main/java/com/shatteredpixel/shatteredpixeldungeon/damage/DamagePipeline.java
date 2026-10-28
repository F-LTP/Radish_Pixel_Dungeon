package com.shatteredpixel.shatteredpixeldungeon.damage;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/** 唯一权威伤害执行入口。所有伤害（含旧 damage(int,Object)）最终都经由本管线。 */
public final class DamagePipeline {
	private DamagePipeline() {}

	private static final ThreadLocal<DamageInfo> ACTIVE = new ThreadLocal<>();

	/** 返回当前正在管线中执行的 DamageInfo（嵌套伤害时用于上下文）。 */
	public static DamageInfo activeInfo() { return ACTIVE.get(); }

	public static DamageResult apply(Char target, DamageInfo info) {
		if (target == null) throw new IllegalArgumentException("target cannot be null");
		if (info == null) throw new IllegalArgumentException("info cannot be null");
		DamageInfo previous = ACTIVE.get();
		ACTIVE.set(info);
		try {
			return target.applyDamage(info);
		} finally {
			if (previous == null) {
				ACTIVE.remove();
			} else {
				ACTIVE.set(previous);
			}
		}
	}
}
