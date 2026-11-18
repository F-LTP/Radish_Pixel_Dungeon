package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.events.ExplosionEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.GameEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.GateSignalEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.watabou.utils.DeviceCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件锁触发器的注册表。内置触发器只覆盖通用事件，内容侧可以注册自己的 id。
 */
public final class GateTriggers {

	private static final Map<String, GateTrigger> triggers = new HashMap<>();

	static {
		register("explosion", ExplosionEvent.class);
		register("any_hero_move", HeroMoveEvent.class);
	}

	private GateTriggers() {
	}

	public static void register(String id, final Class<? extends GameEvent> eventType) {
		register(new GateTrigger() {
			@Override
			public String id() {
				return id;
			}

			@Override
			public boolean matches(GameEvent event) {
				return eventType.isInstance(event);
			}
		});
	}

	public static void register(GateTrigger trigger) {
		if (trigger == null || trigger.id() == null || trigger.id().isEmpty()) {
			throw new IllegalArgumentException("Gate trigger needs a non-empty id");
		}
		triggers.put(trigger.id(), trigger);
	}

	/** 未知 id 返回 null，锁保持关闭。 */
	public static GateTrigger byId(String id) {
		if (id == null || id.isEmpty()) {
			return null;
		}
		GateTrigger trigger = triggers.get(id);
		if (trigger == null) {
			DeviceCompat.log("GateTriggers", "Unknown trigger id: " + id);
		}
		return trigger;
	}

	public static boolean matches(String id, GameEvent event) {
		if (id == null || id.isEmpty()) {
			return false;
		}
		//条件门也可以直接等待一个具名信号，例如 locked("signal:sewer.lever")
		if (id.startsWith("signal:")) {
			return event instanceof GateSignalEvent
					&& id.equals("signal:" + ((GateSignalEvent) event).getSignalId());
		}
		GateTrigger trigger = byId(id);
		return trigger != null && trigger.matches(event);
	}

	public static void signal(String signalId) {
		if (signalId != null && !signalId.isEmpty()) {
			EventManager.emit(new GateSignalEvent(signalId, -1));
		}
	}

	@SubscribeEvent(event = GameEvent.class, priority = -100)
	public static void onAnyEvent(GameEvent event) {
		if (event == null || Dungeon.level == null) {
			return;
		}
		SpecialDoors.onEvent(Dungeon.level, event);
		SpecialStairs.onEvent(Dungeon.level, event);
	}
}
