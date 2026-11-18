package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.GameEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

import java.util.ArrayList;

/**
 * 特殊楼梯的注册与激活规则。循环楼梯在这里按“踩上去就传送”处理。
 */
public final class SpecialStairs {

	/** 防止 A→B→C→A 连环触发。 */
	private static boolean teleporting;

	private SpecialStairs() {
	}

	public static SpecialStair add(Level level, LevelTransition transition) {
		return addAt(level, transition.cell());
	}

	public static SpecialStair addAt(Level level, int cell) {
		SpecialStair stair = level.specialStairs.get(cell);
		if (stair == null) {
			stair = new SpecialStair(cell);
			level.specialStairs.put(cell, stair);
		}
		return stair;
	}

	public static SpecialStair addCycle(Level level, int cell, String chain, int slot) {
		SpecialStair stair = addAt(level, cell);
		stair.chain = chain;
		stair.slot = slot;
		Painter.set(level, cell, Terrain.EXIT);
		addRing(level, cell, slot);
		return stair;
	}

	public static SpecialStair get(Level level, int cell) {
		return level == null ? null : level.specialStairs.get(cell);
	}

	public static SpecialStair of(Level level, LevelTransition transition) {
		return transition == null ? null : get(level, transition.cell());
	}

	public static String blockMessage(SpecialStair stair) {
		if (stair == null) {
			return null;
		}
		if (stair.oneWay) {
			return Messages.get(SpecialStairs.class, "blocked_one_way");
		}
		if (stair.oneShot && stair.used) {
			return Messages.get(SpecialStairs.class, "blocked_single_use");
		}
		if (stair.locked()) {
			return Messages.get(SpecialStairs.class,
					"explosion".equals(stair.triggerId) ? "blocked_bomb" : "blocked_condition");
		}
		if (stair.isCycle() && activeSlot(stair.chain) != stair.slot) {
			return Messages.get(SpecialStairs.class, "blocked_cycle", slotName(activeSlot(stair.chain)));
		}
		return null;
	}

	public static boolean usable(SpecialStair stair) {
		return blockMessage(stair) == null;
	}

	public static int activeSlot(String chain) {
		return Dungeon.gateChains.getOrDefault(chain, 0);
	}

	public static String slotName(int slot) {
		switch (slot % 3) {
			case 0: return "A";
			case 1: return "B";
			default: return "C";
		}
	}

	/** 英雄踩到特殊楼梯格时调用，返回 true 表示已经处理。 */
	public static boolean onStep(Hero hero, int cell) {
		if (teleporting || Dungeon.level == null) {
			return false;
		}
		SpecialStair stair = Dungeon.level.specialStairs.get(cell);
		if (stair == null || (stair.destBranch.isEmpty() && stair.destCell < 0)) {
			return false;
		}

		String blocked = blockMessage(stair);
		if (blocked != null) {
			GLog.w(blocked);
			return true;
		}

		if (!stair.destBranch.isEmpty()) {
			advance(stair);
			if (stair.oneShot) {
				stair.used = true;
			}
			hero.curAction = null;
			hero.sprite.interruptMotion();
			GLog.i(teleportMessage(stair));

			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			InterlevelScene.returnBranchId = stair.destBranch;
			InterlevelScene.returnDepth = stair.destDepth;
			InterlevelScene.returnPos = stair.destCell >= 0 ? stair.destCell : -1;
			Game.switchScene(InterlevelScene.class);
			return true;
		}

		if (stair.destCell < 0 || stair.destCell >= Dungeon.level.length()) {
			GLog.w(Messages.get(SpecialStairs.class, "invalid_dest"));
			return true;
		}
		if (!Dungeon.level.passable[stair.destCell] && !Dungeon.level.avoid[stair.destCell]) {
			GLog.w(Messages.get(SpecialStairs.class, "blocked_dest"));
			return true;
		}
		if (Actor.findChar(stair.destCell) != null) {
			GLog.w(Messages.get(SpecialStairs.class, "occupied_dest"));
			return true;
		}

		advance(stair);
		if (stair.oneShot) {
			stair.used = true;
		}

		hero.curAction = null;
		hero.sprite.interruptMotion();

		teleporting = true;
		try {
			hero.pos = stair.destCell;
			hero.sprite.place(stair.destCell);
			Dungeon.level.occupyCell(hero);
			Dungeon.observe();
			GameScene.updateMap();
		} finally {
			teleporting = false;
		}
		GLog.i(teleportMessage(stair));
		return true;
	}

	private static String teleportMessage(SpecialStair stair) {
		if (stair.isCycle()) {
			return Messages.get(SpecialStairs.class, "used",
					slotName(stair.slot), slotName(stair.slot + 1));
		}
		return Messages.get(SpecialStairs.class, "teleported");
	}

	private static void advance(SpecialStair stair) {
		if (stair.isCycle()) {
			Dungeon.gateChains.put(stair.chain, (stair.slot + 1) % 3);
		}
	}

	public static void onUsed(LevelTransition transition) {
		SpecialStair stair = of(Dungeon.level, transition);
		if (stair == null) {
			return;
		}
		if (stair.oneShot) {
			stair.used = true;
		}
		if (!stair.signalOnUse.isEmpty()) {
			GateTriggers.signal(stair.signalOnUse);
		}
	}

	public static void onEvent(Level level, GameEvent event) {
		boolean unlockedAny = false;
		for (SpecialStair stair : new ArrayList<>(level.specialStairs.values())) {
			if (!stair.locked() || !GateTriggers.matches(stair.triggerId, event)) {
				continue;
			}
			stair.unlocked = true;
			unlockedAny = true;
		}
		if (unlockedAny) {
			GLog.p(Messages.get(SpecialStairs.class, "opened"));
		}
	}

	public static void addRing(Level level, int cell, int slot) {
		if (level == null) {
			return;
		}
		for (CustomTilemap ct : level.customTiles) {
			if (ct instanceof CycleStairVisual
					&& ct.tileX == cell % level.width()
					&& ct.tileY == cell / level.width()) {
				return;
			}
		}
		CycleStairVisual vis = new CycleStairVisual(slot);
		vis.pos(cell, level);
		level.customTiles.add(vis);
		if (level == Dungeon.level) {
			GameScene.updateMap();
		}
	}

	public static String describe(SpecialStair stair) {
		if (stair == null) {
			return "normal";
		}
		if (stair.isCycle()) {
			return "cycle";
		}
		if (stair.oneShot) {
			return "single-use";
		}
		if (stair.locked()) {
			return "explosion".equals(stair.triggerId) ? "blast" : "conditional";
		}
		if (stair.oneWay) {
			return "one-way";
		}
		return "normal";
	}
}
