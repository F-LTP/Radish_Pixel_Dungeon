package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GoldRadish extends Trinket {

    {
        image = ItemSpriteSheet.GOLD_RADISH;
    }

    @Override
    protected int upgradeEnergyCost() {
        return 20+10*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", fixedLevel(buffedLvl()));
        } else {
            return Messages.get(this, "stats_desc", fixedLevel(0));
        }
    }

    public int fixedLevel( int level ){
        switch (level){
            default:
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
        }
    }
}
