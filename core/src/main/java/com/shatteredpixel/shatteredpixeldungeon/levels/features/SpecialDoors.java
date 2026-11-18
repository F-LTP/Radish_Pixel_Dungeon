package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.events.DoorEnteredEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.DoorLeftEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.events.GameEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;

/**
 * 特殊门的注册、判定、进入/离开和存档无关的状态维护。
 */
public final class SpecialDoors {

	private static final int[] DX = {-1, 0, 1, -1, 1, -1, 0, 1};
	private static final int[] DY = {-1, -1, -1, 0, 0, 1, 1, 1};

	private SpecialDoors() {
	}

	public static SpecialDoor add(Level level, int cell) {
		SpecialDoor door = level.specialDoors.get(cell);
		if (door == null) {
			door = new SpecialDoor(cell);
			level.specialDoors.put(cell, door);
			apply(level, cell);
		}
		return door;
	}

	/** 注册一扇条件出现门；初始为墙。 */
	public static SpecialDoor addToggle(Level level, int cell, String triggerId) {
		SpecialDoor door = add(level, cell);
		door.toggled(triggerId);
		apply(level, cell);
		return door;
	}

	public static SpecialDoor get(Level level, int cell) {
		return level == null ? null : level.specialDoors.get(cell);
	}

	public static void remove(Level level, int cell) {
		SpecialDoor door = level.specialDoors.remove(cell);
		if (door != null && (level.map[cell] == Terrain.DOOR || level.map[cell] == Terrain.OPEN_DOOR)) {
			level.passable[cell] = true;
		}
	}

	/** 关闭状态下的门是否绝对不可通行；开启的 OPEN_DOOR 不算。 */
	public static boolean isBlocking(Level level, int cell) {
		SpecialDoor door = get(level, cell);
		return door != null && level.map[cell] == Terrain.DOOR && door.blocking();
	}

	/** 返回给玩家看的阻断文案，不阻断时返回 null。 */
	public static String blockMessage(Char ch, int cell) {
		SpecialDoor door = get(Dungeon.level, cell);
		if (door == null || Dungeon.level.map[cell] != Terrain.DOOR) {
			return null;
		}
		return blockMessage(door);
	}

	public static String blockMessage(SpecialDoor door) {
		String key = door == null ? null : door.blockedMessageKey();
		return key == null ? null : Messages.get(SpecialDoors.class, key);
	}

	public static int bitFor(int dx, int dy) {
		if (dx < -1 || dx > 1 || dy < -1 || dy > 1 || (dx == 0 && dy == 0)) {
			return 0;
		}
		if (dy < 0) {
			return dx < 0 ? 1 : dx > 0 ? 4 : 2;
		}
		if (dy > 0) {
			return dx < 0 ? 32 : dx > 0 ? 128 : 64;
		}
		return dx < 0 ? 8 : 16;
	}

	public static boolean allowsOpen(Level level, int doorCell, int fromCell) {
		SpecialDoor door = get(level, doorCell);
		if (door == null || !door.oneWay) {
			return false;
		}
		int width = level.width();
		int bit = bitFor((fromCell % width) - (doorCell % width),
				(fromCell / width) - (doorCell / width));
		return bit != 0 && (door.fromMask & bit) != 0;
	}

	/**
	 * 从 seedCell 所在区域泛洪，收集与门格相邻的可达格，得到允许开启的邻格掩码。
	 * 0 或全部 8 个邻格说明门没有把区域分开，返回 0 让调用方按普通门处理。
	 */
	public static int sideMask(Level level, int doorCell, int seedCell) {
		int len = level.length();
		if (seedCell < 0 || seedCell >= len || seedCell == doorCell
				|| (!level.passable[seedCell] && !level.avoid[seedCell])) {
			return 0;
		}

		boolean[] seen = new boolean[len];
		int[] queue = new int[len];
		int head = 0;
		int tail = 0;
		queue[tail++] = seedCell;
		seen[seedCell] = true;

		int width = level.width();
		while (head < tail) {
			int c = queue[head++];
			for (int i = 0; i < 8; i++) {
				int n = c + DY[i] * width + DX[i];
				if (n < 0 || n >= len || n == doorCell || seen[n]) {
					continue;
				}
				if (level.passable[n] || level.avoid[n]) {
					seen[n] = true;
					queue[tail++] = n;
				}
			}
		}

		int mask = 0;
		for (int i = 0; i < 8; i++) {
			int n = doorCell + DY[i] * width + DX[i];
			if (n >= 0 && n < len && seen[n]) {
				mask |= 1 << i;
			}
		}

		if (mask == 0 || Integer.bitCount(mask) == 8) {
			DeviceCompat.log("SpecialDoors", "Invalid one-way mask at " + doorCell + ": " + mask);
			return 0;
		}
		return mask;
	}

