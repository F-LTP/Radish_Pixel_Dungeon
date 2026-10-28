package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;

public class DrinkPotionEvent extends GameEvent {
    private final Hero hero;
    private final Potion potion;

    public DrinkPotionEvent(Hero hero, Potion potion) {
        this.hero = hero;
        this.potion = potion;
    }

    public Hero getUser() { return hero; }
    public Potion getPotion() { return potion; }
}