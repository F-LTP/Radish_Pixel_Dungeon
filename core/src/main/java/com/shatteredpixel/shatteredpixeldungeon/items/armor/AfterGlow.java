package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class AfterGlow extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_AFTERGLOW;
    }

    public AfterGlow() {
        super( 5 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new AfterGlow.Warmth();
    }
    @Override

    public int DRMax(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return Math.max(8 + lvl + augment.defenseFactor(lvl),lvl);
        }

        return 15 + 3 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return lvl;
        }

        return 2 * lvl;
    }
    public class Warmth extends ArmorBuff {
        public float storeHealth=0f;
        @Override
        public boolean act(){
            if (storeHealth>=1){
                int preHp=target.HP;
                target.HP+=(int)storeHealth;
                storeHealth-=(int)storeHealth;
                if (target.HP>target.HT) target.HP=target.HT;
                target.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", target.HP-preHp);
            }
            return super.act();
        }
        public void getWarmth(){
            storeHealth+=2+0.2f*buffedLvl();
        }
        private static final String STOREHEALTH       = "store_health";

        @Override
        public void storeInBundle( Bundle bundle ) {
            bundle.put(STOREHEALTH,storeHealth);
            super.storeInBundle(bundle);
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle(bundle);
            if (bundle.contains(STOREHEALTH))
                storeHealth = bundle.getFloat(STOREHEALTH);
            else
                storeHealth =0;
        }

    }
}
