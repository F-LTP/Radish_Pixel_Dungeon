package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class PrisonArmor extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_PRISON;
    }
    public PrisonArmor() {
        super( 3 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new PrisonArmor.myMask();
    }
    @Override
    public int DRMax(int lvl){
        return 5 + 2 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        return 2 + 3 * lvl / 2;
    }
    public class myMask extends ArmorBuff {

    }
}
