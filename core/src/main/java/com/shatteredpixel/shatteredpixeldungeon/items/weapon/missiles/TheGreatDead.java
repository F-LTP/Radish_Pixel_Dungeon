package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TheGreatDead  extends MissileWeapon {
    {
        image = ItemSpriteSheet.THROWING_STONE;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        bones = false;

        tier = 1;
        baseUses = 1000;
        sticky = false;
    }
    @Override
    public int value() {
        return super.value()/2; //half normal value
    }


    @Override
    public int min() {
        return 5;
    }
    @Override
    public int max() {
        return 10;
    }
}
