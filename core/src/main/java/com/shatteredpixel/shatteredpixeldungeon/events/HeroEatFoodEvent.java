package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 英雄进食事件
 * 当英雄进食时触发
 */
public class HeroEatFoodEvent extends GameEvent {
    private final Hero hero;
    private final float foodValue;
    private final Item foodSource;

    public HeroEatFoodEvent(Hero hero, float foodValue, Item foodSource) {
        this.hero = hero;
        this.foodValue = foodValue;
        this.foodSource = foodSource;
    }

    public Hero getHero() { return hero; }
    public float getFoodValue() { return foodValue; }
    public Item getFoodSource() { return foodSource; }
}