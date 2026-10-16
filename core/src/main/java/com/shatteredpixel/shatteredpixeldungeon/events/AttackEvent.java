package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 攻击流程第 2 站：命中判定通过、伤害构建完成后（foldPostProcessing 前）发布。
 * cancel() 可取消本次攻击——反弹类效果用（Rlyeh 取消并反伤）；优先级 +100。
 */
public class AttackEvent extends GameEvent {

	private final Char attacker;
	private final Char defender;
	private final int damage;

	private final Item attackerWeapon;
	private final Item defenderWeapon;
	private final Item defenderArmor;

	public AttackEvent(@NotNull Char attacker, @NotNull Char defender, int damage,
	                   @Nullable Item attackerWeapon, @Nullable Item defenderWeapon, @Nullable Item defenderArmor) {
		this.attacker = attacker;
		this.defender = defender;
		this.damage = damage;
		this.attackerWeapon = attackerWeapon;
		this.defenderWeapon = defenderWeapon;
		this.defenderArmor = defenderArmor;
	}

	public @NotNull Char getAttacker() { return attacker; }

	public @NotNull Char getDefender() { return defender; }

	public int getDamage() { return damage; }

	public @Nullable Item getAttackerWeapon() { return attackerWeapon; }

	public @Nullable Item getDefenderWeapon() { return defenderWeapon; }

	public @Nullable Item getDefenderArmor() { return defenderArmor; }
}
