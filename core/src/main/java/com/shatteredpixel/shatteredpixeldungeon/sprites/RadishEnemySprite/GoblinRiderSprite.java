package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.GoblinRider;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/**
 * 哥布林骑手（青蛙坐骑）。
 *
 * 贴图：16 帧，每帧 16x18（比标准 16x16 高 2px）。
 * 高度超出格子时，CharSprite.worldToCamera() 会把精灵整体上抬，
 * 导致脚底离地。因此按 WardSprite 的做法补偿 perspectiveRaise。
 *
 * 帧分布（共 16 帧）：
 *   0-5   待机
 *   6-8   攻击
 *   9-11  受击 / 远程投矛
 *   12-15 死亡
 */
public class GoblinRiderSprite extends MobSprite {

    public GoblinRiderSprite() {
        super();

        texture(Assets.Sprites.GOBLIN_RIDER);
        TextureFilm frames = new TextureFilm(texture, 16, 18);

        idle = new MovieClip.Animation(2, true);
        idle.frames(frames, 0, 1, 2, 3, 4, 5);

        run = new MovieClip.Animation(10, true);
        run.frames(frames, 0, 1, 2, 3);

        attack = new MovieClip.Animation(12, false);
        attack.frames(frames, 6, 7, 8);

        // 远程投矛：与攻击共用前倾扑出的动作
        zap = new MovieClip.Animation(12, false);
        zap.frames(frames, 6, 7, 8);

        die = new MovieClip.Animation(9, false);
        die.frames(frames, 13, 14, 15);

        play(idle);
    }

    /**
     * 引擎只在 attack / operate 两种动画结束时回调 Char，zap 动画不会自动分发
     * （见 CharSprite.onComplete）。骑手的投矛分支以 sprite.zap() 起手并返回 false
     * 让动画先播，若动画结束时不回调 onZapComplete，演员线程会永远等不到 next()，
     * 表现就是「卡住不动」——与 DeminionSprite / DeviloonSprite 同一套写法。
     */
    @Override
    public void onComplete(Animation anim) {
        super.onComplete(anim);

        if (anim == zap) {
            idle();
            if (ch instanceof GoblinRider) {
                ((GoblinRider) ch).onZapComplete();
            }
        }
    }

    @Override
    public int blood() {
        return 0xFF3F7F3F;
    }
}
