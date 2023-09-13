package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class BladeShield extends MeleeWeapon{

    {
        image = ItemSpriteSheet.BLADESHIELD;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1f;

        tier = 2;
    }

    @Override
    public int max(int lvl) {
        return  9 +     //10 base, down from 20
                lvl * 3 / 2;                   //+2 per level, down from +4
    }


    @Override
    public int defenseFactor( Char owner ) {
        return 3+buffedLvl();     //4 extra defence, plus 2 per level;
    }

    public String statsInfo(){
        if (isIdentified()){
            return Messages.get(this, "stats_desc", 3+buffedLvl());
        } else {
            return Messages.get(this, "typical_stats_desc", 3);
        }
    }
}
