package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CrabArmor extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_CRAB;
    }
    public CrabArmor() {
        super( 2 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new CrabArmor.likeCrab();
    }

    @Override
    public int DRMax(int lvl){
        return 2 + Math.max( 2 * lvl + augment.defenseFactor(lvl), lvl);
    }

    @Override
    public int DRMin(int lvl){
        return 2 + lvl;
    }
    public class likeCrab extends ArmorBuff {

    }
}
