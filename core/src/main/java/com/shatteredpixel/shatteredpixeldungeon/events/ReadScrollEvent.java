package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import org.jetbrains.annotations.NotNull;

/**
 * Scroll.readAnimation 发布，早于卷轴天赋结算；不可取消、卷轴已消耗。
 * 匿名卷轴（无序魔典）常规路径不发，双读路径一律发。
 */
public class ReadScrollEvent extends GameEvent {

	private final Hero hero;
	private final Scroll scroll;

	public ReadScrollEvent(@NotNull Hero hero, @NotNull Scroll scroll) {
		this.hero = hero;
		this.scroll = scroll;
	}

	public @NotNull Hero getUser() { return hero; }

	public @NotNull Scroll getScroll() { return scroll; }
}
