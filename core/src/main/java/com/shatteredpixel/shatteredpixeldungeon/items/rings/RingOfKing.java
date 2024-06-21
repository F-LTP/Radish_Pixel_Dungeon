package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfKing extends Ring {

    {
        icon = ItemSpriteSheet.Icons.RING_KING;
    }

    public String statsInfo() {
        if (isIdentified()){
            return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.15f, soloBuffedBonus()) - 1f)));
        } else {
            return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(15f));
        }
    }

    public static float updateMultiplier( Char target ){
        return (float)Math.pow(1.15, getBuffedBonus(target, KingUpdate.class));
    }

    @Override
    protected RingBuff buff( ) {
        return new RingOfKing.KingUpdate();
    }

    public class KingUpdate extends RingBuff {
    }

}
