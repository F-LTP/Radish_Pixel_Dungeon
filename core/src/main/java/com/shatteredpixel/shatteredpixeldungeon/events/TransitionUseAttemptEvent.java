package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStair;

/** Emitted before the hero activates a level transition; subscribers may cancel. */
public class TransitionUseAttemptEvent extends GameEvent {

	private final Hero hero;
	private final LevelTransition transition;
	private final SpecialStair stair;
	private String blockedMessage = "";

	public TransitionUseAttemptEvent(Hero hero, LevelTransition transition, SpecialStair stair) {
		this.hero = hero;
		this.transition = transition;
		this.stair = stair;
	}

	public Hero getHero() { return hero; }

	public LevelTransition getTransition() { return transition; }

	/** May be null for ordinary stairs. */
	public SpecialStair getStair() { return stair; }

	public String getBlockedMessage() { return blockedMessage; }

	public void block(String message) {
		this.blockedMessage = message;
		cancel();
	}
}
