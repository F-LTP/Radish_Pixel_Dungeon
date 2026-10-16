package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

public class HeroMoveEvent extends GameEvent {
    private final Hero hero;
    private final int fromCell;
    private final int toCell;

    public HeroMoveEvent(Hero hero, int fromCell, int toCell) {
        this.hero = hero;
        this.fromCell = fromCell;
        this.toCell = toCell;
    }

    public Hero getHero() { return hero; }
    public int getFromCell() { return fromCell; }
    public int getToCell() { return toCell; }
}