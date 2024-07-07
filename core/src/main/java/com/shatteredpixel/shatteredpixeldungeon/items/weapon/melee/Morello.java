package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Morello extends MeleeWeapon {

    {
        image = ItemSpriteSheet.MORELLO_BOOK;
        tier = 3;
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 16 + lvl * 2;
    }

}
