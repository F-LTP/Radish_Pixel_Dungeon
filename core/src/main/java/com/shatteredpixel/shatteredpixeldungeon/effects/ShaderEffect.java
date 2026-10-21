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

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.utils.RectF;

import java.nio.FloatBuffer;

/**
 * 着色器效果控制器。
 */
public class ShaderEffect extends Gizmo {

    private final CharSprite target;
    private final ShaderType shaderType;
    private final float duration;
    private float elapsed = 0;
    private float progress = 0;
    private final float random = (float) Math.random();

    private final GlslShaderScript shader;
    private float[] combinedMatrix = new float[16];

    /** LIGHTEVAPORATE 的整体漂移速度（像素/秒），由死亡动画按方向设置。 */
    private float driftX = 0f;
    private float driftY = 0f;

    public ShaderEffect(CharSprite target, ShaderType type, float duration) {
        super();
        this.target = target;
        this.shaderType = type;
        this.duration = duration;

        shader = getShader(type);
    }

    /** 进度走完后的回调（如清理尸体），替代旧的死亡标记轮询。 */
    private Runnable onComplete;

    public void onComplete( Runnable onComplete ) {
        this.onComplete = onComplete;
    }

    /**
     * 设置整体漂移速度（像素/秒）。仅 LIGHTEVAPORATE 使用。
     */
    public void setDrift(float x, float y) {
        this.driftX = x;
        this.driftY = y;
    }

    private GlslShaderScript getShader(ShaderType type) {
        switch (type) {
            case CUT: return Shaders.cut;
            case BURN: return Shaders.burn;
            case ELLIPSE: return Shaders.ellipse;
            case ACID: return Shaders.acid;
            case WIPE: return Shaders.wipe;
            case ALPHA: return Shaders.alpha;
            case SINGULARITY: return Shaders.singularity;
            case NOISE: return Shaders.noise;
            case LIGHTDISSOLVE: return Shaders.lightDissolve;
            case LIGHTEVAPORATE: return Shaders.lightEvaporate;
            default: return Shaders.alpha;
        }
    }

    private void uploadShaderParams() {
        shader.setAlpha(1.0f);
        shader.setTime(elapsed);

        switch (shaderType) {
            case CUT:
                if (shader instanceof CutShader) {
                    CutShader c = (CutShader) shader;
                    c.setCutLine(0.5f, 0.5f, 1.0f, 1.0f);
                    c.setCutProgress(progress);
                    c.setCutSide(1);
                }
                break;
            case BURN:
                if (shader instanceof BurnShader) {
                    BurnShader b = (BurnShader) shader;
                    b.setBurnProgress(progress);
                    b.setRandom(random);
                }
                break;
            case ELLIPSE:
                if (shader instanceof EllipseShader) {
                    EllipseShader e = (EllipseShader) shader;
                    e.setProgress(progress);
                    e.setRandom(random);
                }
                break;
            case ACID:
                if (shader instanceof AcidShader) {
                    AcidShader a = (AcidShader) shader;
                    a.setProgress(progress);
                    a.setRandom(random);
                }
                break;
            case WIPE:
                if (shader instanceof WipeShader) {
                    WipeShader w = (WipeShader) shader;
                    w.setProgress(progress);
                    w.setDirection(1, 0);
                }
                break;
            case ALPHA:
                if (shader instanceof AlphaShader) {
                    AlphaShader a = (AlphaShader) shader;
                    a.setProgress(progress);
                }
                break;
            case SINGULARITY:
                if (shader instanceof SingularityShader) {
                    SingularityShader s = (SingularityShader) shader;
                    s.setProgress(progress);
                    s.setScale(1f - progress, 1f - progress);
                }
                break;
            case NOISE:
                if (shader instanceof NoiseShader) {
                    NoiseShader n = (NoiseShader) shader;
                    n.setProgress(progress);
                }
                break;
            case LIGHTDISSOLVE:
                if (shader instanceof LightDissolveShader) {
                    LightDissolveShader l = (LightDissolveShader) shader;
                    l.setProgress(progress);
                    l.setRandom(random);
                    l.setFlySpeed(1.2f);
                }
                break;
            case LIGHTEVAPORATE:
                if (shader instanceof LightEvaporateShader) {
                    LightEvaporateShader e = (LightEvaporateShader) shader;
                    e.setProgress(progress);
                    e.setRandom(random);
                    // 原作在 HDR + bloom 下从 1 升到 10；LDR 下 4 已接近全白。
                    e.setEmissive(1f + 3f * progress);
                    e.setCellScale(1f);
                }
                break;
        }
    }

