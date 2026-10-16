package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/**
 * Event fired before hero attempts to move.
 * Can be cancelled to prevent movement.
 * Subscribers can cancel this event to block movement (e.g., wheelchair requirement, rooted, etc.)
 */
public class BeforeHeroMoveEvent extends GameEvent {
    private final Hero hero;
    private final int fromCell;
    private final int toCell;

    public BeforeHeroMoveEvent(Hero hero, int fromCell, int toCell) {
        this.hero = hero;
        this.fromCell = fromCell;
        this.toCell = toCell;
    }

    public Hero getHero() { return hero; }
    public int getFromCell() { return fromCell; }
    public int getToCell() { return toCell; }
}