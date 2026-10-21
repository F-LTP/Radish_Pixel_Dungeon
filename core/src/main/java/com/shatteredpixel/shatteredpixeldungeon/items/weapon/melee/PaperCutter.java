package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class PaperCutter extends MeleeWeapon {

	{
		image = ItemSpriteSheet.PAPER_CUTTER;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;

		tier = 1;
		DLY = 1f;
		RCH = 1;

		bones = false;
	}

	@Override
	public int min(int lvl) {
		return 1 + lvl;
	}

	@Override
	public int max(int lvl) {
		return 8 + 2 * lvl;
	}
}
