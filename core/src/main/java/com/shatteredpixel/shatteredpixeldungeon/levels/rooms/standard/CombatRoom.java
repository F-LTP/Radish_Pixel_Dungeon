package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 示例战斗房：作为普通房间生成，英雄进入后锁住它的全部门，房内敌人全灭后恢复。
 */
public class CombatRoom extends EmptyRoom {

	private int[] doorCells = new int[0];
	private int[] guardIds = new int[0];
	private boolean started;
	private boolean cleared;

	@Override
	public boolean canMerge(Level level, Room other, Point p, int mergeTerrain) {
		return false;
	}

	@Override
	public int mobSpawnWeight() {
		return 0;
	}

	@Override
	public void paint(Level level) {
		super.paint(level);

		ArrayList<Door> doors = new ArrayList<>(connected.values());
		doorCells = new int[doors.size()];
		for (int i = 0; i < doors.size(); i++) {
			Door door = doors.get(i);
			door.set(Door.Type.UNLOCKED);
			doorCells[i] = door.x + door.y * level.width();
		}

		for (int cell : doorCells) {
			SpecialDoor door = SpecialDoors.add(level, cell);
			door.triggerId = triggerId();
			door.unlocked = true;
		}

		spawnGuards(level);
	}

	private void spawnGuards(Level level) {
		ArrayList<Integer> ids = new ArrayList<>();
		int count = Random.IntRange(2, 4);
		for (int i = 0; i < count; i++) {
			int cell = -1;
			for (int tries = 0; tries < 30; tries++) {
				int c = level.pointToCell(random(1));
				if (level.map[c] == Terrain.EMPTY && level.findMob(c) == null) {
					cell = c;
					break;
				}
			}
			if (cell < 0) {
				break;
			}
			Mob mob = level.createMob();
			mob.pos = cell;
			level.mobs.add(mob);
			ids.add(mob.id());
		}
		guardIds = new int[ids.size()];
		for (int i = 0; i < guardIds.length; i++) {
			guardIds[i] = ids.get(i);
		}
	}

	private String triggerId() {
		return "signal:combat:" + left + "," + top;
	}

	private void setDoorState(Level level, boolean locked) {
		if (doorCells.length == 0) {
			return;
		}
		for (int cell : doorCells) {
			SpecialDoor door = SpecialDoors.get(level, cell);
			if (door == null) {
				door = SpecialDoors.add(level, cell);
				door.triggerId = triggerId();
			}
			if (locked && level.map[cell] == Terrain.OPEN_DOOR) {
				Level.set(cell, Terrain.DOOR, level);
				if (level == Dungeon.level) {
					GameScene.updateMap(cell);
				}
			}
			door.unlocked = !locked;
		}
		SpecialDoors.applyLevelFlags(level);
	}

	private boolean hasLivingGuard(Level level) {
		for (Mob mob : level.mobs) {
			if (!mob.isAlive()) {
				continue;
			}
			if (isGuard(mob)) {
				return true;
			}
			//分裂/新生成的敌人没有 guardId，按房间范围补判
			if (mob.alignment == Char.Alignment.ENEMY && contains(level.cellToPoint(mob.pos))) {
				return true;
			}
		}
		return false;
	}

	private boolean isGuard(Mob mob) {
		int id = mob.id();
		for (int guardId : guardIds) {
			if (id == guardId) {
				return true;
			}
		}
		return false;
	}

	/** 包含门格所在的边界，避免敌人站在门口时被漏算。 */
	private boolean contains(Point p) {
		return p.x >= left && p.x <= right && p.y >= top && p.y <= bottom;
	}

	private void startCombat() {
		if (started || cleared || Dungeon.level == null) {
			return;
		}
		started = true;
		setDoorState(Dungeon.level, true);
		checkCleared(Dungeon.level);
	}

	private void checkCleared(Level level) {
		if (!started || cleared) {
			return;
		}
		if (!hasLivingGuard(level)) {
			cleared = true;
			setDoorState(level, false);
		}
	}

	private static CombatRoom roomAt(Level level, int cell) {
		if (!(level instanceof RegularLevel) || cell < 0) {
			return null;
		}
		Point p = level.cellToPoint(cell);
		for (Room room : ((RegularLevel) level).rooms()) {
			if (room instanceof CombatRoom && ((CombatRoom) room).contains(p)) {
				return (CombatRoom) room;
			}
		}
		return null;
	}

	@SubscribeEvent(event = HeroMoveEvent.class)
	public static void onHeroMove(HeroMoveEvent event) {
		CombatRoom room = roomAt(Dungeon.level, event.getToCell());
		if (room != null) {
			room.startCombat();
			room.checkCleared(Dungeon.level);
		}
	}

	@SubscribeEvent(event = HeroActEvent.class)
	public static void onHeroAct(HeroActEvent event) {
		CombatRoom room = roomAt(Dungeon.level, event.getHero().pos);
		if (room != null) {
			room.startCombat();
			room.checkCleared(Dungeon.level);
		}
	}

	@SubscribeEvent(event = CharFinalDamageEvent.class)
	public static void onFinalDamage(CharFinalDamageEvent event) {
		if (Dungeon.level == null || !(event.getTarget() instanceof Mob) || event.getHpAfter() > 0) {
			return;
		}
		CombatRoom room = roomAt(Dungeon.level, event.getTarget().pos);
		if (room != null) {
			room.checkCleared(Dungeon.level);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("door_cells", doorCells);
		bundle.put("guard_ids", guardIds);
		bundle.put("started", started);
		bundle.put("cleared", cleared);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		doorCells = bundle.getIntArray("door_cells");
		if (doorCells == null) {
			doorCells = new int[0];
		}
		guardIds = bundle.getIntArray("guard_ids");
		if (guardIds == null) {
			guardIds = new int[0];
		}
		started = bundle.getBoolean("started");
		cleared = bundle.getBoolean("cleared");
	}

	@Override
	public void onLevelLoad(Level level) {
		super.onLevelLoad(level);
		for (int cell : doorCells) {
			SpecialDoor door = SpecialDoors.get(level, cell);
			if (door == null) {
				door = SpecialDoors.add(level, cell);
			}
			door.triggerId = triggerId();
			door.unlocked = !started || cleared;
		}
		SpecialDoors.applyLevelFlags(level);

		if (started && !cleared && !hasLivingGuard(level)) {
			cleared = true;
			setDoorState(level, false);
		}
	}
}
