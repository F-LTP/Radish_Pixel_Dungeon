package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
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
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return Math.max(3 + lvl + augment.defenseFactor(lvl),1+lvl/2);
        }

        return 5 + 2 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 1 + lvl / 2;
        }

        return 2 + 3 * lvl / 2;
    }
    public class myMask extends ArmorBuff {

    }
}
