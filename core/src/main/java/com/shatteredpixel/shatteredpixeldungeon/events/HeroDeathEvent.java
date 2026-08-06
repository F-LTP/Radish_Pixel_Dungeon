package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

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
    }
}
