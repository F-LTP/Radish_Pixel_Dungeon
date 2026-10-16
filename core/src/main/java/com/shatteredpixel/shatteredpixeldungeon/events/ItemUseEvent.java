package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 预留事件：当前无发布点；药水/卷轴已有专属事件，接入时勿重复发布 */
public class ItemUseEvent extends GameEvent {

	private final Char user;
	private final Item item;
	private final Char target;

	public ItemUseEvent(@NotNull Char user, @NotNull Item item) {
		this(user, item, null);
	}

	public ItemUseEvent(@NotNull Char user, @NotNull Item item, @Nullable Char target) {
		this.user = user;
		this.item = item;
		this.target = target;
	}

	public @NotNull Char getUser() { return user; }

	public @NotNull Item getItem() { return item; }

	public @Nullable Char getTarget() { return target; }
}
