package com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;

public class OldSewerLevel extends SewerLevel {

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_OLD_SEWERS;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_A;
    }
}
