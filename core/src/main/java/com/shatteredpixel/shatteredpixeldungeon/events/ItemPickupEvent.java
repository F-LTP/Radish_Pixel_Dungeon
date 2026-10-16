package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;

/** 预留事件：当前无发布点，订阅收不到回调（接入时同步更新 AGENTS.md 的无发布点清单） */
public class ItemPickupEvent extends GameEvent {

	private final Hero hero;
	private final Item item;

	public ItemPickupEvent(@NotNull Hero hero, @NotNull Item item) {
		this.hero = hero;
		this.item = item;
	}

	public @NotNull Hero getHero() { return hero; }

	public @NotNull Item getItem() { return item; }
}
