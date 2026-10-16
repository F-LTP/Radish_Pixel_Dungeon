package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.NotNull;

/** Hero.act() 开头，每回合一次；此时行动未决定、时间未消耗，不可取消 */
public class HeroActEvent extends GameEvent {

	private final Hero hero;

	public HeroActEvent(@NotNull Hero hero) {
		this.hero = hero;
	}

	public @NotNull Hero getHero() { return hero; }
}
