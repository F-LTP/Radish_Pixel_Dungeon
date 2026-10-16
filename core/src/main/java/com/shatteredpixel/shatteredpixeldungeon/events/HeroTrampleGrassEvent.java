package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.NotNull;

/** HighGrass.trample 开头、踩踏者为英雄时发布；freezeTrample 防重入期间不发布 */
public class HeroTrampleGrassEvent extends GameEvent {

	private final Hero hero;
	private final Level level;
	private final int cell;

	public HeroTrampleGrassEvent(@NotNull Hero hero, @NotNull Level level, int cell) {
		this.hero = hero;
		this.level = level;
		this.cell = cell;
	}

	public @NotNull Hero getHero() { return hero; }

	public @NotNull Level getLevel() { return level; }

	public int getCell() { return cell; }
}
