package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfBenediction extends Ring {
    {
        icon = ItemSpriteSheet.Icons.POTION_EXP;
    }

    public String statsInfo() {
        if (isIdentified()){
            return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.2f, soloBuffedBonus()) - 1f)));
        } else {
            return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(20f));
        }
    }

    @Override
    protected Ring.RingBuff buff( ) {
        return new RingOfBenediction.Benediction();
    }

    public static float periodMultiplier( Char target ){
        return (float) Math.pow( 1.2, getBuffedBonus(target, RingOfBenediction.Benediction.class));
    }

    public class Benediction extends Ring.RingBuff {
    }
}
