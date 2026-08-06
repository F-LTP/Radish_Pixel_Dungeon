package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 物品拾取事件
 * 当英雄拾取物品时触发
 */
public class ItemPickupEvent extends GameEvent {
    private final Hero hero;
    private final Item item;

    public ItemPickupEvent(Hero hero, Item item) {
        this.hero = hero;
        this.item = item;
    }

    public Hero getHero() {
        return hero;
    }

    public Item getItem() {
        return item;
    }
}
