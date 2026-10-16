package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 预留事件：当前无发布点——EmergencyTalent 已在订阅但永不触发，接入前先确认该天赋预期 */
public class LevelChangeEvent extends GameEvent {

	public enum TransitionType {
		DESCEND,
		ASCEND,
		TELEPORT,
		FALL,
		PORTAL
	}

	private final Level previousLevel;
	private final Level newLevel;
	private final TransitionType transitionType;

	public LevelChangeEvent(@Nullable Level previousLevel, @NotNull Level newLevel,
	                        @NotNull TransitionType transitionType) {
		this.previousLevel = previousLevel;
		this.newLevel = newLevel;
		this.transitionType = transitionType;
	}

	public @Nullable Level getPreviousLevel() { return previousLevel; }

	public @NotNull Level getNewLevel() { return newLevel; }

	public @NotNull TransitionType getTransitionType() { return transitionType; }
}
