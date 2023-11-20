package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
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
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return Math.max(1 + lvl + augment.defenseFactor(lvl),1);
        }
        return 2 + Math.max( 2 * lvl + augment.defenseFactor(lvl), lvl);
    }
    @Override
    public int DRMin(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 1;
        }
        return 2 + lvl;
    }
    public class likeCrab extends ArmorBuff {

    }
}
