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
    public String upgradeStat1(int level) {
        if (cursed && cursedKnown) level = Math.min(-1, level-3);
        return Integer.toString(level+1);
    }

    @Override
    public String upgradeStat2(int level) {
        if (cursed && cursedKnown) level = Math.min(-1, level-3);
        return Integer.toString(level+1);
    }

    @Override
    protected RingBuff buff( ) {
        return new KingUpdate();
    }

    public static int updateMultiplier( Char target ){
        return getBuffedBonus( target, KingUpdate.class );
    }

    public class KingUpdate extends RingBuff {
    }

}
