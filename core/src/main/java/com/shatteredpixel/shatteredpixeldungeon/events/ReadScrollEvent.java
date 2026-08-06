package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;

/**
 * 阅读卷轴事件
 * 当英雄阅读卷轴时触发
 */
public class ReadScrollEvent extends GameEvent {
    private final Hero hero;
    private final Scroll scroll;

    public ReadScrollEvent(Hero hero, Scroll scroll) {
        this.hero = hero;
        this.scroll = scroll;
    }

    public Hero getUser() { return hero; }
    public Scroll getScroll() { return scroll; }
}