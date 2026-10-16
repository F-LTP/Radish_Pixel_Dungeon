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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 骰子法师法术基类。
 *
 * 每个法术可隶属于一个"学派"(school)，并占据该学派的某一等级位(level)。
 * 当学派投入 N 点天赋时，只允许使用该学派列表中的第 N 个法术（前序法术不可用）。
 * 被动技能(isPassive)不消耗魔力点、不可主动施放。
 */
public abstract class DiceMageSpell {

    public String name() {
        return Messages.get(getClass(), "name");
    }

    public String desc() {
        return Messages.get(getClass(), "desc");
    }

    /**
     * 所属学派。返回 null 表示不隶属任何学派（固定初始法术，如"迸发"）。
     */
    public Talent school() {
        return null;
    }

    /**
     * 在该学派列表中的等级位(1..3)。0 表示不适用。
     */
    public int level() {
        return 0;
    }

    /**
     * 是否为被动技能（不可施放）。
     */
    public boolean isPassive() {
        return false;
    }

    public abstract int mpCost();

    public Talent iconTalent() {
        Talent s = school();
        if (s != null) return s;
        return null;
    }

    /**
     * 该法术在 SND atlas（snd/atlas_image.png）中对应的法术图标名。
     * 返回 null 表示不使用 SND 贴图。
     */
    public String sndImageName() {
        return null;
    }

    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        if (isPassive()) return false;

        Talent s = school();
        if (s != null && hero.pointsInTalent(s) != level()) {
            MagicPoint mp = hero.buff(MagicPoint.class);
            // 已"学会"的法术无视天赋等级限制
            if (mp == null || !mp.isLearned(getClass())) {
                return false;
            }
        }

        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return false;

        if (mp.offCooldown(getClass()) == false) return false;

        // 魔力点足够，或背包法杖充能可覆盖本次消耗，均可施放
        return mp.getIntPoints() >= mpCost() || mp.canAfford(mpCost());
    }

    public void cast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;

        if (!canCast()) {
            GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
            return;
        }

        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null && !mp.infiniteMana() && mp.getIntPoints() < mpCost() && mp.canAfford(mpCost())) {
            // 魔力不足但法杖充能足够：弹窗询问是否消耗法杖充能
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndDiceMageWandDrain(mp, mpCost(), this));
            return;
        }

        onCast(hero);
    }

    /** 法杖充能消耗确认后，继续本次施法（魔力已被补充到足够）。 */
    public void continueCast(Hero hero) {
        if (hero == null) return;
        if (!mpCheck()) return;
        onCast(hero);
    }

    private boolean mpCheck() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return false;
        if (!mp.offCooldown(getClass())) return false;
        return mp.getIntPoints() >= mpCost();
    }

    protected boolean spendMagic(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null && mp.spendPoints(mpCost())) {
            return true;
        }
        GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
        return false;
    }

    /**
     * 施法后为当前法术设置冷却回合数。
     */
    protected void startCooldown(Hero hero, float turns) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null) mp.setCooldown(getClass(), turns);
    }

    protected abstract void onCast(Hero hero);

    protected void getTarget(CellSelector.Listener listener) {
        GameScene.selectCell(listener);
    }

    protected boolean isValidEnemy(Char target) {
        return target != null && target.alignment == Char.Alignment.ENEMY;
    }

    /** 傻福：按天赋等级提高力量阈值以上的法术伤害。 */
    protected int strBonusDamage(Hero hero) {
        int points = hero.pointsInTalent(Talent.SPELL_EMPOWER);
        if (points <= 0) return 0;
        int[] thresholds = {16, 14, 12, 10};
        return Random.IntRange(0, 2 * Math.max(0, hero.STR() - thresholds[points - 1]));
    }

    /**
     * 力量加成护盾：每点力量 0-1 点护盾。
     */
    protected int strBonusShield(Hero hero) {
        return Random.IntRange(0, hero.STR());
    }

    /**
     * 为施法者附加力量护盾。
     */
    protected void applyStrShield(Hero hero) {
        int shield = strBonusShield(hero);
        if (shield > 0) Buff.affect(hero, Barrier.class).incShield(shield);
    }

    protected boolean isValidAlly(Char target) {
        return target != null && target.alignment == Char.Alignment.ALLY;
    }

    /**
     * 处决目标（深渊/无限等斩杀类法术共用）。
     *
     * 天狗与矮人国王拥有阶段化的特殊死亡规则：
     * - 天狗的死亡（含开门 unseal、掉落面具）由 PrisonBossLevel.progress() 状态机驱动，
     *   直接调用 die() 会跳过流程导致 Boss 房不开门，因此这里连续推进状态机到 WON；
     * - 矮人国王的 isAlive() 被阶段锁住（phase != 3 时恒为 true），
     *   直接 HP=0 + damage() 永远不会触发 die()，因此强制进入最终阶段再走原生 die()
     *   （其 die() 内含掉落王冠、unseal 开门、清除随从等完整流程）。
     */
    protected void executeKill(Char target, Hero hero) {
        if (target instanceof Tengu && Dungeon.level instanceof PrisonBossLevel) {
            final PrisonBossLevel lvl = (PrisonBossLevel) Dungeon.level;
            target.HP = 0;
            target.deathMarked = false;
            // FIGHT_ARENA 分支会执行 unseal() 并调用 tengu.die()，完成全部收尾
            while (lvl.state() != PrisonBossLevel.State.WON) {
                lvl.progress();
            }
            return;
        }
        if (target instanceof DwarfKing) {
            ((DwarfKing) target).forcedExecute(this);
            return;
        }
        target.HP = 0;
        target.deathMarked = false;
        target.damage(new DamageInfo(1, DamageType.TRUE, hero, null, this));
        if (!target.isAlive()) target.die(this);
    }
}
