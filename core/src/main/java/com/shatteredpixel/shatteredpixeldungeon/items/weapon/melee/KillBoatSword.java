package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class KillBoatSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.KILL_BOAT;
        tier = 5;
    }
    public boolean delayAttack = false;

    @Override
    public int min(int lvl) {
        return 10 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 60 + lvl * 8;
    }

}
