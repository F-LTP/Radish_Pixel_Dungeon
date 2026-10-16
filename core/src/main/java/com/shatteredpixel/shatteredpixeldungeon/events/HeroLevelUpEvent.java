package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/**
 * 英雄升级事件
 * 当英雄升级时触发
 */
public class HeroLevelUpEvent extends GameEvent {
    private final Hero hero;
    private final int previousLevel;
    private final int newLevel;

    public HeroLevelUpEvent(Hero hero, int previousLevel, int newLevel) {
        this.hero = hero;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    public Hero getHero() {
        return hero;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}
