package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 护盾被打破时发布：受击前护盾 > 0、受击后护盾归零，且本次护盾前伤害 > 0（护盾全吸收、只吸收一部分都会发）。
 * 在护盾相位内、HP 扣减之前发布，此时目标 HP 仍是受击前的值；不可取消。
 * absorbed 为本次被护盾吃掉的伤害。
 */
public class ShieldBreakEvent extends GameEvent {

	private final Char target;
	private final Char attacker;
	private final int absorbed;

	public ShieldBreakEvent(@NotNull Char target, @Nullable Char attacker, int absorbed) {
		this.target = target;
		this.attacker = attacker;
		this.absorbed = absorbed;
	}

	public @NotNull Char getTarget() { return target; }

	public @Nullable Char getAttacker() { return attacker; }

	public int getAbsorbed() { return absorbed; }
}
