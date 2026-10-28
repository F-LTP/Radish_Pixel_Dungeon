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
 * 方向擦除着色器。
 */
public class WipeShader extends GlslShaderScript {

    private Uniform uBounds;
    private Uniform uAlpha;
    private Uniform uDirection;

    public WipeShader() {
        super("wipe");
        uBounds = uniform("uBounds");
        uAlpha = uniform("uAlpha");
        uDirection = uniform("uDirection");
    }

    public void setBounds(float x, float y, float width, float height) {
        uBounds.value4f(x, y, width, height);
    }

    public void setProgress(float progress) {
        uAlpha.value1f(progress);
    }

    /**
     * 设置擦除方向
     * @param dirX 方向X (1.0 或 0.0)
     * @param dirY 方向Y (1.0 或 0.0)
     */
    public void setDirection(float dirX, float dirY) {
        uDirection.value2f(dirX, dirY);
    }
}