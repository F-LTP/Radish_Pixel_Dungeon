package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;

/**
 * 一次性门用完后留下的"画着红叉的门"：视觉是门，实际按墙处理（不可通行）。
 */
public class SealedDoorVisual extends CustomTilemap {

	{
		texture = Assets.Environment.SEALED_DOOR;
		tileW = tileH = 1;
	}

	final int TEX_WIDTH = 16;

	@Override
	public Tilemap create() {
		Tilemap v = super.create();
		v.map(mapSimpleImage(0, 0, TEX_WIDTH), 1);
		return v;
	}

	@Override
	public String name(int tileX, int tileY) {
		return Messages.get(this, "name");
	}

	@Override
	public String desc(int tileX, int tileY) {
		return Messages.get(this, "desc");
	}
}