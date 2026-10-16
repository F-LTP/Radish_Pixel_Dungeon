package com.shatteredpixel.shatteredpixeldungeon.custom;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HealingBlocked;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Dog;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroDeathEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 集中处理萝卜地牢怪物攻击相关的事件订阅。
 *
 * <p>命中附加效果（如 Dog 禁疗）由伤害事件安全表达。
 * 击杀文案中：有 Char 凶手的仍由各怪物内联打印（避免全游戏死亡路径
 * 文案来源不一致导致的重复/丢失）；无凶手（非 Char 来源，如中毒/火焰/
 * 坠落）的由伤害类型统一推导文案，集中在本类处理。</p>
 *
 * <p>订阅方法均为 {@code public static} 并标注 {@link SubscribeEvent}，
 * 由 event-processor 在编译期自动收集，无需手动注册。</p>
 */
public class RadishCombatEvents {

	/** 命中附加效果：恶犬攻击命中并实际造成伤害后，使目标禁疗。 */
	@SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
	public static void onDogHit(CharFinalDamageEvent event) {
		Char attacker = event.getAttacker();
		Char target = event.getTarget();
		if (attacker instanceof Dog && target.isAlive()) {
			HealingBlocked.block(target, HealingBlocked.DURATION);
		}
	}

	/**
	 * 死亡文案：仅处理无 Char 凶手、且由伤害类型可推导出明确原因的死亡
	 * （COMBAT/OTHER 仍保持各来源内联，避免重复打印）。
	 */
	@SubscribeEvent(event = HeroDeathEvent.class, priority = 0)
	public static void onHeroDeath(HeroDeathEvent event) {
		if (event.getKiller() != null) return;
		HeroDeathEvent.DeathCause cause = event.getCause();
		if (cause != null && cause != HeroDeathEvent.DeathCause.COMBAT
				&& cause != HeroDeathEvent.DeathCause.OTHER) {
			GLog.w( Messages.get( RadishCombatEvents.class, "death_" + cause.name().toLowerCase() ) );
		}
	}
}
