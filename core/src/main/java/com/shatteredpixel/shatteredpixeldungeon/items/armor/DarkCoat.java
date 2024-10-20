package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class DarkCoat extends Armor {

    {
        image = ItemSpriteSheet.ARMOR_BLACKCOAT;
    }

    public DarkCoat() {
        super( 4 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new DarkCoat.myPace();
    }
    @Override
    public int DRMax(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 4 + lvl * 3 / 2 + augment.defenseFactor(lvl);
        }

        return 6 + 4 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 1;
        }

        return 2 + lvl;
    }
    public class myPace extends ArmorBuff {

    }
}
