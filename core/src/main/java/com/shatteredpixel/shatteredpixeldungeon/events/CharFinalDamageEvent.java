package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 角色最终伤害事件（伤害减免计算后触发）。
 * <p>
 * 在最终伤害结算完成、HP 实际扣减之后发布，{@link #getDamage()} 为目标实际损失的生命值
 * （已扣除护盾、抗性、免疫、圣盾、追加伤害等全部减免/修正）。
 * <p>
 * 与 {@link CharUnprocedDamageEvent}（减免计算前，携带原始伤害）配合使用。
 */
public class CharFinalDamageEvent extends GameEvent {

    private final Char target;
    private final Char attacker;
    private final Object source;
    private final Item sourceItem;
    private final int damage;
    private final DamageType type;

    public CharFinalDamageEvent(Char target, Char attacker, Object source, int damage, DamageType type) {
        this(target, attacker, source, null, damage, type);
    }

    public CharFinalDamageEvent(Char target, Char attacker, Object source, Item sourceItem, int damage, DamageType type) {
        this.target = target;
        this.attacker = attacker;
        this.source = source;
        this.sourceItem = sourceItem;
        this.damage = damage;
        this.type = type;
    }

    public Char getTarget() { return target; }

    /** 攻击者，若无法从来源推断则为 null。 */
    public Char getAttacker() { return attacker; }

    /** 原始伤害来源对象（武器、法杖、Buff等）。 */
    public Object getSource() { return source; }

    /** 造成伤害的装备（武器/法杖等），若无法推断则为 null。 */
    public Item getSourceItem() { return sourceItem; }

    /** 最终造成的实际伤害（HP 扣减量）。 */
    public int getDamage() { return damage; }

    public DamageType getType() { return type; }
}
