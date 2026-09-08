package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;

/**
 * 英雄死亡事件
 * 当英雄死亡时触发
 */
public class HeroDeathEvent extends GameEvent {
    private final Hero hero;
    private final Char killer;
    private final DeathCause cause;

    public HeroDeathEvent(Hero hero, Char killer, DeathCause cause) {
        this.hero = hero;
        this.killer = killer;
        this.cause = cause;
    }

    public Hero getHero() {
        return hero;
    }

    public Char getKiller() {
        return killer;
    }

    public DeathCause getCause() {
        return cause;
    }

    public enum DeathCause {
        COMBAT,         // 战斗死亡
        POISON,         // 中毒
        FIRE,           // 火焰
        ELECTRICITY,    // 电击
        FALLING,        // 掉落
        STARVATION,     // 饥饿
        OTHER           // 其他原因
        ;

        /**
         * 根据伤害类型推导死亡原因。
         * 无法归类的类型（普通物理/魔法战斗伤害等）返回 null，
         * 由调用方结合凶手是否为 Char 决定 COMBAT 或 OTHER。
         */
        public static DeathCause fromDamageType(DamageType type) {
            if (type == null) return null;
            switch (type) {
                case POISON:
                case OOZE:
                    return POISON;
                case FIRE:
                case BURNING_STATUS:
                case TOXIC:
                case CORROSIVE:
                    return FIRE;
                case LIGHTNING:
                    return ELECTRICITY;
                case FALL:
                case CHASM:
                    return FALLING;
                case HUNGER:
                    return STARVATION;
                default:
                    return null;
            }
        }
    }
}
