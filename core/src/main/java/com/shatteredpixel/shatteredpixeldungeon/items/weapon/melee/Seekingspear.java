package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Seekingspear extends MeleeWeapon{
    //private static ItemSprite.Glowing RED = new ItemSprite.Glowing( 0x660022 );
    {
        image = ItemSpriteSheet.SEEKING;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 0.9f;

        tier = 4;
        RCH=2;
    }
    @Override
    public int min(int lvl) {
        return  8+lvl;
    }
    @Override
    public int max(int lvl) {
        return  4*tier +    //16 base, down from 25
                lvl*(tier);   //scaling down
    }
}
