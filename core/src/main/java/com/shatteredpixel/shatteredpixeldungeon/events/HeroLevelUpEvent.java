package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.NotNull;

/**
 * 升级后发布（属性已成长）；连升多级只发一次，previousLevel 恒为 newLevel-1。
 * 满级转祝福不发布；只关心经验获取请订阅 HeroGainExperienceEvent。
 */
public class HeroLevelUpEvent extends GameEvent {

	private final Hero hero;
	private final int previousLevel;
	private final int newLevel;

	public HeroLevelUpEvent(@NotNull Hero hero, int previousLevel, int newLevel) {
		this.hero = hero;
		this.previousLevel = previousLevel;
		this.newLevel = newLevel;
	}

	public @NotNull Hero getHero() { return hero; }

	public int getPreviousLevel() { return previousLevel; }

	public int getNewLevel() { return newLevel; }
}
