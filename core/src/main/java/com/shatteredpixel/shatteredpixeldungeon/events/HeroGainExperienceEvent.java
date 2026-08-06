package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/**
 * 英雄获得经验事件
 * 当英雄获得经验时触发
 */
public class HeroGainExperienceEvent extends GameEvent {
    private final Hero hero;
    private final int exp;
    private final Class<?> source;

    public HeroGainExperienceEvent(Hero hero, int exp, Class<?> source) {
        this.hero = hero;
        this.exp = exp;
        this.source = source;
    }

    public Hero getHero() { return hero; }
    public int getExp() { return exp; }
    public Class<?> getSource() { return source; }
}