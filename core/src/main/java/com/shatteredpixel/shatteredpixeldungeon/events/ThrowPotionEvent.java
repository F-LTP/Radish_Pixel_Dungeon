package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import org.jetbrains.annotations.NotNull;

/**
 * Potion.onThrow 末尾发布：落地即发（碎裂/落井/命中角色都算），投掷者非英雄不发；不可取消。
 * 喝药走 DrinkPotionEvent，两条路径互斥。
 */
public class ThrowPotionEvent extends GameEvent {

	private final Hero hero;
	private final Potion potion;
	private final int cell;

	public ThrowPotionEvent(@NotNull Hero hero, @NotNull Potion potion, int cell) {
		this.hero = hero;
		this.potion = potion;
		this.cell = cell;
	}

	public @NotNull Hero getUser() { return hero; }

	public @NotNull Potion getPotion() { return potion; }

	public int getCell() { return cell; }
}
