package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.events.GameEvent;

/**
 * 条件锁的触发规则。id 用于存档，matches 决定某个事件能否解锁。
 */
public interface GateTrigger {

	String id();

	boolean matches(GameEvent event);
}
