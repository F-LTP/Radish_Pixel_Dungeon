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
 * 酸蚀溶解着色器。
 */
public class AcidShader extends GlslShaderScript {

    private Uniform uAlpha;
    private Uniform uRandom;

    public AcidShader() {
        super("acid");
        uAlpha = uniform("uAlpha");
        uRandom = uniform("uRandom");
    }

    public void setProgress(float progress) {
        uAlpha.value1f(progress);
    }

    public void setRandom(float random) {
        uRandom.value1f(random);
    }
}