package com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;

public class BloodPrisonLevel extends PrisonLevel {

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_BLOOD_PRISON;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_B;
    }
}
