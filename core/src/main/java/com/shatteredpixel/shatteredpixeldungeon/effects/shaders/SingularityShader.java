/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * 奇点收缩着色器。
 */
public class SingularityShader extends GlslShaderScript {

    private Uniform uBounds;
    private Uniform uAlpha;
    private Uniform uScaleX;
    private Uniform uScaleY;

    public SingularityShader() {
        super("singularity");
        uBounds = uniform("uBounds");
        uAlpha = uniform("uAlpha");
        uScaleX = uniform("uScaleX");
        uScaleY = uniform("uScaleY");
    }

    public void setBounds(float x, float y, float width, float height) {
        uBounds.value4f(x, y, width, height);
    }

    public void setProgress(float progress) {
        uAlpha.value1f(progress);
    }

    public void setScale(float scaleX, float scaleY) {
        uScaleX.value1f(scaleX);
        uScaleY.value1f(scaleY);
    }
}