	/** 从允许的邻格中选一个离英雄最近的作为靠近目标。 */
	public static int approachCell(Level level, int doorCell) {
		SpecialDoor door = get(level, doorCell);
		if (door == null || !door.oneWay) {
			return doorCell;
		}
		int width = level.width();
		int best = -1;
		int bestDist = Integer.MAX_VALUE;
		for (int i = 0; i < 8; i++) {
			if ((door.fromMask & (1 << i)) == 0) {
				continue;
			}
			int n = doorCell + DY[i] * width + DX[i];
			if (n < 0 || n >= level.length() || !level.passable[n]
					|| Actor.findChar(n) != null) {
				continue;
			}
			int dist = level.distance(Dungeon.hero.pos, n);
			if (dist < bestDist) {
				best = n;
				bestDist = dist;
			}
		}
		return best < 0 ? doorCell : best;
	}

	public static void open(Level level, int cell) {
		Level.set(cell, Terrain.OPEN_DOOR, level);
		if (level == Dungeon.level) {
			GameScene.updateMap(cell);
		}
	}

	public static void onEntered(Char ch, int cell) {
		SpecialDoor door = get(Dungeon.level, cell);
		if (door == null) {
			return;
		}
		EventManager.emit(new DoorEnteredEvent(ch, door, cell));
		if (!door.signalOnEnter.isEmpty()) {
			GateTriggers.signal(door.signalOnEnter);
		}
	}

	public static void onLeft(Char ch, int cell, int toCell) {
		SpecialDoor door = get(Dungeon.level, cell);
		if (door == null) {
			return;
		}
		EventManager.emit(new DoorLeftEvent(ch, door, cell));

		if (!door.oneShot || door.sealed || ch != Dungeon.hero) {
			return;
		}
		//封门前清空门格：否则怪物或掉落物会永久阻止一次性门封死
		if (!clearCell(Dungeon.level, cell, ch, toCell)) {
			DeviceCompat.log("SpecialDoors", "No free cell to eject occupants at " + cell);
			return;
		}

		door.sealed = true;
		Level.set(cell, Terrain.LOCKED_DOOR, Dungeon.level);
		SealedDoorVisual vis = new SealedDoorVisual();
		vis.pos(cell, Dungeon.level);
		Dungeon.level.customTiles.add(vis);
		GameScene.updateMap(cell);
	}

	/** 把门格上的其他角色和物品弹到相邻空格；exclude 为发起移动的角色。 */
	private static boolean clearCell(Level level, int cell, Char exclude, int avoid) {
		for (Char other : Actor.chars()) {
			if (other == exclude || other.pos != cell) {
				continue;
			}
			int target = ejectTarget(level, cell, avoid, false);
			if (target < 0) {
				return false;
			}
			other.pos = target;
			if (other.sprite != null) {
				other.sprite.place(target);
			}
		}

		Heap heap = level.heaps.get(cell);
		if (heap != null) {
			int target = ejectTarget(level, cell, avoid, true);
			if (target < 0) {
				return false;
			}
			level.heaps.remove(cell);
			heap.pos = target;
			level.heaps.put(target, heap);
			if (heap.sprite != null) {
				heap.sprite.place(target);
			}
		}
		return true;
	}

