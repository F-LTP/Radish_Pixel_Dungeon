package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * hit() 内基础命中/闪避算完后发布；accuracy/evasion 分发后写回参与随机。
 * forceHit()/forceMiss() 覆写结果，先写者获胜（与 cancel 语义不同）；发布点在隐身必中判定之前，故 forceMiss 能压过偷袭必中。
 * 优先级：攻方必中 +1000 → 守方必闪 +800 区间 → 武器必中 +500；法杖判定 magic=true。
 */
public class HitCheckEvent extends GameEvent {

	public enum Result {
		NONE,
		FORCE_HIT,
		FORCE_MISS
	}

	private final Char attacker;
	private final Char defender;
	private final boolean magic;

	private final Item attackerWeapon;
	private final Item defenderWeapon;

	private float accuracy;
	private float evasion;

	private Result result = Result.NONE;

	public HitCheckEvent(@NotNull Char attacker, @NotNull Char defender, boolean magic,
	                     @Nullable Item attackerWeapon, @Nullable Item defenderWeapon,
	                     float accuracy, float evasion) {
		this.attacker = attacker;
		this.defender = defender;
		this.magic = magic;
		this.attackerWeapon = attackerWeapon;
		this.defenderWeapon = defenderWeapon;
		this.accuracy = accuracy;
		this.evasion = evasion;
	}

	public @NotNull Char getAttacker() { return attacker; }

	public @NotNull Char getDefender() { return defender; }

	public boolean isMagic() { return magic; }

	public @Nullable Item getAttackerWeapon() { return attackerWeapon; }

	public @Nullable Item getDefenderWeapon() { return defenderWeapon; }

	public float getAccuracy() { return accuracy; }

	public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

	public float getEvasion() { return evasion; }

	public void setEvasion(float evasion) { this.evasion = evasion; }

	public @NotNull Result getResult() { return result; }

	public void setResult(@NotNull Result result) { this.result = result; }

	public void forceHit() {
		if (result == Result.NONE) result = Result.FORCE_HIT;
	}

	public void forceMiss() {
		if (result == Result.NONE) result = Result.FORCE_MISS;
	}
}
