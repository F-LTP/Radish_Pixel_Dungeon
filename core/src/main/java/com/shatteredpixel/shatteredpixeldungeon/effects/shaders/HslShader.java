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

import com.badlogic.gdx.Gdx;
import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * HSL 颜色调整着色器。
 */
public class HslShader extends GlslShaderScript {

    private Uniform uHsl;
    private int uHslLocation;

    public HslShader() {
        super("hsl");
        uHsl = uniform("uHsl");
        if (uHsl != null) {
            uHslLocation = uHsl.location();
        }
    }

    /**
     * 设置 HSL 调整值
     * @param h 色相偏移 (-100 到 100)
     * @param s 饱和度调整 (-100 到 100)
     * @param l 亮度调整 (-100 到 100)
     */
    public void setHsl(float h, float s, float l) {
        if (uHsl != null) {
            Gdx.gl20.glUniform3f(uHslLocation, h, s, l);
        }
    }
}