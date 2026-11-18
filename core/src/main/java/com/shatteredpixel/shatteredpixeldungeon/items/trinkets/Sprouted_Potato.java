package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Sprouted_Potato extends Trinket {

    {
        image = ItemSpriteSheet.SPOTOA;
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 7(13) -> 8(21) -> 9(30)
        return 6+level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc",
                    Messages.decimalFormat("#.##", 100 * hungerMultiplier(buffedLvl())),
                    Messages.decimalFormat("#.##", 100 * regenerationMultiplier(buffedLvl())));
        } else {
            return Messages.get(this, "typical_stats_desc",
                    Messages.decimalFormat("#.##", 100 * hungerMultiplier(0)),
                    Messages.decimalFormat("#.##", 100 * regenerationMultiplier(0)));
        }
    }

    public static float hungerMultiplier() {
        return hungerMultiplier(trinketLevel(Sprouted_Potato.class));
    }

    public static float hungerMultiplier(int level) {
        if (level <= -1){
            return 1;
        } else {
            return 3f - 0.5f*level;
        }
    }

    public static float regenerationMultiplier() {
        return regenerationMultiplier(trinketLevel(Sprouted_Potato.class));
    }

    public static float regenerationMultiplier(int level) {
//        if (level <= -1){
//            return 1;
//        } else {
//            return 0.1f + 0.1f*level;
//        }
        return 0.5f;
    }

    public static class Potato_Poison extends Buff implements Hero.Doom {

        protected float partialLevel = 0f;

        protected int level = 0;

        {
            type = buffType.NEGATIVE;
        }

        private static final String PARTIALLEVEL = "partiallevel";

        private static final String LEVEL = "level";

        private static final String POINT = "point";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( PARTIALLEVEL, partialLevel );
            bundle.put( LEVEL, level );

        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            partialLevel = bundle.getFloat( PARTIALLEVEL );
            level = bundle.getInt( LEVEL );
        }

        public void set(int setlevel) {level = setlevel;}

        public void harden(float point) {
            partialLevel += point;

            if (partialLevel>=1) {
                level += (int)partialLevel;
                partialLevel -= (int)partialLevel;
            }

            if (target instanceof Hero) {
                ((Hero) target).updateHT(false);
            }

            if (target.HT <= 0) {
                detach();

                target.HP = 0;

                target.die(this);
            }
        }

        public void reduce(float point) {
            partialLevel -= point;

            while (partialLevel < 0) {
                partialLevel += 1;
                level -= 1;
            }

            if (target instanceof Hero) {
                ((Hero) target).updateHT(false);
            }

            if (level <= 0) {
                detach();
            }
        }

        @Override
        public void detach() {
            level = 0;
            ((Hero) target).updateHT(false);

            super.detach();
        }

        @Override
        public String icon() {
            return BuffIndicator.POISON;
        }

        @Override
        public String desc() {
//            String desc = Messages.get(this, "desc", level);
            return Messages.get(this, "desc", level);
        }

        public int level() {return level;}

        @Override
        public void onDeath() {
            Dungeon.fail( this );
            GLog.n( Messages.get(this, "ondeath") );
        }
    }
}
