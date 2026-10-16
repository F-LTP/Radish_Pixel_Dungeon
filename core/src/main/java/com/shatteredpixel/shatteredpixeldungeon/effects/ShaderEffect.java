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
                    l.setMoveAmount(0.15f);
                    l.setMoveDir(0.94f, -0.34f);
                }
                break;
        }
    }

    @Override
    public void update() {
        super.update();

        elapsed += Game.elapsed;
        progress = Math.min(elapsed / duration, 1.0f);

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

    // ========== 静态入口 ==========

    /**
     * 死亡着色器动画：准备状态清理 → 渲染线程挂载 shader → 进度走完后清理尸体并回调。
     */
    public static DeathAnimation deathAnimation( ShaderType type, float duration ) {
        return (sprite, onFinished) -> {
            sprite.prepareDeathVisual();

            Game.runOnRenderThread( () -> {
                if (sprite.parent == null) {
                    if (onFinished != null) {
                        onFinished.run();
                    }
                    return;
                }

                ShaderEffect old = sprite.getShaderEffect();
                if (old != null) {
                    old.killAndErase();
                }

                ShaderEffect effect = new ShaderEffect(sprite, type, duration);
                effect.onComplete( onFinished );
                sprite.setShaderEffect(effect);
                sprite.parent.add(effect);
            } );
        };
    }

    public enum ShaderType {
        CUT, BURN, ELLIPSE, ACID, WIPE, ALPHA, SINGULARITY, NOISE, LIGHTDISSOLVE
    }
}
