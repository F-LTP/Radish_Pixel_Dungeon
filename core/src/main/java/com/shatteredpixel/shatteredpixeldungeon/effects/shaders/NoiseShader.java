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

/**
 * 噪声溶解着色器。
 */
public class NoiseShader extends GlslShaderScript {
    
    public NoiseShader() {
        super("noise");
    }
    
    public void setProgress(float progress) {
        if (uAlpha != null) {
            uAlpha.value1f(progress);
        }
    }
}
