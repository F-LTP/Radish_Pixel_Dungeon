package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 近战攻击未命中事件。
 * <p>
 * 在 {@code Char.attack(...)} 的命中判定失败、进入未命中分支后发布（早于未命中音效与
 * 防御方文字提示）。携带攻击者、防御者，以及双方武器与防御方护甲（如有）。
 * <p>
 * 订阅者按「谁持有该物品 / 该 buff」自行判断参与方向，不要把持有者当作玩家：
 * 智能怪物挑战（{@code Challenges.REAL_INTELLIGENCE}）下怪物也能持有玩家装备。
 * <p>
 * 优先级约定：职业专属效果（+100）优先于通用装备效果（0）。
 */
public class AttackMissEvent extends GameEvent {

    private final Char attacker;
    private final Char defender;

    private final Item attackerWeapon;
    private final Item defenderWeapon;
    private final Item defenderArmor;

    public AttackMissEvent(Char attacker, Char defender,
                           Item attackerWeapon, Item defenderWeapon, Item defenderArmor) {
        this.attacker = attacker;
        this.defender = defender;
        this.attackerWeapon = attackerWeapon;
        this.defenderWeapon = defenderWeapon;
        this.defenderArmor = defenderArmor;
    }

    /** 攻击方。 */
    public Char getAttacker() { return attacker; }

    /** 防御方。 */
    public Char getDefender() { return defender; }

    /** 攻击者武器（若有），否则为 null。 */
    public Item getAttackerWeapon() { return attackerWeapon; }

    /** 防御者武器（若有），否则为 null。 */
    public Item getDefenderWeapon() { return defenderWeapon; }

    /** 防御者护甲（若有），否则为 null。 */
    public Item getDefenderArmor() { return defenderArmor; }
}
