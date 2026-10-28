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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * 取消攻击冷却追踪器
 * 基础50回合，天赋可减少：+1=40, +2=33, +3=25
 */
public class CancelAttackCooldown extends FlavourBuff {

    {
        type = buffType.NEUTRAL;
    }

    public static final float BASE_DURATION = 50f;

    /**
     * 根据天赋等级获取冷却时间
     */
    public static float getDuration() {
        if (Dungeon.hero == null || Dungeon.hero.heroClass != HeroClasses.MOONLIGHT) {
            return BASE_DURATION;
        }
        int points = Dungeon.hero.pointsInTalent(Talent.WONT_LOSE);
        switch (points) {
            case 1: return 40f;
            case 2: return 33f;
            case 3: return 25f;
            default: return BASE_DURATION;
        }
    }

    @Override
    public String icon() {
        return BuffIndicator.TIME;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0.5f, 0.5f, 1f); // 蓝色调
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (getDuration() - visualcooldown()) / getDuration());
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(cooldown()));
    }
}