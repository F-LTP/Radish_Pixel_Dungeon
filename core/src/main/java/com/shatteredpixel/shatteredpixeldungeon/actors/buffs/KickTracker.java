/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 踹飞技能追踪器
 * 当周围3格内有3个或以上敌人时可用
 * 点击后踹飞所有敌人3格（使用BlastWand的throwChar方法）
 * 实现ActionIndicator.Action接口以显示在右下角按钮
 */
public class KickTracker extends Buff implements ActionIndicator.Action {

    {
        type = buffType.POSITIVE;
    }

    public static final int KICK_RANGE = 3; // 踹飞距离
    public static final int DETECT_RANGE = 3; // 检测范围

    @Override
    public String icon() {
        return BuffIndicator.COMBO;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(1f, 0.5f, 0f); // 橙色调
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public String actionIcon() {
        return HeroIcon.COMBO;
    }

    @Override
    public Visual secondaryVisual() {
        BitmapText txt = new BitmapText(PixelScene.pixelFont);
        txt.text("!");
        txt.hardlight(CharSprite.POSITIVE);
        txt.measure();
        return txt;
    }

    @Override
    public int indicatorColor() {
        return 0xFF8800; // 橙色
    }

    @Override
    public boolean act() {
        if (nearbyEnemies((Hero) target).size() < 3) {
            detach();
            ActionIndicator.clearAction(this);
        } else {
            spend(TICK);
        }
        return true;
    }

    private ArrayList<Char> nearbyEnemies(Hero hero) {
        ArrayList<Char> enemies = new ArrayList<>();

        // 检测周围3格内的敌人
        PathFinder.buildDistanceMap(hero.pos, BArray.not(Dungeon.level.solid, null), DETECT_RANGE);
        for (Char ch : Actor.chars()) {
            if (ch != hero && ch.alignment == Char.Alignment.ENEMY
                    && PathFinder.distance[ch.pos] <= DETECT_RANGE
                    && Dungeon.level.heroFOV[ch.pos]) {
                enemies.add(ch);
            }
        }
        return enemies;
    }

    @Override
    public void doAction() {
        Hero hero = (Hero) target;
        ArrayList<Char> enemies = nearbyEnemies(hero);
        if (enemies.size() < 3) {
            detach();
            ActionIndicator.clearAction(this);
            GLog.w(Messages.get(this, "not_enough_enemies"));
            return;
        }

        // 踹飞所有敌人（使用BlastWand的throwChar方法）
        int kickedCount = 0;
        for (Char enemy : enemies) {
            if (kickEnemy(hero, enemy)) {
                kickedCount++;
            }
        }

        // 视觉效果
        Sample.INSTANCE.play(Assets.Sounds.BLAST);
        hero.sprite.emitter().burst(BlastParticle.FACTORY, 20);

        // 清除技能
        detach();
        ActionIndicator.clearAction(this);

        if (kickedCount > 0) {
            GLog.p(Messages.get(this, "kicked", kickedCount));
        }

        hero.spendAndNext(1f);
    }

    /**
     * 使用BlastWand的throwChar方法踹飞敌人
     * @return 是否成功踹飞
     */
    private boolean kickEnemy(Hero hero, Char enemy) {
        // 造成少量伤害（1-3点）
        int damage = Random.NormalIntRange(1, 3);
        enemy.damage(DamageInfo.of(damage, DamageType.PHYSICAL, hero, this));

        // 计算击退方向（从英雄指向敌人）
        int dx = enemy.pos % Dungeon.level.width() - hero.pos % Dungeon.level.width();
        int dy = enemy.pos / Dungeon.level.width() - hero.pos / Dungeon.level.width();

        // 目标位置：从敌人位置沿击退方向延伸
        int targetX = enemy.pos % Dungeon.level.width() + dx * KICK_RANGE;
        int targetY = enemy.pos / Dungeon.level.width() + dy * KICK_RANGE;
        int targetPos;
        if (targetX >= 0 && targetX < Dungeon.level.width() && targetY >= 0) {
            //超出地图下沿不用另外处理：弹道只把目标下标换算回坐标来定方向，这时换算结果仍然正确
            targetPos = targetX + targetY * Dungeon.level.width();
        } else {
            //横向出界的下标会绕到相邻的行，向上出界会变成负数，换算回坐标后方向是错的，敌人会被踹向不相干的方向。
            //这时改用「英雄 -> 敌人」连线的延长线，取它在地图内的最后一格当目标
            Ballistica toEnemy = new Ballistica(hero.pos, enemy.pos, Ballistica.STOP_TARGET);
            targetPos = toEnemy.path.get(toEnemy.path.size() - 1);
        }

        // 构建弹道轨迹（从敌人位置指向击退方向）
        // 目标格只用来定方向，弹道不能在目标格停下：throwChar 把「弹道在击退距离之内结束」当成撞到了东西，
        // 而紧挨英雄的敌人目标格离它正好 3 格，带着「到目标就停」会让它在空地上也结算撞墙伤害和麻痹。
        // 与震爆法杖自己的写法一致，只在墙和角色前停下
        Ballistica trajectory = new Ballistica(enemy.pos, targetPos, Ballistica.MAGIC_BOLT);

        // 使用BlastWand的throwChar方法，power=KICK_RANGE表示踹飞3格
        // collideDmg=true 造成碰撞伤害
        WandOfBlastWave.throwChar(enemy, trajectory, KICK_RANGE, false, true, this);

        return true;
    }

    /**
     * 检测是否应该给予踹飞技能
     */
    public static void checkKick(Hero hero) {
        // 检查是否已有该buff
        if (hero.buff(KickTracker.class) != null) return;

        int enemyCount = 0;
        PathFinder.buildDistanceMap(hero.pos, BArray.not(Dungeon.level.solid, null), DETECT_RANGE);
        for (Char ch : Actor.chars()) {
            if (ch != hero && ch.alignment == Char.Alignment.ENEMY
                    && PathFinder.distance[ch.pos] <= DETECT_RANGE
                    && Dungeon.level.heroFOV[ch.pos]) {
                enemyCount++;
            }
        }

        if (enemyCount >= 3) {
            KickTracker kick = Buff.affect(hero, KickTracker.class);
            ActionIndicator.setAction(kick);
        }
    }

    @Override
    public void detach() {
        super.detach();
        ActionIndicator.clearAction(this);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        //这个状态只有按下按钮用掉才会消失，读档后要把按钮放回去。
        //不放的话状态还留在身上，checkKick 看到已有状态就不会再给，这次踹飞再也用不出来
        ActionIndicator.setAction(this);
    }

    @Override
    public String desc() {
        int enemyCount = 0;
        Hero hero = (Hero) target;
        PathFinder.buildDistanceMap(hero.pos, BArray.not(Dungeon.level.solid, null), DETECT_RANGE);
        for (Char ch : Actor.chars()) {
            if (ch != hero && ch.alignment == Char.Alignment.ENEMY
                    && PathFinder.distance[ch.pos] <= DETECT_RANGE
                    && Dungeon.level.heroFOV[ch.pos]) {
                enemyCount++;
            }
        }
        return Messages.get(this, "desc", enemyCount);
    }
}
