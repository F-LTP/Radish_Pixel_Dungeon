package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 减免计算前的原始伤害（目标存活、dmg>=0 时发布）；不可取消、无修改入口（改伤用 DamageInfo modifier / PreAttackEvent）。
 * attacker 由 src 推断，来源非 Char（毒/饥饿/陷阱）为 null；type 未指定为 PHYSICAL；最终伤害见 CharFinalDamageEvent。
 */
public class CharUnprocedDamageEvent extends GameEvent {

	private final Char target;
	private final Char attacker;
	private final Object source;
	private final int damage;
	private final DamageType type;

	public CharUnprocedDamageEvent(@NotNull Char target, @Nullable Char attacker,
	                               @Nullable Object source, int damage, @NotNull DamageType type) {
		this.target = target;
		this.attacker = attacker;
		this.source = source;
		this.damage = damage;
		this.type = type;
	}

	public @NotNull Char getTarget() { return target; }

	public @Nullable Char getAttacker() { return attacker; }

	public @Nullable Object getSource() { return source; }

	public int getDamage() { return damage; }

	public @NotNull DamageType getType() { return type; }
}
