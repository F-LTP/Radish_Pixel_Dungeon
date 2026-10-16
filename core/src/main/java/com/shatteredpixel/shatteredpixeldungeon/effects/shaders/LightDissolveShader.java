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
 * 光解消散着色器。
 *
 * 贴图自上而下、逐像素地变白成光点，白化像素向右上缓慢漂移，随后渐隐消散。
 * 像素格对齐贴图源像素，由 {@link #setPixelSize(float, float)} 按当前帧尺寸计算。
 */
public class LightDissolveShader extends GlslShaderScript {

    private Uniform uProgress;
    private Uniform uPixelSize;
    private Uniform uRandom;
    private Uniform uMoveAmount;
    private Uniform uMoveDir;

    public LightDissolveShader() {
        super("lightdissolve");
        uProgress = uniform("uProgress");
        uPixelSize = uniform("uPixelSize");
        uRandom = uniform("uRandom");
        uMoveAmount = uniform("uMoveAmount");
        uMoveDir = uniform("uMoveDir");
    }

    /**
     * 消散进度 0.0 - 1.0。
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
     * 每实例随机种子，控制溶解顺序扰动。
     */
    public void setRandom(float random) {
        if (uRandom != null) uRandom.value1f(random);
    }

    /**
     * 最大漂移量，相对帧宽的比例（约 0.10 - 0.20）。
     */
    public void setMoveAmount(float amount) {
        if (uMoveAmount != null) uMoveAmount.value1f(amount);
    }

    /**
     * 漂移方向（右上方）。
     */
    public void setMoveDir(float x, float y) {
        if (uMoveDir != null) uMoveDir.value2f(x, y);
    }
}
