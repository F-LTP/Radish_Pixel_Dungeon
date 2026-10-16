package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;

public class ThrowPotionEvent extends GameEvent {
    private final Hero hero;
    private final Potion potion;
    private final int cell;

    public ThrowPotionEvent(Hero hero, Potion potion, int cell) {
        this.hero = hero;
        this.potion = potion;
        this.cell = cell;
    }

    public Hero getUser() { return hero; }
    public Potion getPotion() { return potion; }
    public int getCell() { return cell; }
}