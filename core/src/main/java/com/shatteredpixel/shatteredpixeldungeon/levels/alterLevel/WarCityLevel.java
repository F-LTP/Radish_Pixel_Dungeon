package com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;

public class WarCityLevel extends CityLevel {

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_WAR_CITY;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_D;
    }
}
