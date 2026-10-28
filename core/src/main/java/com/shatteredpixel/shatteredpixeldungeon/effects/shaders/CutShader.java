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
 * Slice&Dice 风格切割着色器。
 */
public class CutShader extends GlslShaderScript {

    private Uniform uCutLine;
    private Uniform uCutAlpha;
    private Uniform uCutSide;

    public CutShader() {
        super("cut");
        uCutLine = uniform("uCutLine");
        uCutAlpha = uniform("uCutAlpha");
        uCutSide = uniform("uCutSide");
    }

    public void setCutLine(float startX, float startY, float dirX, float dirY) {
        uCutLine.value4f(startX, startY, dirX, dirY);
    }

    public void setCutProgress(float progress) {
        uCutAlpha.value1f(progress);
    }

    public void setCutSide(int side) {
        uCutSide.value1f((float) side);
    }
}