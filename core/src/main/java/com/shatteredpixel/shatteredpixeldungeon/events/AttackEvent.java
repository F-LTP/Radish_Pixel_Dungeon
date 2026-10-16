package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 近战攻击事件。
 * <p>
 * 在普通近战攻击命中（hit 判定通过）后、伤害结算（defenseProc / 伤害管线）之前发布。
 * 携带攻击者、防御者、本次攻击伤害，以及双方武器（如有）与防御者护甲（如有）。
 * <p>
 * 订阅者可通过 {@link #cancel()} 取消本次攻击——取消后原攻击伤害不再结算。
 * 该机制常用于"反弹"类效果（如 Rlyeh）：取消攻击并对攻击者施加反弹伤害。
 */
public class AttackEvent extends GameEvent {

    private final Char attacker;
    private final Char defender;
    private final int damage;

    private final Item attackerWeapon;
    private final Item defenderWeapon;
    private final Item defenderArmor;

    public AttackEvent(Char attacker, Char defender, int damage,
                       Item attackerWeapon, Item defenderWeapon, Item defenderArmor) {
        this.attacker = attacker;
        this.defender = defender;
        this.damage = damage;
        this.attackerWeapon = attackerWeapon;
        this.defenderWeapon = defenderWeapon;
        this.defenderArmor = defenderArmor;
    }

    /** 攻击者。 */
    public Char getAttacker() { return attacker; }

    /** 防御者。 */
    public Char getDefender() { return defender; }

    /** 本次攻击的伤害值（减免计算前）。 */
    public int getDamage() { return damage; }

    /** 攻击者武器（若有），否则为 null。 */
    public Item getAttackerWeapon() { return attackerWeapon; }

    /** 防御者武器（若有），否则为 null。 */
    public Item getDefenderWeapon() { return defenderWeapon; }

    /** 防御者护甲（若有），否则为 null。 */
    public Item getDefenderArmor() { return defenderArmor; }
}
