package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 减免全部结算后发布，damage 为实际 HP 扣减（>0 才发；护盾全吸收/免疫不发）；不可取消，改伤害回上游。
 * 发布时目标 HP 可能已 <=0（die() 在其后才调用）；attacker/type 语义同 CharUnprocedDamageEvent。
 */
public class CharFinalDamageEvent extends GameEvent {

	private final Char target;
	private final Char attacker;
	private final Object source;
	private final Item sourceItem;
	private final int damage;
	private final DamageType type;

	public CharFinalDamageEvent(@NotNull Char target, @Nullable Char attacker,
	                            @Nullable Object source, int damage, @NotNull DamageType type) {
		this(target, attacker, source, null, damage, type);
	}

	public CharFinalDamageEvent(@NotNull Char target, @Nullable Char attacker,
	                            @Nullable Object source, @Nullable Item sourceItem,
	                            int damage, @NotNull DamageType type) {
		this.target = target;
		this.attacker = attacker;
		this.source = source;
		this.sourceItem = sourceItem;
		this.damage = damage;
		this.type = type;
	}

	public @NotNull Char getTarget() { return target; }

	public @Nullable Char getAttacker() { return attacker; }

	public @Nullable Object getSource() { return source; }

	public @Nullable Item getSourceItem() { return sourceItem; }

	public int getDamage() { return damage; }

	public @NotNull DamageType getType() { return type; }
}
