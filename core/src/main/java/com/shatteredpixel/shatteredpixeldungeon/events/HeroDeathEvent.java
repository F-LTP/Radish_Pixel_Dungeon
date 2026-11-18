package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 真正死亡路径（super.die 后、reallyDie 前）发布；Ankh 复活不发布——收到即本局结束。
 * killer 为伤害来源 Char（投射物回退 lastAttacker，非战斗为 null）；cause 由 lastDamageType 推导。
 */
public class HeroDeathEvent extends GameEvent {

	public enum DeathCause {
		COMBAT,
		POISON,
		FIRE,
		ELECTRICITY,
		FALLING,
		STARVATION,
		OTHER;

		/** 无法归类的类型（普通物理/魔法伤害等）返回 null，由调用方按凶手定 COMBAT/OTHER */
		public static DeathCause fromDamageType(DamageType type) {
			if (type == null) return null;
			switch (type) {
				case POISON:
				case OOZE:
					return POISON;
				case FIRE:
				case BURNING_STATUS:
				case TOXIC:
				case CORROSIVE:
					return FIRE;
				case LIGHTNING:
					return ELECTRICITY;
				case FALL:
				case CHASM:
					return FALLING;
				case HUNGER:
					return STARVATION;
				default:
					return null;
			}
		}
	}

	private final Hero hero;
	private final Char killer;
	private final DeathCause cause;

	public HeroDeathEvent(@NotNull Hero hero, @Nullable Char killer, @NotNull DeathCause cause) {
		this.hero = hero;
		this.killer = killer;
		this.cause = cause;
	}

	public @NotNull Hero getHero() { return hero; }

	public @Nullable Char getKiller() { return killer; }

	public @NotNull DeathCause getCause() { return cause; }
}
