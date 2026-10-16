package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 物品使用事件
 * 当角色使用物品时触发
 */
public class ItemUseEvent extends GameEvent {
    private final Char user;
    private final Item item;
    private final Char target;

    public ItemUseEvent(Char user, Item item) {
        this(user, item, null);
    }

    public ItemUseEvent(Char user, Item item, Char target) {
        this.user = user;
        this.item = item;
        this.target = target;
    }

    public Char getUser() {
        return user;
    }

    public Item getItem() {
        return item;
    }

    public Char getTarget() {
        return target;
    }
}
