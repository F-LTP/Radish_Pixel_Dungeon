package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/**
 * 英雄回合事件
 * 当英雄执行 act() 方法时触发，每个游戏回合触发一次
 */
public class HeroActEvent extends GameEvent {
    private final Hero hero;

    public HeroActEvent(Hero hero) {
        this.hero = hero;
    }

    public Hero getHero() { return hero; }
}
