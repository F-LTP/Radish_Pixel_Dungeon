package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.DeviceCompat;

/**
 * 普通门格上的特殊规则。规则可组合：单向、一次性、条件锁。
 */
public class SpecialDoor implements Bundlable {

	public int cell = -1;

	/** 只允许从 fromMask 指定的邻格手动开启。 */
	public boolean oneWay;
	/** 8 位邻格掩码，位序与 PathFinder.NEIGHBOURS8 一致。 */
	public int fromMask;

	public boolean oneShot;
	public boolean sealed;

	public String triggerId = "";
	public boolean unlocked;

	/** 条件出现门：未满足时该格是墙，触发器每次命中在墙与门之间翻转。 */
	public boolean toggle;
	public boolean appeared;

	/** 角色进入这扇门时发布的具名信号，供其他条件门订阅。 */
	public String signalOnEnter = "";

	public SpecialDoor() {
	}

	public SpecialDoor(int cell) {
		this.cell = cell;
	}

	/** 允许从 seedCell 所在的整片区域手动开启。 */
	public SpecialDoor oneWayFromSide(Level level, int seedCell) {
		int mask = SpecialDoors.sideMask(level, cell, seedCell);
		oneWay = mask != 0;
		fromMask = mask;
		return this;
	}

	/** 直接指定允许的邻格；掩码为 0 或覆盖全部 8 个方向时按普通门处理。 */
	public SpecialDoor oneWayFromCells(Level level, int... cells) {
		int width = level.width();
		int mask = 0;
		for (int c : cells) {
			mask |= SpecialDoors.bitFor((c % width) - (cell % width), (c / width) - (cell / width));
		}
		if (mask == 0 || Integer.bitCount(mask) == 8) {
			DeviceCompat.log("SpecialDoor", "Invalid one-way mask at " + cell + ": " + mask);
			oneWay = false;
			fromMask = 0;
		} else {
			oneWay = true;
			fromMask = mask;
		}
		return this;
	}

	public SpecialDoor oneShot() {
		this.oneShot = true;
		return this;
	}

	public SpecialDoor locked(String triggerId) {
		this.triggerId = triggerId == null ? "" : triggerId;
		this.unlocked = false;
		return this;
	}

	/** 条件出现门：初始是墙，触发器每次命中在墙与门之间翻转。 */
	public SpecialDoor toggled(String triggerId) {
		this.toggle = true;
		this.appeared = false;
		this.triggerId = triggerId == null ? "" : triggerId;
		return this;
	}

	public SpecialDoor signalOnEnter(String signalId) {
		this.signalOnEnter = signalId == null ? "" : signalId;
		return this;
	}

	public boolean locked() {
		return !toggle && triggerId != null && !triggerId.isEmpty() && !unlocked;
	}

	public boolean blocking() {
		return sealed || oneWay || (toggle ? !appeared : locked());
	}

	public String blockedMessageKey() {
		if (toggle && !appeared) {
			return null;
		}
		if (sealed) {
			return "blocked_single_use";
		}
		if (locked()) {
			return "explosion".equals(triggerId) ? "blocked_bomb" : "blocked_condition";
		}
		if (oneWay) {
			return "blocked_one_way";
		}
		return null;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put("cell", cell);
		bundle.put("one_way", oneWay);
		bundle.put("from_mask", fromMask);
		bundle.put("one_shot", oneShot);
		bundle.put("sealed", sealed);
		bundle.put("trigger", triggerId);
		bundle.put("unlocked", unlocked);
		bundle.put("toggle", toggle);
		bundle.put("appeared", appeared);
		bundle.put("signal_on_enter", signalOnEnter);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		cell = bundle.getInt("cell");
		oneWay = bundle.getBoolean("one_way");
		fromMask = bundle.getInt("from_mask");
		oneShot = bundle.getBoolean("one_shot");
		sealed = bundle.getBoolean("sealed");
		triggerId = bundle.getString("trigger");
		if (triggerId == null) {
			triggerId = "";
		}
		unlocked = bundle.getBoolean("unlocked");
		toggle = bundle.getBoolean("toggle");
		appeared = bundle.getBoolean("appeared");
		signalOnEnter = bundle.getString("signal_on_enter");
		if (signalOnEnter == null) {
			signalOnEnter = "";
		}
	}
}
