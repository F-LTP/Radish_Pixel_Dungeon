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
 * 贴图从右上到左下缓慢变白（最终为 70% 白 + 30% 原色）：每个像素在单格白化到 75% 后都脱落为光点，光点从 0 透明度
 * 淡入到 50%-80%，沿右上方向飞行并逐渐变透明。每格移动速度随机（平均为满速的一半，
 * 最远不超过满速），到动画结束时全部消失。
 * 像素格对齐贴图源像素，由 {@link #setPixelSize(float, float)} 按当前帧尺寸计算。
 */
public class LightDissolveShader extends GlslShaderScript {

    private Uniform uProgress;
    private Uniform uPixelSize;
    private Uniform uRandom;
    private Uniform uFlySpeed;
    private Uniform uMoveDir;

    public LightDissolveShader() {
        super("lightdissolve");
        uProgress = uniform("uProgress");
        uPixelSize = uniform("uPixelSize");
        uRandom = uniform("uRandom");
        uFlySpeed = uniform("uFlySpeed");
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
     * 单位进度内沿漂移方向飞行的局部 UV 距离（相对帧尺寸）。
     * 光点不再停在固定距离，而是一直飞到离开帧边界。
     */
    public void setFlySpeed(float speed) {
        if (uFlySpeed != null) uFlySpeed.value1f(speed);
    }

    /**
     * 漂移方向，单位向量，位于当前帧的局部 UV 空间。
     */
    public void setMoveDir(float x, float y) {
        if (uMoveDir != null) uMoveDir.value2f(x, y);
    }
}
