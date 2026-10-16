package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RiverCrystal extends Trinket {

    {
        image = ItemSpriteSheet.RIVER_GLASS;
    }


    @Override
    protected int upgradeEnergyCost() {
        return 10+5*level();
    }

    // 判定次数：+0/1/2/3 → 3/2/3/2
    public int judgeTimes() {
        int[] times = {3, 2, 3, 2};
        return times[Math.min(level(), 3)];
    }

    // 虚拟升级数：+0/1/2/3 → 1/1/2/2
    public int virtualLevel() {
        return (level() + 2) / 2;
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", virtualLevel());
        } else {
            return Messages.get(this, "typical_stats_desc", 1);
        }

    }
}
