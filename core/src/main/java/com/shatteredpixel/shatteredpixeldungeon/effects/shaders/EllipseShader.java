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
 * 椭圆形消失着色器。
 */
public class EllipseShader extends GlslShaderScript {

    private Uniform uBounds;
    private Uniform uAlpha;
    private Uniform uRandom;

    public EllipseShader() {
        super("ellipse");
        uBounds = uniform("uBounds");
        uAlpha = uniform("uAlpha");
        uRandom = uniform("uRandom");
    }

    public void setBounds(float x, float y, float width, float height) {
        uBounds.value4f(x, y, width, height);
    }

    public void setProgress(float progress) {
        uAlpha.value1f(progress);
    }

    public void setRandom(float random) {
        uRandom.value1f(random);
    }
}