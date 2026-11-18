package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.NotNull;

/** Hero.move() 完成后发布，不可取消（拦移动用 BeforeHeroMoveEvent）；直改 hero.pos 的位移不触发 */
public class HeroMoveEvent extends GameEvent {

	private final Hero hero;
	private final int fromCell;
	private final int toCell;

	public HeroMoveEvent(@NotNull Hero hero, int fromCell, int toCell) {
		this.hero = hero;
		this.fromCell = fromCell;
		this.toCell = toCell;
	}

	public @NotNull Hero getHero() { return hero; }

	public int getFromCell() { return fromCell; }

	public int getToCell() { return toCell; }
}
