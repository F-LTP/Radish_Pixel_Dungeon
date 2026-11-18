package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;

/**
 * 循环门的外观：楼梯周围一圈光圈。
 */
public class CycleStairVisual extends CustomTilemap {

	{
		texture = Assets.Environment.CYCLE_STAIR;
		tileW = tileH = 1;
	}

	final int TEX_WIDTH = 16;

	public int slot = 0;

	public CycleStairVisual() {
	}

	public CycleStairVisual(int slot) {
		this.slot = slot;
	}

	@Override
	public void storeInBundle(com.watabou.utils.Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("slot", slot);
	}

	@Override
	public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
		super.restoreFromBundle(bundle);
		slot = bundle.getInt("slot");
	}

	@Override
	public Tilemap create() {
		Tilemap v = super.create();
		v.map(mapSimpleImage(0, 0, TEX_WIDTH), 1);
		return v;
	}

	@Override
	public String name(int tileX, int tileY) {
		if (slot < 0) {
			return Messages.get(this, "name");
		}
		return Messages.get(this, "name") + " " + (slot == 1 ? "B" : slot == 2 ? "C" : "A");
	}

	@Override
	public String desc(int tileX, int tileY) {
		String base = Messages.get(this, "desc");
		if (slot >= 0) {
			String a = slot == 1 ? "B" : slot == 2 ? "C" : "A";
			String b = slot == 1 ? "C" : slot == 2 ? "A" : "B";
			base += "\n\n" + Messages.get(this, "cycle_info", a, b);
		}
		return base;
	}
}