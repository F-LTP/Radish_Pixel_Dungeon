package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
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
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 6 + lvl * 3 / 2 + augment.defenseFactor(lvl);
        }

        return 10 + 4 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        return 0;
    }
    public class hexDodge extends ArmorBuff {
    }
}
