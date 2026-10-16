package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 未命中分支开头发布，早于 defenseVerb 文字与 MISS 音效；不可把未命中改判为命中（使用HitCheckEvent）。
 * 优先级：职业效果 +100 先于装备 0。
 */
public class AttackMissEvent extends GameEvent {

	private final Char attacker;
	private final Char defender;

	private final Item attackerWeapon;
	private final Item defenderWeapon;
	private final Item defenderArmor;

	public AttackMissEvent(@NotNull Char attacker, @NotNull Char defender,
	                       @Nullable Item attackerWeapon, @Nullable Item defenderWeapon, @Nullable Item defenderArmor) {
		this.attacker = attacker;
		this.defender = defender;
		this.attackerWeapon = attackerWeapon;
		this.defenderWeapon = defenderWeapon;
		this.defenderArmor = defenderArmor;
	}

	public @NotNull Char getAttacker() { return attacker; }

	public @NotNull Char getDefender() { return defender; }

	public @Nullable Item getAttackerWeapon() { return attackerWeapon; }

	public @Nullable Item getDefenderWeapon() { return defenderWeapon; }

	public @Nullable Item getDefenderArmor() { return defenderArmor; }
}
