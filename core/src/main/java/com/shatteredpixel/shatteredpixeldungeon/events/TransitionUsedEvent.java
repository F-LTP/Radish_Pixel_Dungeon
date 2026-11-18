package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStair;

/** Emitted after a level transition has been successfully activated. */
public class TransitionUsedEvent extends GameEvent {

	private final Hero hero;
	private final LevelTransition transition;
	private final SpecialStair stair;

	public TransitionUsedEvent(Hero hero, LevelTransition transition, SpecialStair stair) {
		this.hero = hero;
		this.transition = transition;
		this.stair = stair;
	}

	public Hero getHero() { return hero; }

	public LevelTransition getTransition() { return transition; }

	/** May be null for ordinary stairs. */
	public SpecialStair getStair() { return stair; }
}
