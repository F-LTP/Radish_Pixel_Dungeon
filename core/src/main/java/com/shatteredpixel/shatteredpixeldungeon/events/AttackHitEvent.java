package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 近战攻击命中并完成伤害结算事件。
 * <p>
 * 在 {@code Char.attack(...)} 调用 {@code enemy.damage(attackDamage)} 之后、命中特效与
 * 血花之前发布。携带攻击者、防御者、本次实际生效伤害、{@link DamageInfo}、攻击者武器、
 * 防御者护甲，以及本次伤害投掷所用的 {@link Preparation}（未潜行则为 null）。
 * <p>
 * 订阅者按「谁持有该 buff / 物品」自行判断参与方向，不要把持有者当作玩家：
 * 智能怪物挑战（{@code Challenges.REAL_INTELLIGENCE}）下怪物也能持有玩家装备。
 * <p>
 * 优先级约定（数值越大越先执行）：命中后附带 debuff（-100 区间）→ 控制效果（-200 区间）
 * → 斩杀（-300 区间）→ 处决 / 暗杀（-400 区间），保持原有结算顺序。
 */
public class AttackHitEvent extends GameEvent {

    private final Char attacker;
    private final Char defender;

    private final int effectiveDamage;
    private final DamageInfo damage;

    private final Item attackerWeapon;
    private final Item defenderArmor;

    private final Preparation preparation;

    public AttackHitEvent(Char attacker, Char defender, int effectiveDamage, DamageInfo damage,
                          Item attackerWeapon, Item defenderArmor, Preparation preparation) {
        this.attacker = attacker;
        this.defender = defender;
        this.effectiveDamage = effectiveDamage;
        this.damage = damage;
        this.attackerWeapon = attackerWeapon;
        this.defenderArmor = defenderArmor;
        this.preparation = preparation;
    }

    /** 攻击方。 */
    public Char getAttacker() { return attacker; }

    /** 防御方。 */
    public Char getDefender() { return defender; }

    /** 本次实际生效伤害。 */
    public int getEffectiveDamage() { return effectiveDamage; }

    /** 本次攻击的伤害信息。 */
    public DamageInfo getDamage() { return damage; }

    /** 攻击者武器（若有），否则为 null。 */
    public Item getAttackerWeapon() { return attackerWeapon; }

    /** 防御者护甲（若有），否则为 null。 */
    public Item getDefenderArmor() { return defenderArmor; }

    /** 本次伤害投掷所用的潜行增益（若攻击者未潜行则为 null）。 */
    public Preparation getPreparation() { return preparation; }
}
