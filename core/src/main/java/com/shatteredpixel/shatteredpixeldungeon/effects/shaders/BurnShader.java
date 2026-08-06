/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
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

package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * Slice&Dice 风格燃烧着色器。
 */
public class BurnShader extends GlslShaderScript {

    private Uniform uBounds;
    private Uniform uBurnProgress;
    private Uniform uRandom;

    public BurnShader() {
        super("burn");
        uBounds = uniform("uBounds");
        uBurnProgress = uniform("uBurnProgress");
        uRandom = uniform("uRandom");
    }

    public void setBounds(float x, float y, float width, float height) {
        uBounds.value4f(x, y, width, height);
    }

    public void setBurnProgress(float progress) {
        uBurnProgress.value1f(progress);
    }

    public void setRandom(float random) {
        uRandom.value1f(random);
    }
}