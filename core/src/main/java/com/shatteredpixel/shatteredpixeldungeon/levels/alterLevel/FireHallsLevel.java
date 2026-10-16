package com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;

public class FireHallsLevel extends HallsLevel {

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_FIRE_HALLS;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_E;
    }
}
