package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemRing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class LunarCorona extends LegacyItemRing {
    {
        icon = ItemSpriteSheet.Icons.RING_WEALTH;
    }
    @Override
    protected RingBuff buff( ) {
        return new Phase();
    }

    public class Phase extends RingBuff {
        {
            type = buffType.NEUTRAL;
            announced = true;
        }

        private boolean Waxing = true;
        private int PhaseInc = 0;

        private static final String WAXING	= "waxing";
        private static final String PHASEINC	= "phaseinc";

        public boolean isWaxing(){
            return Waxing;
        }
        @Override
        public boolean act() {
            PhaseInc++;
            if(PhaseInc >= 50){
                PhaseInc = 0;
                Waxing = !Waxing;
            }
            return super.act();
        }

        @Override
        public String icon() {
            return BuffIndicator.COMBO;
        }

        public class Wax{};
        public class Wan{};

        @Override
        public String desc() {
            return Messages.get(this, "desc", Waxing?Messages.get(new Wax(),"name"):Messages.get(new Wan(),"name"));
        }

    }
}
