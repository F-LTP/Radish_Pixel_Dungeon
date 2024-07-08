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
            return Messages.get(this, "stats", soloBonus(), soloBonus());
        } else {
            return Messages.get(this, "typical_stats", 1, new DecimalFormat("#.##").format(1f));
        }
    }

    @Override
    protected RingBuff buff( ) {
        return new KingUpdate();
    }

    public static int updateMultiplier( Char target ){
        return getBonus( target, KingUpdate.class );
    }

    public class KingUpdate extends RingBuff {
    }

}
