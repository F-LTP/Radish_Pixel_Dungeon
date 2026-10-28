package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CloakofGreyFeather extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_GREYFEATHER;
    }

    public CloakofGreyFeather() {
        super( 4 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new CloakofGreyFeather.hexDodge();
    }
    @Override
    public int DRMax(int lvl){
        return 10 + 4 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        return 0;
    }
    public class hexDodge extends ArmorBuff {
    }
}
