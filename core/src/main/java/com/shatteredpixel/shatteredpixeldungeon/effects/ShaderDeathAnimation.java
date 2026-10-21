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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.utils.PointF;

/**
 * 着色器死亡动画：由 shader 接管尸体。
 * 光解、燃烧、酸蚀、切断等所有 shader 死法共用此实现，差异只在于 {@link ShaderEffect.ShaderType}。
 * 默认不播放序列帧死亡动画；需要 shader 与正常死亡动画叠加时，
 * 使用 {@link #ShaderDeathAnimation(ShaderEffect.ShaderType, float, boolean)} 并传入 true。
 */
public class ShaderDeathAnimation implements DeathAnimation {

    /** 其它 shader 死法的默认时长。 */
    public static final float DEFAULT_DURATION = 0.5f;
    /** 光解消散时长：约为默认 shader 死法的 0.33 倍速。 */
    public static final float LIGHT_DISSOLVE_DURATION = 1.5f;
    /** 光蒸发时长：与原作 Combine Rifle 的 2.5 秒一致。 */
    public static final float LIGHT_EVAPORATE_DURATION = 2.5f;
    /** 光蒸发整体漂移速度：2 像素/秒，2.5 秒共约 5 像素。 */
    public static final float LIGHT_EVAPORATE_DRIFT_SPEED = 2f;

    /** 按 shader 类型返回默认时长（光解更慢）。 */
    public static float durationFor(ShaderEffect.ShaderType type) {
        switch (type) {
            case LIGHTDISSOLVE: return LIGHT_DISSOLVE_DURATION;
            case LIGHTEVAPORATE: return LIGHT_EVAPORATE_DURATION;
            default: return DEFAULT_DURATION;
        }
    }

    private final ShaderEffect.ShaderType type;
    private final float duration;
    private final boolean playDeathAnimation;

    public ShaderDeathAnimation(ShaderEffect.ShaderType type, float duration) {
        this(type, duration, false);
    }

    public ShaderDeathAnimation(ShaderEffect.ShaderType type, float duration, boolean playDeathAnimation) {
        this.type = type;
        this.duration = duration;
        this.playDeathAnimation = playDeathAnimation;
    }

    @Override
    public void play(CharSprite sprite, Runnable onFinished) {
        sprite.prepareDeathVisual(playDeathAnimation);

        Game.runOnRenderThread(() -> {
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
            if (type == ShaderEffect.ShaderType.LIGHTEVAPORATE) {
                applyLightEvaporateDrift(sprite, effect);
            }
            effect.onComplete(onFinished);
            sprite.setShaderEffect(effect);
            sprite.parent.add(effect);
        });
    }

    /**
     * 原作的复制体会沿弹道方向整体漂移。这里用“最近攻击者 -> 死者”的方向近似；
     * 没有攻击者时（如 HolyDeath 处决）默认向上飘。
     */
    private static void applyLightEvaporateDrift(CharSprite sprite, ShaderEffect effect) {
        float dx = 0f;
        float dy = -1f;
        Char ch = sprite.ch;
        if (ch != null && ch.lastAttacker != null
                && ch.lastAttacker != ch && ch.lastAttacker.sprite != null) {
            PointF from = ch.lastAttacker.sprite.center();
            PointF to = sprite.center();
            dx = to.x - from.x;
            dy = to.y - from.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len > 1e-4f) {
                dx /= len;
                dy /= len;
            } else {
                dx = 0f;
                dy = -1f;
            }
        }
        effect.setDrift(dx * LIGHT_EVAPORATE_DRIFT_SPEED,
                        dy * LIGHT_EVAPORATE_DRIFT_SPEED);
    }
}
