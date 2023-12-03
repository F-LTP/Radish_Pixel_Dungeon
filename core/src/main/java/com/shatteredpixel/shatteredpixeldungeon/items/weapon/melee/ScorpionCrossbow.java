package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ScorpionCrossbow extends MeleeWeapon{

    {
        image = ItemSpriteSheet.CROSSBOW_S;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1f;

        //check Dart.class for additional properties

        tier = 5;
    }

    @Override
    public int max(int lvl) {
        return  4*tier +
                lvl*(tier);
    }

    @Override
    public int min(int lvl) {
        return  tier + 1 +
                lvl;
    }
}
