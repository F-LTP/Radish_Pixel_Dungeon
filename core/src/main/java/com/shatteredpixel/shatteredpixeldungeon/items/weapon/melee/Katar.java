package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Katar extends MeleeWeapon{
    {
        image = ItemSpriteSheet.KATAR;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1.3f;

        tier = 2;
        DLY = 0.5f; //2x speed
    }

    @Override
    public int max(int lvl) {
        return  7 +
                lvl * 3 / 2;  //+2 per level, down from +4
    }
}
