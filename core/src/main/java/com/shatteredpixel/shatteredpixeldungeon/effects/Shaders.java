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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.*;
import com.watabou.glscripts.Script;

/**
 * 着色器注册表。
 */
public class Shaders {

    // ============== Slice&Dice 效果着色器 ==============
    
    public static final CutShader cut = load(CutShader.class);
    public static final BurnShader burn = load(BurnShader.class);
    public static final EllipseShader ellipse = load(EllipseShader.class);
    public static final HslShader hsl = load(HslShader.class);
    public static final SingularityShader singularity = load(SingularityShader.class);
    public static final AcidShader acid = load(AcidShader.class);
    public static final WipeShader wipe = load(WipeShader.class);
    public static final AlphaShader alpha = load(AlphaShader.class);
    public static final NoiseShader noise = load(NoiseShader.class);
    
    // ============== 加载方法 ==============
    
    private static <T extends com.watabou.glwrap.GlslShaderScript> T load(Class<T> shaderClass) {
        return Script.use(shaderClass);
    }
    
    /**
     * 预编译所有着色器
     */
    public static void init() {
        cut.getClass();
        burn.getClass();
        ellipse.getClass();
        hsl.getClass();
        singularity.getClass();
        acid.getClass();
        wipe.getClass();
        alpha.getClass();
        noise.getClass();
    }
    
    /**
     * 清理所有着色器
     */
    public static void reset() {
        Script.reset();
    }
}