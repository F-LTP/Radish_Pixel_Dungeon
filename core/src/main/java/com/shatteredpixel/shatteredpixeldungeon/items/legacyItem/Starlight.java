package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemArmor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

// DoggingDog on 20250523
// 星辉
public class Starlight extends LegacyItemArmor {

    {
        image = ItemSpriteSheet.STARLIGHT;
    }

    public Starlight() {
        super(1);
    }

    @Override
    public int DRMin(int lvl){
        return 4 + 2 * lvl;
    }

    @Override
    public int DRMax(int lvl){
        return 8 + Math.max(2 * lvl + augment.defenseFactor(2 * lvl), 2 * lvl);
    }

    @Override
    protected ArmorBuff buff( ) {
        return new Venus();
    }

    public class Venus extends ArmorBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }

        public static final int DISTANCE	= 3;

        private int BOOST = 0;

        public int getBOOST(){
            BOOST = Math.max(0,(target.HT-target.HP)) /Math.max(1,(target.HT/10));
            return BOOST;
        }

        @Override
        public String icon() {
            return BuffIndicator.BLESS;
        }

    }
}
