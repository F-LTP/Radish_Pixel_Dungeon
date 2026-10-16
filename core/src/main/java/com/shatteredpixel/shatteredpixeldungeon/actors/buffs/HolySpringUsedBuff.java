/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

/**
 * 神圣泉水已使用记录Buff
 * 记录月华英雄是否已转化知识之泉和生命之泉
 * 这个Buff不显示图标，只用于存储数据
 */
public class HolySpringUsedBuff extends Buff {

    // 是否已转化知识之泉
    public boolean awarenessUsed = false;
    // 是否已转化生命之泉
    public boolean healthUsed = false;

    {
        type = buffType.NEUTRAL;
    }

    @Override
    public String icon() {
        return BuffIndicator.NONE; // 不显示图标
    }

    @Override
    public void tintIcon(Image icon) {
        // 不显示
    }

    @Override
    public boolean act() {
        spend(TICK);
        return true;
    }

    private static final String AWARENESS_USED = "awareness_used";
    private static final String HEALTH_USED = "health_used";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(AWARENESS_USED, awarenessUsed);
        bundle.put(HEALTH_USED, healthUsed);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        awarenessUsed = bundle.getBoolean(AWARENESS_USED);
        healthUsed = bundle.getBoolean(HEALTH_USED);
    }

    // 标记知识之泉已转化
    public void markAwarenessUsed() {
        awarenessUsed = true;
    }

    // 标记生命之泉已转化
    public void markHealthUsed() {
        healthUsed = true;
    }

    // 检查是否还能转化知识之泉
    public boolean canTransformAwareness() {
        return !awarenessUsed;
    }

    // 检查是否还能转化生命之泉
    public boolean canTransformHealth() {
        return !healthUsed;
    }
}