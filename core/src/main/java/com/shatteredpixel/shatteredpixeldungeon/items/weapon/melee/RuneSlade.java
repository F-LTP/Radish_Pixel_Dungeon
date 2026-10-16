package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RuneSlade extends MeleeWeapon {

    {
        image = ItemSpriteSheet.RUNE_SLADE;
        tier = 2;
    }

    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        return 4 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 16 + lvl * 2;
    }

}
