package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * earnExp 开头发布，早于升级判定与经验转化效果（此时 hero.exp 未含本次）。
 * AscensionChallenge 的经验不计入 hero.exp 但仍发布，用 source 区分。
 */
public class HeroGainExperienceEvent extends GameEvent {

	private final Hero hero;
	private final int exp;
	private final Class<?> source;

	public HeroGainExperienceEvent(@NotNull Hero hero, int exp, @Nullable Class<?> source) {
		this.hero = hero;
		this.exp = exp;
		this.source = source;
	}

	public @NotNull Hero getHero() { return hero; }

	public int getExp() { return exp; }

	public @Nullable Class<?> getSource() { return source; }
}
