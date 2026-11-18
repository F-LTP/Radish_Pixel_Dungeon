package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;

/**
 * 楼梯端点的特殊规则。与特殊门分开建模，支持循环链。
 */
public class SpecialStair implements Bundlable {

	public int cell = -1;

	public boolean oneWay;
	public boolean oneShot;
	public boolean used;

	public String triggerId = "";
	public boolean unlocked;

	/** 这扇楼梯被使用后发布的具名信号，供其他条件门订阅。 */
	public String signalOnUse = "";

	/** 循环链 id；为空表示不是循环楼梯。 */
	public String chain = "";
	/** 0=A 1=B 2=C */
	public int slot;
	/** 同层落点；跨层时也是目标层的落点，-1 表示目标层入口。 */
	public int destCell = -1;
	/** 非空表示跨层。 */
	public String destBranch = "";
	public int destDepth;

	public SpecialStair() {
	}

	public SpecialStair(int cell) {
		this.cell = cell;
	}

	public boolean locked() {
		return triggerId != null && !triggerId.isEmpty() && !unlocked;
	}

	public boolean isCycle() {
		return chain != null && !chain.isEmpty();
	}

	public SpecialStair oneWay() {
		this.oneWay = true;
		return this;
	}

	public SpecialStair oneShot() {
		this.oneShot = true;
		return this;
	}

	public SpecialStair locked(String triggerId) {
		this.triggerId = triggerId == null ? "" : triggerId;
		this.unlocked = false;
		return this;
	}

	public SpecialStair signalOnUse(String signalId) {
		this.signalOnUse = signalId == null ? "" : signalId;
		return this;
	}

	public SpecialStair destCell(int destCell) {
		this.destCell = destCell;
		return this;
	}

	public SpecialStair dest(String branch, int depth, int destCell) {
		this.destBranch = branch == null ? "" : branch;
		this.destDepth = depth;
		this.destCell = destCell;
		return this;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put("cell", cell);
		bundle.put("one_way", oneWay);
		bundle.put("one_shot", oneShot);
		bundle.put("used", used);
		bundle.put("trigger", triggerId);
		bundle.put("unlocked", unlocked);
		bundle.put("signal_on_use", signalOnUse);
		bundle.put("chain", chain);
		bundle.put("slot", slot);
		bundle.put("dest_cell", destCell);
		bundle.put("dest_branch", destBranch);
		bundle.put("dest_depth", destDepth);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		cell = bundle.getInt("cell");
		oneWay = bundle.getBoolean("one_way");
		oneShot = bundle.getBoolean("one_shot");
		used = bundle.getBoolean("used");
		triggerId = bundle.getString("trigger");
		if (triggerId == null) {
			triggerId = "";
		}
		unlocked = bundle.getBoolean("unlocked");
		signalOnUse = bundle.getString("signal_on_use");
		if (signalOnUse == null) {
			signalOnUse = "";
		}
		chain = bundle.getString("chain");
		if (chain == null) {
			chain = "";
		}
		slot = bundle.getInt("slot");
		destCell = bundle.getInt("dest_cell");
		destBranch = bundle.getString("dest_branch");
		if (destBranch == null) {
			destBranch = "";
		}
		destDepth = bundle.getInt("dest_depth");
	}
}
