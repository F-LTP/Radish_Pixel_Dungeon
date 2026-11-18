package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import org.jetbrains.annotations.NotNull;

/** Potion.drink 内、效果生效后发布；投掷路径见 ThrowPotionEvent */
public class DrinkPotionFinishEvent extends GameEvent {

	private final Hero hero;
	private final Potion potion;

	public DrinkPotionFinishEvent(@NotNull Hero hero, @NotNull Potion potion) {
		this.hero = hero;
		this.potion = potion;
	}

	public @NotNull Hero getUser() { return hero; }

	public @NotNull Potion getPotion() { return potion; }
}
