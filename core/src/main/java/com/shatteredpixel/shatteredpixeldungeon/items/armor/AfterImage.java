package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class AfterImage extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_AFTERIMAGE;
    }

    public AfterImage() {
        super( 5 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new AfterImage.Blur();
    }
    @Override

    public int DRMax(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return Math.max(3 + 2 * lvl + augment.defenseFactor(lvl),2);
        }

        return 8 + 5 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 2;
        }

        return 4;
    }
    public class Blur extends ArmorBuff {
        public int dodges=0;
        @Override
        public boolean act(){
            if (dodges>=3){
                dodges-=3;
                Buff.affect(target,absoluteEvasion.class);
            }
            return super.act();
        }


        private static final String DODGES       = "dodges";

        @Override
        public void storeInBundle( Bundle bundle ) {
            bundle.put(DODGES,dodges);
            super.storeInBundle(bundle);
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle(bundle);
            if (bundle.contains(DODGES))
                dodges = bundle.getInt(DODGES);
            else
                dodges =0;
        }
    }
    public static class absoluteEvasion extends Buff{

    }
}
