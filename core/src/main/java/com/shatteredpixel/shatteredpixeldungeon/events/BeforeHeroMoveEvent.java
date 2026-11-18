package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.NotNull;

/**
 * Hero.getCloser() 寻路每步发布（移动/拾取/交互/开门等 HeroAction 均经此）；cancel() 取消本步移动。
 * toCell 是本步寻路目标，非玩家点击的终点；层间传送等绕过 getCloser 的位移不触发。
 */
public class BeforeHeroMoveEvent extends GameEvent {

	private final Hero hero;
	private final int fromCell;
	private final int toCell;

	public BeforeHeroMoveEvent(@NotNull Hero hero, int fromCell, int toCell) {
		this.hero = hero;
		this.fromCell = fromCell;
		this.toCell = toCell;
	}

	public @NotNull Hero getHero() { return hero; }

	public int getFromCell() { return fromCell; }

	public int getToCell() { return toCell; }
}
