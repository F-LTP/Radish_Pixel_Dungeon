package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WheelchairRush;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CatapultStartBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WheelchairCrashBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Speed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.events.BeforeHeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Wheelchair extends Artifact {

    public static final String AC_RIDE = "RIDE";
    public static final String AC_CRASH = "CRASH";

    {
        image = ItemSpriteSheet.ARTIFACT_WHEELCHAIR;

        levelCap = 10;

        charge = 3;
        chargeCap = 3;

        defaultAction = AC_RIDE;
        usesTargeting = true;
    }

    private int moveDistance = 0;

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (hero.buff(MagicImmune.class) != null) {
            return actions;
        }
        if (isEquipped(hero) && (charge > 0 || canRideWithoutCharge(hero)) && !cursed) {
            actions.add(AC_RIDE);
        }
        // 轮椅翻车：月华英雄在加速状态下可使用
        if (isEquipped(hero) && !cursed && hero.heroClass == HeroClasses.MOONLIGHT) {
            int points = hero.pointsInTalent(Talent.WHEELCHAIR_CRASH);
            if (points > 0 && hasAnySpeedBuff(hero)) {
                actions.add(AC_CRASH);
            }
        }
        return actions;
    }

    // 检查是否有任何加速状态
    private boolean hasAnySpeedBuff(Hero hero) {
        return hero.buff(Speed.class) != null
                || hero.buff(WheelchairRush.class) != null
                || hero.buff(Haste.class) != null
                || hero.buff(Stamina.class) != null;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_RIDE)) {
            curUser = hero;

            if (!isEquipped(hero)) {
                GLog.i(Messages.get(Artifact.class, "need_to_equip"));
                usesTargeting = false;
            } else if (hero.buff(Roots.class) != null) {
                GLog.w(Messages.get(this, "rooted"));
                usesTargeting = false;
            } else if (charge < 1 && !canRideWithoutCharge(hero)) {
                GLog.i(Messages.get(this, "no_charge"));
                usesTargeting = false;
            } else if (cursed) {
                GLog.w(Messages.get(this, "cursed"));
                usesTargeting = false;
            } else {
                usesTargeting = true;
                GameScene.selectCell(rideTargeter);
            }
        } else if (action.equals(AC_CRASH)) {
            curUser = hero;
            usesTargeting = false;
            performCrash(hero);
        }
    }

    private boolean canRideWithoutCharge(Hero hero) {
        return hero.buff(CatapultStartBuff.class) != null;
    }

    // 执行轮椅翻车
    private void performCrash(Hero hero) {

        int points = hero.pointsInTalent(Talent.WHEELCHAIR_CRASH);
        if (points <= 0) return;

        // 计算最大加速状态回合数
        int maxTurns = 0;

        Speed speed = hero.buff(Speed.class);
        if (speed != null) maxTurns = Math.max(maxTurns, (int)speed.cooldown());

        WheelchairRush rush = hero.buff(WheelchairRush.class);
        if (rush != null) maxTurns = Math.max(maxTurns, (int)rush.cooldown());

        Haste haste = hero.buff(Haste.class);
        if (haste != null) maxTurns = Math.max(maxTurns, (int)haste.cooldown());

        Stamina stamina = hero.buff(Stamina.class);
        if (stamina != null) maxTurns = Math.max(maxTurns, (int)stamina.cooldown());

        NaturesPower.naturesPowerTracker natPower = hero.buff(NaturesPower.naturesPowerTracker.class);
        if (natPower != null) maxTurns = Math.max(maxTurns, (int)natPower.cooldown());

        // 结束所有加速效果
        Buff.detach(hero, Speed.class);
        Buff.detach(hero, WheelchairRush.class);
        Buff.detach(hero, Haste.class);
        Buff.detach(hero, Stamina.class);
        Buff.detach(hero, NaturesPower.naturesPowerTracker.class);

        // 计算伤害：基础伤害 + 加速回合数
        int baseDamage = 15 + (points - 1) * 8; // +1:15, +2:23, +3:31 (用户说30，用15+8*(points-1)得到15/23/31，改成30就+2时23)
        if (points == 3) baseDamage = 30; // 调整+3为30
        int damage = baseDamage + maxTurns;

        //播放一个冲击波效果
        WandOfBlastWave.BlastWave.blast(hero.pos, 5);
        // 对5x5范围内敌人造成伤害（使用副本避免 ConcurrentModificationException）
        for (Mob mob : new ArrayList<>(Dungeon.level.mobs)) {
            if (Dungeon.level.distance(hero.pos, mob.pos) <= 2) {
                // 2格距离是5x5范围
                mob.damage(new DamageInfo(damage, DamageType.PHYSICAL, hero, this, this));
                CellEmitter.get(mob.pos).burst(SparkParticle.FACTORY, 6);
            }
        }

        //播放坠机音效
        Sample.INSTANCE.play(Assets.Sounds.MAN);
        // 施加轮椅翻车效果（5回合缠绕）
        Buff.affect(hero, WheelchairCrashBuff.class, WheelchairCrashBuff.DURATION);

        // 视觉和音效
        CellEmitter.get(hero.pos).burst(SparkParticle.FACTORY, 12);
        Sample.INSTANCE.play(Assets.Sounds.BONES);

        GLog.p(Messages.get(Wheelchair.class, "crash_success", damage, maxTurns));

        hero.spendAndNext(1f);
        Talent.onArtifactUsed(hero);
        updateQuickslot();
    }

    // 跳跃范围：2 + 0.2*等级（向下取整）
    // 弹射起步+2：跳跃距离+1
    public int jumpRange() {
        int range = 2 + (int)(level() * 0.2f);
        // 检查弹射起步天赋+2
        if (curUser != null) {
            if (curUser.heroClass == HeroClasses.MOONLIGHT && curUser.pointsInTalent(Talent.CATAPULT_START) >= 2) {
                range += 1;
            }
        }
        return range;
    }

    // 加速持续时间：10 + 1*等级
    public float speedDuration() {
        return 10f + level();
    }

    // 充能上限：3 + 等级，最大10
    public int maxCharge() {
        return Math.min(10, 3 + level());
    }

    private CellSelector.Listener rideTargeter = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                // 检查是否处于坠毁状态
                if (curUser.buff(WheelchairCrashBuff.class) != null) {
                    GLog.w(Messages.get(Wheelchair.class, "cannot_jump_crashed"));
                    return;
                }
                if (curUser.buff(Roots.class) != null) {
                    GLog.w(Messages.get(Wheelchair.class, "rooted"));
                    return;
                }

                // 检查范围
                int range = jumpRange();
                if (Dungeon.level.distance(curUser.pos, target) > range) {
                    GLog.w(Messages.get(Wheelchair.class, "out_of_range", range));
                    return;
                }

                // 使用 Ballistica 计算路径，防止穿墙跳跃
                Ballistica route = new Ballistica(curUser.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                int cell = route.collisionPos;

                // 如果目标位置有角色，回退一格
                int backTrace = route.dist - 1;
                while (Actor.findChar(cell) != null && cell != curUser.pos && backTrace >= 0) {
                    cell = route.path.get(backTrace);
                    backTrace--;
                }

                // 如果最终位置就是当前位置，说明无法跳跃
                if (cell == curUser.pos) {
                    GLog.w(Messages.get(Wheelchair.class, "invalid_target"));
                    return;
                }

                // 检查目标位置是否为深渊，需要确认
                if (Dungeon.level.map[cell] == Terrain.CHASM || Dungeon.level.pit[cell]) {
                    showChasmConfirmDialog(curUser, cell);
                    return;
                }

                // 执行跳跃
                performJump(curUser, cell);
            }
        }

        @Override
        public String prompt() {
            int range = jumpRange();
            return Messages.get(Wheelchair.class, "prompt", range);
        }
    };

    // 显示跳入深渊确认对话框
    private void showChasmConfirmDialog(final Hero hero, final int targetPos) {
        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                GameScene.show(
                        new WndOptions(new Image(Dungeon.level.tilesTex(), 176, 16, 16, 16),
                                Messages.get(Chasm.class, "chasm"),
                                Messages.get(Wheelchair.class, "jump_chasm_prompt"),
                                Messages.get(Chasm.class, "yes"),
                                Messages.get(Chasm.class, "no")) {

                            private float elapsed = 0f;

                            @Override
                            public synchronized void update() {
                                super.update();
                                elapsed += Game.elapsed;
                            }

                            @Override
                            public void hide() {
                                if (elapsed > 0.2f) {
                                    super.hide();
                                }
                            }

                            @Override
                            protected void onSelect(int index) {
                                if (index == 0 && elapsed > 0.2f) {
                                    // 确认跳跃
                                    performJump(hero, targetPos);
                                }
                            }
                        }
                );
            }
        });
    }

    private void performJump(Hero hero, int targetPos) {
        // 检查弹射起步效果Buff
        CatapultStartBuff catapultBuff = hero.buff(CatapultStartBuff.class);
        if (catapultBuff != null) {
            Buff.detach(hero, CatapultStartBuff.class);
        } else {
            charge--;
        }

        Talent.onArtifactUsed(hero);
        updateQuickslot();

        final int dest = targetPos;
        final float duration = speedDuration();

        hero.busy();
        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

        hero.sprite.jump(hero.pos, dest, 0, 0.1f, () -> {
            hero.move(dest);
            Dungeon.level.occupyCell(hero);
            Dungeon.observe();
            GameScene.updateFog();

            CellEmitter.get(hero.pos).burst(SparkParticle.FACTORY, 6);

            Buff.detach(hero, WheelchairRush.class);
            Buff.affect(hero, WheelchairRush.class, duration);

            hero.spendAndNext(1f);
            ((HeroSprite) hero.sprite).updateArmor();
        });
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new wheelchairCharge();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;
        chargeCap = maxCharge();
        if (charge < chargeCap) {
            partialCharge += 0.01f * amount;
            while (partialCharge >= 1f) {
                partialCharge--;
                charge++;
                if (charge >= chargeCap) {
                    charge = chargeCap;
                    break;
                }
            }
            updateQuickslot();
        }
    }

    /**
     * 月华没有强壮体质天赋且没有装备轮椅时就不能移动，这是猫叠的恶趣味
     */
    @SubscribeEvent(event = BeforeHeroMoveEvent.class)
    public static void onBeforeHeroMove(BeforeHeroMoveEvent event) {
        Hero hero = event.getHero();
        if (hero.heroClass == HeroClasses.MOONLIGHT && hero.pointsInTalent(Talent.STRONG_BODY) == 0) {
            if (!(hero.belongings.artifact instanceof Wheelchair)) {
                GLog.w(Messages.get(hero, "wheelchair_needed"));
                event.cancel();
            }
        }
    }

    /**
     * 订阅英雄移动完成事件
     * 装备轮椅时，记录移动距离用于升级
     */
    @SubscribeEvent(event = HeroMoveEvent.class)
    public static void onHeroMove(HeroMoveEvent event) {
        Hero hero = event.getHero();

        // 升级逻辑：仅当装备轮椅时执行
        if (hero.belongings.artifact instanceof Wheelchair) {
            Wheelchair wheelchair = (Wheelchair) hero.belongings.artifact;
            if (wheelchair.cursed) return;

            wheelchair.moveDistance++;
            int upgradeThreshold = 300 + 200 * wheelchair.level();
            if (wheelchair.moveDistance >= upgradeThreshold && wheelchair.level() < wheelchair.levelCap) {
                wheelchair.moveDistance -= upgradeThreshold;
                wheelchair.upgrade();
                wheelchair.chargeCap = wheelchair.maxCharge();
                GLog.p(Messages.get(wheelchair, "levelup"));
                wheelchair.updateQuickslot();
            }
        }
    }

    @Override
    public String desc() {
        String desc = Messages.get(this, "desc");

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n";
            if (cursed) {
                desc += Messages.get(this, "desc_cursed");
            } else {
                desc += Messages.get(this, "desc_equipped",
                        charge, chargeCap,
                        jumpRange(),
                        (int)speedDuration());
            }
        }

        return desc;
    }

    private static final String MOVE_DISTANCE = "move_distance";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MOVE_DISTANCE, moveDistance);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        moveDistance = bundle.getInt(MOVE_DISTANCE);
        chargeCap = maxCharge();
    }

    public class wheelchairCharge extends ArtifactBuff {

        @Override
        public boolean act() {
            chargeCap = maxCharge();

            if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
                // 充能速度：每 (80 - 2*level) 回合恢复1充能
                float chargeGain = 1f / Math.max(1, 80 - 2 * level());
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);

                partialCharge += chargeGain;

                while (partialCharge >= 1f) {
                    partialCharge--;
                    charge++;
                    if (charge >= chargeCap) {
                        charge = chargeCap;
                        break;
                    }
                }
                updateQuickslot();
            }

            spend(TICK);
            return true;
        }
    }
}
