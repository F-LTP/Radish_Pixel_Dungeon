package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinRiderSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 哥布林骑手：一区跆拳洞穴（苔藓分支第 2 层）的小 Boss。
 *
 * 远程：投掷长矛，伤害略高于石子（3~6）。
 * 近战：与坐骑青蛙一起攻击（哥布林 2~3 + 青蛙 1~5）。
 * 掉落：3 只捕鱼矛 + 金钥匙。
 */
public class GoblinRider extends Goblin {

    {
        spriteClass = GoblinRiderSprite.class;

        // 在普通哥布林（HP 20、命中 14）基础上上调
        HP = HT = 26;
        defenseSkill = 5;

        EXP = 8;
        maxLvl = 12;

        loot = null;
        lootChance = 0f;
    }

    private static final String SPEARREMAIN = "SpearRemain";
    private int spearRemain = 3;

    // 本次攻击是否为远程投矛（由 doAttack 设置，onZapComplete 读取）
    private boolean rangedAttack = false;
    // 本次投矛的落点（doAttack 时就算好，动画结束后直接用，避免 enemy 中途失效）
    private int spearCell = -1;

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SPEARREMAIN, spearRemain);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        spearRemain = bundle.getInt(SPEARREMAIN);
    }

    @Override
    public int damageRoll() {
        // 哥布林 2~3 + 青蛙 1~5，一起打一次
        return Random.NormalIntRange(2, 3) + Char.combatRoll(1, 5);
    }

    @Override
    public int attackSkill(Char target) {
        return 16;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 4) + Char.combatRoll(0, 2);
    }

    // 骑手投的是长矛而不是石头。canAttack 沿用 Goblin 的写法（父类判定 || canThrowAt），
    // 这样相邻、挑战装备的武器距离、精英的额外攻击距离都不会被覆写丢掉
    @Override
    protected boolean canThrowAt(Char enemy) {
        return spearRemain > 0
                && new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
    }

    /**
     * 近战走标准流程（sprite.attack），远程走投矛流程（sprite.zap）。
     * 两者都必须返回 false 让动画先播，并在动画结束时统一由
     * onAttackComplete / onZapComplete 结算并 spend()，否则回合不结束会卡死。
     */
    @Override
    protected boolean doAttack(Char enemy) {
        boolean visible = sprite != null && (sprite.visible || enemy.sprite.visible);

        // 相邻，或矛已投完 / 弹道被挡但仍在攻击距离内（精英的隔空攻击）：近战
        if (Dungeon.level.adjacent(pos, enemy.pos) || !canThrowAt(enemy)) {
            rangedAttack = false;
            if (visible) {
                sprite.attack(enemy.pos);
                return false;
            } else {
                attack(enemy);
                Invisibility.dispel(this);
                spend(attackDelay());
                return true;
            }
        }

        // 远程投矛
        rangedAttack = true;
        spearRemain = Math.max(spearRemain - 1, 0);
        spearCell = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos;
        if (visible) {
            sprite.zap(enemy.pos);
            return false;
        } else {
            resolveSpear(spearCell);
            spearCell = -1;
            Invisibility.dispel(this);
            spend(attackDelay());
            rangedAttack = false;
            return true;
        }
    }

    @Override
    public void onAttackComplete() {
        if (rangedAttack) {
            // 远程分支理论上不会走到这里，保底结算，避免回合不结束
            finishRanged();
        } else {
            super.onAttackComplete();
        }
    }

    @Override
    public void onZapComplete() {
        if (rangedAttack) {
            finishRanged();
        } else {
            spend(attackDelay());
            next();
        }
    }

    /**
     * 投矛结算：算伤害 → 放一个纯视觉的飞行物 → 无条件结束本回合。
     * 这里绝不能再把 spend()/next() 交给 MissileSprite 的回调：
     * 回调一旦丢失（精灵被回收、中途切层、目标死亡）就会永远不结束回合，表现为骑手原地空转、玩家卡死。
     */
    private void finishRanged() {
        rangedAttack = false;
        int cell = spearCell >= 0 ? spearCell : pos;
        spearCell = -1;

        resolveSpear(cell);
        Invisibility.dispel(this);

        if (sprite != null && sprite.parent != null) {
            ((MissileSprite) sprite.parent.recycle(MissileSprite.class)).reset(
                    sprite, cell, new FishingSpear(), new Callback() {
                        @Override
                        public void call() {
                            // 仅视觉，不承担任何结算
                        }
                    });
        }

        spend(attackDelay());
        next();
    }

    /**
     * 投矛结算：只对落点上的敌对单位判定命中/闪避，矛本身不落地。
     * 与 Headcrab 的远程箭矢同一套虚拟投掷物写法。
     */
    private void resolveSpear(int cell) {
        if (!isAlive()) return;

        Char target = Actor.findChar(cell);
        if (target == null || target == this || target.alignment == alignment) {
            return;
        }

        if (hit(this, target, true)) {
            int dmg = Random.NormalIntRange(3, 6);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            dmg = target.defenseProc(this, dmg);
            dmg = Math.max(0, dmg - target.drRoll());
            target.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, this, new ThrowingStone()));

            if (!target.isAlive() && target == Dungeon.hero) {
                Dungeon.fail(getClass());
                GLog.n(Messages.get(this, "kill"));
            }
        } else {
            target.sprite.showStatus(CharSprite.NEUTRAL, target.defenseVerb());
        }
    }

    @Override
    public void die(Object cause) {
        // 掉落 3 只捕鱼矛
        for (int i = 0; i < 3; i++) {
            Dungeon.level.drop(new FishingSpear(), pos).sprite.drop();
        }
        // 金钥匙（用于开启本层赐福卷轴房间的金宝箱）
        Dungeon.level.drop(new GoldenKey(Dungeon.depth), pos).sprite.drop();
        super.die(cause);
    }

    @Override
    public String description() {
        return Messages.get(this, "desc");
    }

    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(GoblinRider.class, "kill"));
    }
}
