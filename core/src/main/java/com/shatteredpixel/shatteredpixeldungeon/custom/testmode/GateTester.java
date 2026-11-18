package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStair;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStairs;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * 测试工具：选择地图上的一扇门/楼梯，循环切换它的特殊规则。
 */
public class GateTester extends TestItem {

	private static final String AC_CYCLE = "CYCLE";

	{
		image = ItemSpriteSheet.RECLAIM_TRAP;
		defaultAction = AC_CYCLE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_CYCLE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_CYCLE.equals(action)) {
			return;
		}

		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null) {
					return;
				}
				apply(cell);
			}

			@Override
			public String prompt() {
				return Messages.get(GateTester.class, "prompt");
			}
		});
	}

	private void apply(int cell) {
		LevelTransition transition = Dungeon.level.getTransition(cell);
		if (transition != null) {
			cycleStair(transition);
			return;
		}

		int terrain = Dungeon.level.map[cell];
		if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR) {
			cycleDoor(cell);
			return;
		}

		GLog.w(Messages.get(this, "no_gate"));
	}

	private void cycleStair(LevelTransition transition) {
		SpecialStair stair = SpecialStairs.of(Dungeon.level, transition);
		if (stair == null) {
			stair = SpecialStairs.add(Dungeon.level, transition).oneWay();
		} else if (stair.oneWay) {
			stair.oneWay = false;
			stair.oneShot = true;
		} else if (stair.oneShot) {
			stair.oneShot = false;
			stair.locked("explosion");
		} else if ("explosion".equals(stair.triggerId)) {
			stair.locked("any_hero_move");
		} else {
			Dungeon.level.specialStairs.remove(stair.cell);
			stair = null;
		}
		GLog.i(Messages.get(this, "switched", SpecialStairs.describe(stair)));
	}

	private void cycleDoor(int cell) {
		SpecialDoor door = SpecialDoors.get(Dungeon.level, cell);
		if (door == null) {
			door = SpecialDoors.add(Dungeon.level, cell).oneWayFromSide(Dungeon.level, Dungeon.hero.pos);
		} else if (door.oneWay) {
			door.oneWay = false;
			door.fromMask = 0;
			door.oneShot = true;
		} else if (door.oneShot) {
			door.oneShot = false;
			door.locked("explosion");
		} else if ("explosion".equals(door.triggerId)) {
			door.locked("any_hero_move");
		} else {
			SpecialDoors.remove(Dungeon.level, cell);
			door = null;
		}
		SpecialDoors.applyLevelFlags(Dungeon.level);
		GameScene.updateMap();
		GLog.i(Messages.get(this, "switched", SpecialDoors.describe(door)));
	}
}