	/** 优先弹向 avoid 的反方向，其次 8 邻格，最后两格内的空格。 */
	private static int ejectTarget(Level level, int cell, int avoid, boolean forHeap) {
		int width = level.width();
		int cellX = cell % width;
		int cellY = cell / width;

		if (avoid >= 0) {
			int dx = Integer.signum(cellX - avoid % width);
			int dy = Integer.signum(cellY - avoid / width);
			int n = cell + dy * width + dx;
			if ((dx != 0 || dy != 0) && ejectFree(level, n, cell, avoid, forHeap)) {
				return n;
			}
		}

		for (int i = 0; i < 8; i++) {
			int n = cell + DY[i] * width + DX[i];
			if (ejectFree(level, n, cell, avoid, forHeap)) {
				return n;
			}
		}

		for (int dy = -2; dy <= 2; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				if (Math.abs(dx) < 2 && Math.abs(dy) < 2) {
					continue;
				}
				int x = cellX + dx;
				int y = cellY + dy;
				if (x < 0 || y < 0 || x >= width || y >= level.height()) {
					continue;
				}
				int n = y * width + x;
				if (ejectFree(level, n, cell, avoid, forHeap)) {
					return n;
				}
			}
		}
		return -1;
	}

	private static boolean ejectFree(Level level, int cell, int doorCell, int avoid, boolean forHeap) {
		if (cell < 0 || cell >= level.length() || cell == doorCell || cell == avoid) {
			return false;
		}
		if (!level.passable[cell] || level.solid[cell]) {
			return false;
		}
		if (Actor.findChar(cell) != null) {
			return false;
		}
		return !forHeap || level.heaps.get(cell) == null;
	}

	public static void onEvent(Level level, GameEvent event) {
		boolean unlockedAny = false;
		for (SpecialDoor door : new ArrayList<>(level.specialDoors.values())) {
			if (door.toggle) {
				if (GateTriggers.matches(door.triggerId, event)) {
					toggle(level, door);
				}
				continue;
			}
			if (!door.locked() || !GateTriggers.matches(door.triggerId, event)) {
				continue;
			}
			door.unlocked = true;
			unlockedAny = true;
			if (level.map[door.cell] == Terrain.DOOR) {
				if ("explosion".equals(door.triggerId)) {
					Level.set(door.cell, Terrain.OPEN_DOOR, level);
					if (level == Dungeon.level) {
						GameScene.updateMap(door.cell);
					}
				}
				apply(level, door.cell);
			}
		}
		if (unlockedAny) {
			GLog.p(Messages.get(SpecialDoors.class, "opened"));
		}
	}

	private static void toggle(Level level, SpecialDoor door) {
		if (door.appeared) {
			if (!clearCell(level, door.cell, null, -1)) {
				DeviceCompat.log("SpecialDoors", "No free cell to seal toggle door at " + door.cell);
				return;
			}
			door.appeared = false;
			if (level.map[door.cell] != Terrain.WALL) {
				Level.set(door.cell, Terrain.WALL, level);
			}
		} else {
			door.appeared = true;
			Level.set(door.cell, Terrain.DOOR, level);
			apply(level, door.cell);
		}
		if (level == Dungeon.level) {
			refresh(level, door.cell);
		}
	}

	/** 墙体/门格的外形变化需要连同 8 邻格一起重绘。 */
	private static void refresh(Level level, int cell) {
		level.cleanWalls();
		GameScene.updateMap(cell);
		int width = level.width();
		for (int i = 0; i < 8; i++) {
			int n = cell + DY[i] * width + DX[i];
			if (n >= 0 && n < level.length()) {
				GameScene.updateMap(n);
			}
		}
		Dungeon.observe();
	}

	/** 爆炸不能摧毁特殊门，避免刚被炸开的门又被同一次爆炸烧掉。 */
	public static boolean isIndestructible(int cell) {
		return Dungeon.level != null && Dungeon.level.specialDoors.containsKey(cell);
	}

	public static void applyLevelFlags(Level level) {
		if (level == null || level.specialDoors == null) {
			return;
		}
		for (SpecialDoor door : level.specialDoors.values()) {
			apply(level, door.cell);
		}
	}

	private static void apply(Level level, int cell) {
		SpecialDoor door = level.specialDoors.get(cell);
		if (door == null) {
			return;
		}
		int terrain = level.map[cell];
		if (door.toggle) {
			if (door.appeared) {
				if (terrain == Terrain.WALL) {
					Level.set(cell, Terrain.DOOR, level);
				}
				if (level.map[cell] == Terrain.DOOR) {
					level.passable[cell] = !door.blocking();
				} else if (level.map[cell] == Terrain.OPEN_DOOR) {
					level.passable[cell] = true;
				}
			} else if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR
					|| terrain == Terrain.LOCKED_DOOR) {
				Level.set(cell, Terrain.WALL, level);
			}
			return;
		}
		if (door.sealed) {
			if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR) {
				Level.set(cell, Terrain.LOCKED_DOOR, level);
			}
		} else if (terrain == Terrain.DOOR) {
			level.passable[cell] = !door.blocking();
		} else if (terrain == Terrain.OPEN_DOOR) {
			level.passable[cell] = true;
		}
	}

	public static String describe(SpecialDoor door) {
		if (door == null) {
			return "normal";
		}
		if (door.toggle) {
			return door.appeared ? "conditional-door" : "conditional-wall";
		}
		if (door.oneShot) {
			return "single-use";
		}
		if (door.locked()) {
			return "explosion".equals(door.triggerId) ? "blast" : "conditional";
		}
		if (door.oneWay) {
			return "one-way";
		}
		return "normal";
	}
}
