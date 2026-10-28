package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;

/**
 * 角色未经过减免计算的原始伤害事件（伤害减免计算前触发）。
 * <p>
 * 在伤害结算开始、任何减免/修正之前发布，{@link #getDamage()} 为传入的原始伤害值。
 * 用于需要感知"命中前原始伤害"的监听者。
 * <p>
 * 若需要实际造成的最终伤害，请订阅 {@link CharFinalDamageEvent}（减免计算后）。
 */
public class CharUnprocedDamageEvent extends GameEvent {

    private final Char target;
    private final Char attacker;
    private final Object source;
    private final int damage;
    private final DamageType type;

    public CharUnprocedDamageEvent(Char target, Char attacker, Object source, int damage, DamageType type) {
        this.target = target;
        this.attacker = attacker;
        this.source = source;
        this.damage = damage;
        this.type = type;
    }

    public Char getTarget() { return target; }

    /** 攻击者，若无法从来源推断则为 null。 */
    public Char getAttacker() { return attacker; }

    /** 原始伤害来源对象（武器、法杖、Buff等）。 */
    public Object getSource() { return source; }

    /** 减免计算前的原始伤害值。 */
    public int getDamage() { return damage; }

    public DamageType getType() { return type; }
}
