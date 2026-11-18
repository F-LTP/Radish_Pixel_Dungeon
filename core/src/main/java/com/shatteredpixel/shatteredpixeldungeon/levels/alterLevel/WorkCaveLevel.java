package com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel;

public class WorkCaveLevel extends CavesLevel {

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_WORK_CAVES;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_C;
    }
}
