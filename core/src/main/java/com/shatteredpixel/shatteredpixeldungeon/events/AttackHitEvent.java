package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 攻击流程第 3 站：enemy.damage() 之后、血花之前发布；伤害已生效，不可取消。
 * effectiveDamage 是 fold 后用于血花的数值，damage 是完整 DamageInfo，两者可能不同；preparation 未潜行为 null。
 * 优先级：命中后 debuff -100 区间 → 控制 -200 → 斩杀 -300 → 处决/暗杀 -400。
 */
public class AttackHitEvent extends GameEvent {

	private final Char attacker;
	private final Char defender;

	private final int effectiveDamage;
	private final DamageInfo damage;

	private final Item attackerWeapon;
	private final Item defenderArmor;

	private final Preparation preparation;

	public AttackHitEvent(@NotNull Char attacker, @NotNull Char defender, int effectiveDamage,
	                      @NotNull DamageInfo damage,
	                      @Nullable Item attackerWeapon, @Nullable Item defenderArmor,
	                      @Nullable Preparation preparation) {
		this.attacker = attacker;
		this.defender = defender;
		this.effectiveDamage = effectiveDamage;
		this.damage = damage;
		this.attackerWeapon = attackerWeapon;
		this.defenderArmor = defenderArmor;
		this.preparation = preparation;
	}

	public @NotNull Char getAttacker() { return attacker; }

	public @NotNull Char getDefender() { return defender; }

	public int getEffectiveDamage() { return effectiveDamage; }

	public @NotNull DamageInfo getDamage() { return damage; }

	public @Nullable Item getAttackerWeapon() { return attackerWeapon; }

	public @Nullable Item getDefenderArmor() { return defenderArmor; }

	public @Nullable Preparation getPreparation() { return preparation; }
}
