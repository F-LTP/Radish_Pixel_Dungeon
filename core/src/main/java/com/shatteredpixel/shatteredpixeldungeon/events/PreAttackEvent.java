package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import org.jetbrains.annotations.NotNull;

/**
 * attack() 开头（FOV/无敌判定前）发布；cancel() 则本次攻击作废（attack 返回 true，如影舞者破隐换突袭）。
 * dmgMulti/dmgBonus/accMulti 分发后写回攻击流程；优先级：替换/取消 +1000，伤害乘区 +500。
 */
public class PreAttackEvent extends GameEvent {

	private final Char attacker;
	private final Char defender;

	private float dmgMulti;
	private float dmgBonus;
	private float accMulti;

	public PreAttackEvent(@NotNull Char attacker, @NotNull Char defender,
	                      float dmgMulti, float dmgBonus, float accMulti) {
		this.attacker = attacker;
		this.defender = defender;
		this.dmgMulti = dmgMulti;
		this.dmgBonus = dmgBonus;
		this.accMulti = accMulti;
	}

	public @NotNull Char getAttacker() { return attacker; }

	public @NotNull Char getDefender() { return defender; }

	public float getDmgMulti() { return dmgMulti; }

	public void setDmgMulti(float dmgMulti) { this.dmgMulti = dmgMulti; }

	public void multiplyDmgMulti(float factor) { this.dmgMulti *= factor; }

	public float getDmgBonus() { return dmgBonus; }

	public void setDmgBonus(float dmgBonus) { this.dmgBonus = dmgBonus; }

	public float getAccMulti() { return accMulti; }

	public void setAccMulti(float accMulti) { this.accMulti = accMulti; }
}