    @Override
    public void update() {
        super.update();

        elapsed += Game.elapsed;
        progress = Math.min(elapsed / duration, 1.0f);

        // 原作会复制精灵并沿弹道方向整体漂移，这里直接推动当前 sprite
        if (shaderType == ShaderType.LIGHTEVAPORATE && target != null) {
            target.x += driftX * Game.elapsed;
            target.y += driftY * Game.elapsed;
        }

        if (progress >= 1.0f) {
            killAndErase();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * 由 CharSprite.draw() 调用，接管渲染。
     */
    public void drawWithShader(com.watabou.gltextures.SmartTexture texture, RectF frame,
                               float spriteWidth, float spriteHeight,
                               float[] spriteMatrix,
                               FloatBuffer verticesBuffer, Vertexbuffer buffer) {
        if (texture == null) {
//            GLog.w("drawWithShader: texture is null!");
            return;
        }

        Camera cam = camera();
        if (cam == null) cam = Camera.main;
        if (cam == null || cam.matrix == null) {
//            GLog.w("drawWithShader: camera is null!");
            return;
        }

        if (buffer == null) {
//            GLog.w("drawWithShader: buffer is null!");
            return;
        }

        Matrix.multiply(cam.matrix, spriteMatrix, combinedMatrix);

        shader.use();
        shader.setCamera(combinedMatrix);
        shader.setFrame(frame.left, frame.top, frame.width(), frame.height());

        // 光解消散：像素格对齐贴图源像素（帧内每个源像素对应的局部 uv 尺寸）
        if (shaderType == ShaderType.LIGHTDISSOLVE && shader instanceof LightDissolveShader) {
            LightDissolveShader l = (LightDissolveShader) shader;
            float pxW = frame.width() * texture.width;
            float pxH = frame.height() * texture.height;
            l.setPixelSize(pxW > 0f ? 1f / pxW : 1f / 16f,
                           pxH > 0f ? 1f / pxH : 1f / 16f);
            // 直接从实际四边形 UV 判断翻转方向：flipHorizontal 只有在下一次
            // updateFrame() 后才会反映到顶点上，直接读精灵标志可能和实际渲染不一致。
            float uSign = verticesBuffer.get(6) >= verticesBuffer.get(2) ? 1f : -1f;
            float vSign = verticesBuffer.get(11) >= verticesBuffer.get(3) ? 1f : -1f;
            alignLightDissolveDir(l, spriteWidth, spriteHeight, uSign, vSign);
        }

        // 光蒸发同样需要像素格对齐，但不做方向换算
        if (shaderType == ShaderType.LIGHTEVAPORATE && shader instanceof LightEvaporateShader) {
            LightEvaporateShader e = (LightEvaporateShader) shader;
            float pxW = frame.width() * texture.width;
            float pxH = frame.height() * texture.height;
            e.setPixelSize(pxW > 0f ? 1f / pxW : 1f / 16f,
                           pxH > 0f ? 1f / pxH : 1f / 16f);
        }

        uploadShaderParams();
        shader.bindTextures();
        texture.bind();

        if (shader.aXYZW != null) {
            shader.aXYZW.enable();
        } else {
//            GLog.w("drawWithShader: aXYZW is null!");
        }
        if (shader.aUV != null) {
            shader.aUV.enable();
        } else {
//            GLog.w("drawWithShader: aUV is null!");
        }

        buffer.updateGLData();
        buffer.bind();
        shader.aXYZW.vertexBuffer(2, 4, 0);
        shader.aUV.vertexBuffer(2, 4, 2);
        buffer.release();

        Quad.bindIndices();
        Gdx.gl20.glDrawElements(Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0);

        com.watabou.glscripts.Script.unuse();
    }

    /**
     * 把屏幕上的"右上"方向换算到当前精灵帧的局部 UV 空间。
     * 精灵可能被水平/垂直翻转或旋转，只有逆变换后飞散方向在屏幕上才保持一致。
     */
    private void alignLightDissolveDir(LightDissolveShader shader, float spriteWidth, float spriteHeight,
                                       float uSign, float vSign) {
        if (target == null) {
            shader.setMoveDir(0.7071f, -0.7071f);
            return;
        }

        // 屏幕方向：45° 向右上（屏幕坐标 y 向下）
        float sx = 0.7071f;
        float sy = -0.7071f;

        // 逆旋转：精灵局部向量 -> 屏幕向量的矩阵为 Rotate(angle)
        double inv = Math.toRadians(-target.angle);
        float cos = (float) Math.cos(inv);
        float sin = (float) Math.sin(inv);
        float lx = sx * cos - sy * sin;
        float ly = sx * sin + sy * cos;

        // 逆缩放
        float scaleX = Math.abs(target.scale.x) > 1e-6f ? target.scale.x : 1f;
        float scaleY = Math.abs(target.scale.y) > 1e-6f ? target.scale.y : 1f;
        lx /= scaleX;
        ly /= scaleY;

        // 局部像素方向 -> 帧 UV 方向（UV 轴的实际朝向由顶点数据给出）
        if (spriteWidth <= 0f) spriteWidth = 1f;
        if (spriteHeight <= 0f) spriteHeight = 1f;
        float ux = lx / spriteWidth * uSign;
        float uy = ly / spriteHeight * vSign;

        float len = (float) Math.sqrt(ux * ux + uy * uy);
        if (len > 1e-6f) {
            ux /= len;
            uy /= len;
        } else {
            ux = 0.7071f;
            uy = -0.7071f;
        }
        shader.setMoveDir(ux, uy);
    }

    public boolean isFinished() {
        return elapsed >= duration;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (target != null && target.getShaderEffect() == this) {
            target.setShaderEffect(null);
        }
    }

    public enum ShaderType {
        CUT, BURN, ELLIPSE, ACID, WIPE, ALPHA, SINGULARITY, NOISE, LIGHTDISSOLVE, LIGHTEVAPORATE
    }
}
