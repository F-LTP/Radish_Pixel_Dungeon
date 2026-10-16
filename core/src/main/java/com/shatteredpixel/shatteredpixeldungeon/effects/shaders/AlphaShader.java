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
 * 透明淡出着色器。
 */
public class AlphaShader extends GlslShaderScript {

    private Uniform uBounds;
    private Uniform uAlpha;

    public AlphaShader() {
        super("alpha");
        uBounds = uniform("uBounds");
        uAlpha = uniform("uAlpha");
    }

    public void setBounds(float x, float y, float width, float height) {
        uBounds.value4f(x, y, width, height);
    }

    public void setProgress(float progress) {
        uAlpha.value1f(progress);
    }
}