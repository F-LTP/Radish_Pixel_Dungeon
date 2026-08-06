package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/**
 * 英雄踩踏高草事件
 * 当英雄踩踏高草时触发
 */
public class HeroTrampleGrassEvent extends GameEvent {
    private final Hero hero;
    private final Level level;
    private final int cell;

    public HeroTrampleGrassEvent(Hero hero, Level level, int cell) {
        this.hero = hero;
        this.level = level;
        this.cell = cell;
    }

    public Hero getHero() { return hero; }
    public Level getLevel() { return level; }
    public int getCell() { return cell; }
}