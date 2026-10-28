/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.glwrap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.glscripts.Script;

/**
 * 通用 GLSL 着色器基类。
 * 
 * 从 assets/shaders/{name}.vert 和 assets/shaders/{name}.frag 加载着色器源码，
 * 自动适配 Noosa 命名约定（Slice&Dice → Noosa）。
 */
public abstract class GlslShaderScript extends Script {

    // ============== 通用 Uniform ==============
    protected Uniform uTime;        // 动画时间
    protected Uniform uAlpha;       // 整体透明度
    protected Uniform uCamera;      // 摄像机矩阵
    protected Uniform uFrame;       // 当前 sprite 帧在图集中的 UV 范围
    
    // ============== 通用 Attribute ==============
    public Attribute aXYZW;      // 顶点位置
    public Attribute aUV;        // 纹理坐标
    
    // 着色器名称（用于加载文件）
    private final String shaderName;
    
    /**
     * 构造函数
     * @param shaderName 着色器名称（不含扩展名），如 "cut"、"burn"
     */
    protected GlslShaderScript(String shaderName) {
        this.shaderName = shaderName;
        compile(buildShaderSource());
        
        // 获取通用 uniform/attribute
        uTime = uniform("uTime");
        uAlpha = uniform("uAlpha");
        uCamera = uniform("uCamera");
        uFrame = uniform("uFrame");
        aXYZW = attribute("aXYZW");
        aUV = attribute("aUV");
    }
    
    /**
     * 构建合并的着色器源码（vertex + "//\n" + fragment）
     */
    private String buildShaderSource() {
        String vertex = loadVertexShader();
        String fragment = loadFragmentShader();
        return vertex + "//\n" + fragment;
    }
    
    /**
     * 从文件加载顶点着色器源码
     */
    protected String loadVertexShader() {
        FileHandle file = Gdx.files.internal("shaders/" + shaderName + ".vert");
        if (!file.exists()) {
            throw new RuntimeException("Vertex shader not found: " + file.path());
        }
        return adaptVertexShader(file.readString());
    }
    
    /**
     * 从文件加载片段着色器源码
     */
    protected String loadFragmentShader() {
        FileHandle file = Gdx.files.internal("shaders/" + shaderName + ".frag");
        if (!file.exists()) {
            throw new RuntimeException("Fragment shader not found: " + file.path());
        }
        return adaptFragmentShader(file.readString());
    }
    
    /**
     * 子类可覆盖以提供硬编码顶点着色器（兼容旧方式）
     */
    protected String vertexShaderSource() {
        return null;
    }
    
    /**
     * 子类可覆盖以提供硬编码片段着色器（兼容旧方式）
     */
    protected String fragmentShaderSource() {
        return null;
    }

    /**
     * 适配顶点着色器：转换 Slice&Dice 命名为 Noosa 命名
     */
    protected String adaptVertexShader(String src) {
        return src
            .replace("a_position", "aXYZW")
            .replace("a_texCoord0", "aUV")
            .replace("v_texCoords", "vUV");
    }

    /**
     * 适配片段着色器
     */
    protected String adaptFragmentShader(String src) {
        return src
            .replace("v_texCoords", "vUV")
            .replace("u_texture", "uTex");
    }

    /**
     * 设置顶点属性指针
     */
    public void setVertexAttribPointers() {
        if (aXYZW != null) {
            aXYZW.enable();
            aXYZW.vertexPointer(2, 4, null);
        }
        if (aUV != null) {
            aUV.enable();
            aUV.vertexPointer(2, 4, null);
        }
    }
    
    /**
     * 在每次着色器绘制前调用，用于绑定额外纹理。
     * 子类覆盖此方法以绑定自定义纹理（如噪声纹理）。
     * 默认空实现。
     */
    public void bindTextures() {
        // 默认无额外纹理需要绑定
    }

    /**
     * 设置时间参数（用于动画）
     */
    public void setTime(float time) {
        if (uTime != null) {
            uTime.value1f(time);
        }
    }

    /**
     * 设置透明度
     */
    public void setAlpha(float alpha) {
        if (uAlpha != null) {
            uAlpha.value1f(alpha);
        }
    }

    /**
     * 设置摄像机矩阵
     */
    public void setCamera(float[] matrix) {
        if (uCamera != null) {
            uCamera.valueM4(matrix);
        }
    }

    public void setFrame(float left, float top, float width, float height) {
        if (uFrame != null) {
            uFrame.value4f(left, top, width, height);
        }
    }
}
