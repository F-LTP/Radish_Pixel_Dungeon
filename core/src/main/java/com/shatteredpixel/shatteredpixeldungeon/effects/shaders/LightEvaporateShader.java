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
 * 光蒸发着色器。
 *
 * 参考《挺进地牢》Combine Rifle 击杀普通敌人时的消散：每个源像素格由固定 hash 决定消失顺序，
 * 整只精灵的 emissive 强度随进度上升，像素原地随机消失。整体漂移由
 * {@code ShaderEffect} 在 CPU 侧完成。与 {@link LightDissolveShader} 不同，像素块不做逐格位移。
 */
public class LightEvaporateShader extends GlslShaderScript {

    private Uniform uProgress;
    private Uniform uPixelSize;
    private Uniform uRandom;
    private Uniform uEmissive;
    private Uniform uCellScale;

    public LightEvaporateShader() {
        super("lightevaporate");
        uProgress = uniform("uProgress");
        uPixelSize = uniform("uPixelSize");
        uRandom = uniform("uRandom");
        uEmissive = uniform("uEmissive");
        uCellScale = uniform("uCellScale");
    }

    /**
     * 蒸发进度 0.0 - 1.0。
     */
    public void setProgress(float progress) {
        if (uProgress != null) uProgress.value1f(progress);
    }

    /**
     * 单个源像素在帧内局部 uv 中的尺寸，用于像素格对齐。
     */
    public void setPixelSize(float width, float height) {
        if (uPixelSize != null) uPixelSize.value2f(width, height);
    }

    /**
     * 每实例随机种子，控制像素消失顺序。
     */
    public void setRandom(float random) {
        if (uRandom != null) uRandom.value1f(random);
    }

    /**
     * 当前 emissive 强度。原作在 2.5 秒内从 1 升到 10（HDR + bloom）。
     */
    public void setEmissive(float emissive) {
        if (uEmissive != null) uEmissive.value1f(emissive);
    }

    /**
     * 像素格缩放。1 表示每个源像素一格；小尺寸精灵可调大以获得更粗的颗粒。
     */
    public void setCellScale(float cellScale) {
        if (uCellScale != null) uCellScale.value1f(cellScale);
    }
}
