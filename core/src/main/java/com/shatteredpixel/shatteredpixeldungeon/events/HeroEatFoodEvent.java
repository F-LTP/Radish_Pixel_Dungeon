package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Talent.onFoodEaten 开头发布——食物/号角/蜂蜜药剂的统一进食入口，早于 HEARTY_MEAL 等天赋结算。
 * 食物已消耗，不可取消。
 */
public class HeroEatFoodEvent extends GameEvent {

	private final Hero hero;
	private final float foodValue;
	private final Item foodSource;

	public HeroEatFoodEvent(@NotNull Hero hero, float foodValue, @NotNull Item foodSource) {
		this.hero = hero;
		this.foodValue = foodValue;
		this.foodSource = foodSource;
	}

	public @NotNull Hero getHero() { return hero; }

	public float getFoodValue() { return foodValue; }

	public @NotNull Item getFoodSource() { return foodSource; }
}
