package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SilverScaleArmor extends Armor {

    {
        image = ItemSpriteSheet.ARMOR_SILVERSCALE;
    }

    public SilverScaleArmor() {
        super( 3 );
    }

    @Override
    public int value() {
        if (seal != null) return 0;

        int price = 5;
        if (hasGoodGlyph()) {
            price *= 1.5;
        }
        if (cursedKnown && (cursed || hasCurseGlyph())) {
            price /= 2;
        }
        if (levelKnown && level() > 0) {
            price *= (level() + 1);
        }
        if (price < 1) {
            price = 1;
        }
        return price;
    }

}
