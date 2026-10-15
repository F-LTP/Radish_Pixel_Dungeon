package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/**
 * 攻击前事件。
 * <p>
 * 在 {@code Char.attack(...)} 的最开头、任何命中判定与伤害构建之前发布，
 * 早于 FOV 校验与无敌判定。订阅者可借此在本次攻击真正发生前替换或取消它。
 * <p>
 * {@link #cancel()} 后 {@code attack()} 会直接返回 {@code true}（视为该次行动已被消耗，
 * 不会再结算任何命中与伤害）。典型用途：影舞者破隐把普通攻击替换为突袭选点。
 * <p>
 * {@link #getDmgMulti()} / {@link #getDmgBonus()} / {@link #getAccMulti()} 为可变值，
 * 订阅者可按需乘算（例如充能 buff 提升 30% 伤害），修改会影响后续的伤害构建与命中判定。
 * <p>
 * 优先级约定：替换 / 取消类最高（+1000），伤害乘区类次之（+500）。
 */
public class PreAttackEvent extends GameEvent {

    private final Char attacker;
    private final Char defender;

    private float dmgMulti;
    private float dmgBonus;
    private float accMulti;

    public PreAttackEvent(Char attacker, Char defender, float dmgMulti, float dmgBonus, float accMulti) {
        this.attacker = attacker;
        this.defender = defender;
        this.dmgMulti = dmgMulti;
        this.dmgBonus = dmgBonus;
        this.accMulti = accMulti;
    }

    /** 攻击方。 */
    public Char getAttacker() { return attacker; }

    /** 防御方。 */
    public Char getDefender() { return defender; }

    /** 本次攻击的伤害乘区（可变）。 */
    public float getDmgMulti() { return dmgMulti; }

    public void setDmgMulti(float dmgMulti) { this.dmgMulti = dmgMulti; }

    /** 在现有伤害乘区上再乘一个系数。 */
    public void multiplyDmgMulti(float factor) { this.dmgMulti *= factor; }

    /** 本次攻击的伤害加值（可变）。 */
    public float getDmgBonus() { return dmgBonus; }

    public void setDmgBonus(float dmgBonus) { this.dmgBonus = dmgBonus; }

    /** 本次攻击的命中乘区（可变）。 */
    public float getAccMulti() { return accMulti; }

    public void setAccMulti(float accMulti) { this.accMulti = accMulti; }
}
