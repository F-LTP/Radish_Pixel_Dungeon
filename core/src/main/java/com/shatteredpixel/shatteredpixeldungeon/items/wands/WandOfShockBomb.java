package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Halo;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfShockBomb extends DamageWand {
    {
        image = ItemSpriteSheet.WAND_BOMBWAVES;

        collisionProperties = Ballistica.PROJECTILE;
    }

    @Override
    public int min(int lvl) {
        return 3+lvl;
    }

    @Override
    public int max(int lvl) {
        return 10+lvl*4;
    }

    @Override
    public String statsDesc() {
        if (levelKnown)
            return Messages.get(this, "stats_desc", rollMin(buffedLvl()),rollMax(buffedLvl()));
        else
            return Messages.get(this, "stats_desc", min(0),max(0));
    }

    @Override
    public void onZap(Ballistica attack) {
        int targetPos = attack.collisionPos;

        // 先移除现有的爆炸区域
        ShockBombTracker tracker = Buff.append(Dungeon.hero, ShockBombTracker.class);
        tracker.damage = damageRoll();
        tracker.setPos(targetPos, buffedLvl());
        tracker.fromZap = true;
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        float triggerChance = 15 + (float) buffedLvl() /4 + buffedLvl();
        //蓄能打击天赋会放大法杖近战附带效果的触发率，其它法杖都乘了这个倍率
        triggerChance *= procChanceMultiplier(attacker);
        triggerChance = Math.min(triggerChance, 100); // 限制最大概率为100%

        if (Random.Int(100) < triggerChance) {
            // 先移除现有的爆炸区域
            ShockBombTracker tracker = Buff.append(Dungeon.hero, ShockBombTracker.class);
            tracker.damage = damageRoll();
            tracker.setPos(defender.pos, buffedLvl());
        }
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        // 使用橙红色作为火花颜色
        particle.color(ColorMath.random(0xFF6600, 0xFFCC00));  // 随机橙黄色
        particle.am = 1f;

        // 设置初始大小
        particle.setSize(0.2f, 1.2f);

        // 设置生命周期
        particle.setLifespan(1.5f);

        // 随机四散的角度和速度
        float angle = Random.Float(360f);
        float speed = Random.Float(3f, 5f);  // 随机速度

        float rad = angle * (float)Math.PI / 180f;

        // 设置初始速度，让火花向外飞散
        particle.speed.x = (float)Math.cos(rad) * speed;
        particle.speed.y = (float)Math.sin(rad) * speed;

        // 不设置重力，让火花直线飞行
        particle.acc.x = 0;
        particle.acc.y = 0;

        // 添加一些随机性
        particle.shuffleXY(0.1f);

        // 设置初始位置在法杖周围
        float dst = Random.Float(11f);
        particle.x -= dst;
        particle.y += dst;
    }

    public static class ShockBombTracker extends Buff {
        public int pos;
        //放置时所在的楼层，由 setPos 记录。旧存档里的 tracker 没有这两项，branchId 保持 null
        private int depth = -1;
        private String branchId = null;
        private int level;
        private int turnsLeft = 1;
        private boolean[] fieldOfView;
        private ShockBombVFX halo;
        //是不是直接施放法杖放下的。只有这种爆炸算一次法杖命中（wandProc），
        //战斗法师近战触发的不算，和其它法杖的近战附带效果一致
        private boolean fromZap = false;

        @Override
        public boolean act() {
            if (!onFloor(depth, branchId)) {
                detach();
                return true;
            }

            if (turnsLeft > 0) {
                // 显示倒计时
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                FloatingText.show(p.x, p.y, pos, Messages.get(WandOfShockBomb.class, "warning"), CharSprite.NEGATIVE);

                // 显示圆形范围标记。光圈不在这里建：挂上 tracker 或进入场景时 fx() 已经建好了
                markBlastArea(pos, PathFinder.CIRCLE5x, 2);

                turnsLeft--;
            } else {
                explode();
                detach();
            }

            spend(TICK);
            return true;
        }

        //在爆炸范围的每一格上画一个预警标记，大约两秒后自己淡出
        private static void markBlastArea(int center, int[] offsets, int radius) {
            for (int offset : offsets) {
                int cell = center + offset;
                if (inBlastArea(center, cell, radius)) {
                    GameScene.effect(new TargetedCell(cell, Window.RADISH));
                }
            }
        }

        private void explode() {
            // 计算伤害：3+等级-10+等级*4
            int damage = wandDamage();

            // 播放音效和震动
            Sample.INSTANCE.play(Assets.Sounds.BLAST);
            PixelScene.shake(3, 0.5f);

            // 使用相同的圆形范围
            for (int i = 0; i < PathFinder.CIRCLE5x.length; i++) {
                int cell = pos + PathFinder.CIRCLE5x[i];
                if (inBlastArea(pos, cell, 2)) {
                    if (fromZap) {
                        //要赶在伤害之前：目标被炸死之后再挂灵魂标记就没有意义了
                        Char ch = Actor.findChar(cell);
                        if (ch != null && takesBlastDamage(ch)) {
                            wandProc(ch, level, 1);
                        }
                    }
                    runExplode(damage, cell, pos);
                }
            }

            Dungeon.observe();
            GameScene.updateFog();
        }

        private static boolean takesBlastDamage(Char ch) {
            return ch.alignment == Char.Alignment.ENEMY || (ch instanceof Mimic && ch.alignment == Char.Alignment.NEUTRAL);
        }

        //CIRCLE5x / CIRCLE7x 是按格子下标算的偏移，爆心贴着地图左右边界时会绕到相邻行的另一头，
        //所以除了要在地图内，还得按坐标确认这一格真的在爆心周围
        private static boolean inBlastArea(int center, int cell, int radius) {
            return Dungeon.level.insideMap(cell) && Dungeon.level.distance(center, cell) <= radius;
        }

        //tracker 挂在英雄身上，会跟着英雄换层，而 pos 只是放置时那一层的格子下标，
        //到了别的楼层指向的是一个不相干的位置，不能在那里显示预警或结算爆炸。
        //branchId 为 null 说明是旧存档里没有记录楼层的 tracker，只能当作还在原来那一层
        private static boolean onFloor(int depth, String branchId) {
            return branchId == null || (depth == Dungeon.depth && branchId.equals(Dungeon.branchId));
        }

        private static void runExplode(int damage, int cell, int center) {
            if (Dungeon.level.insideMap(cell)) {
                Char ch = Actor.findChar(cell);
                if (ch != null) {
                    if (takesBlastDamage(ch)) {
                        ch.damage(new DamageInfo(damage, DamageType.LIGHTNING, null, null, new DM100.LightningBolt()));
                    }
                    if (ch.alignment == Char.Alignment.ALLY) {
                        //正好站在爆心上的友军没有「向外」的方向，原地不动。
                        //这个判断不能省：起点和终点相同的弹道会退化成一条指向左上的斜线
                        if (cell != center) {
                            //和激流陷阱推开周围目标的写法相同：先从爆心穿过友军拉一条线定出方向，再从友军脚下沿这条线走弹道。
                            //落点交给 throwChar 判定，不会越界也不会穿墙，落地后照常触发地格效果；友军不结算撞墙伤害
                            Ballistica trajectory = new Ballistica(center, cell, Ballistica.STOP_TARGET);
                            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
                            WandOfBlastWave.throwChar(ch, trajectory, 2, true, false, WandOfShockBomb.class);
                        }
                        WandOfBlastWave.BlastWave.blast(cell,2);
                    }
                }
            }
        }

        public int damage;

        public int wandDamage() {
            return damage;
        }

        public void detach() {
            super.detach();
            if (halo != null) {
                halo.killAndErase();
            }
        }

        public class ShockBombVFX extends Halo {
            @Override
            public void update() {
                am = brightness + 0.01f * (float) Math.cos(20);
                scale.set((radius / 3 + (float) Math.cos(20 * Game.timeTotal)) / RADIUS);
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                point(p.x, p.y);
                super.update();
            }
        }

        public void setPos(int pos, int level) {
            this.pos = pos;
            this.level = level;
            this.depth = Dungeon.depth;
            this.branchId = Dungeon.branchId;
        }

        @Override
        public void fx(boolean on) {
            if (on && onFloor(depth, branchId) && (halo == null || halo.parent == null)) {
                halo = new ShockBombVFX();
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                halo.point(p.x, p.y);
                halo.hardlight(1, 0.2f, 0f);
                GameScene.effect(halo);
                //读档或重新进入这一层时场景是重建的，之前画的预警标记已经不在了。
                //预警报过、只差引爆的话补画一次，否则只看得到光圈，看不出会炸到哪几格
                if (turnsLeft <= 0) markBlastArea(pos, PathFinder.CIRCLE5x, 2);
            }
            super.fx(on);
        }

        // 序列化相关
        private static final String POS = "pos";
        private static final String DEPTH = "depth";
        private static final String BRANCH_ID = "branch_id";
        private static final String LEVEL = "level";
        private static final String LEFT = "left";

        private static final String DAMAGE = "damage";
        private static final String FROM_ZAP = "from_zap";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(POS, pos);
            if (branchId != null) {
                bundle.put(DEPTH, depth);
                bundle.put(BRANCH_ID, branchId);
            }
            bundle.put(LEVEL, level);
            bundle.put(LEFT, turnsLeft);
            bundle.put(DAMAGE, damage);
            bundle.put(FROM_ZAP, fromZap);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            pos = bundle.getInt(POS);
            if (bundle.contains(BRANCH_ID)) {
                depth = bundle.getInt(DEPTH);
                branchId = bundle.getString(BRANCH_ID);
            }
            level = bundle.getInt(LEVEL);
            turnsLeft = bundle.getInt(LEFT);
            damage = bundle.getInt(DAMAGE);
            //改动前的存档里没有这个键，按「不是施放放下的」处理
            fromZap = bundle.contains(FROM_ZAP) && bundle.getBoolean(FROM_ZAP);
        }

    }

    public static class ShockMageBombTracker extends Buff {
        public int pos;
        private int depth = -1;
        private String branchId = null;
        private int level;
        private int turnsLeft = 1;
        private boolean[] fieldOfView;
        private ShockBombVFX halo;

        @Override
        public boolean act() {
            if (!ShockBombTracker.onFloor(depth, branchId)) {
                detach();
                return true;
            }

            if (turnsLeft > 0) {
                // 显示倒计时
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                FloatingText.show(p.x, p.y, pos, Messages.get(WandOfShockBomb.class, "nuke"), CharSprite.NEGATIVE);

                // 显示圆形范围标记。光圈不在这里建：挂上 tracker 或进入场景时 fx() 已经建好了
                ShockBombTracker.markBlastArea(pos, PathFinder.CIRCLE7x, 3);

                turnsLeft--;
            } else {
                explode();
                detach();
            }

            spend(TICK);
            return true;
        }

        private void explode() {
            // 计算伤害：3+等级-10+等级*4
            int damage = wandDamage();

            // 播放音效和震动
            Sample.INSTANCE.play(Assets.Sounds.BLAST);
            PixelScene.shake(3, 0.5f);

            // 使用相同的圆形范围
            for (int i = 0; i < PathFinder.CIRCLE7x.length; i++) {
                int cell = pos + PathFinder.CIRCLE7x[i];
                if (ShockBombTracker.inBlastArea(pos, cell, 3)) {
                    ShockBombTracker.runExplode(damage, cell, pos);
                }
            }

            Dungeon.observe();
            GameScene.updateFog();
        }

        public int damage;

        public int wandDamage() {
            return damage;
        }

        public void detach() {
            super.detach();
            if (halo != null) {
                halo.killAndErase();
            }
        }

        public class ShockBombVFX extends Halo {
            @Override
            public void update() {
                am = brightness + 0.01f * (float) Math.cos(20);
                scale.set((radius / 2 + (float) Math.cos(20 * Game.timeTotal)) / RADIUS);
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                point(p.x, p.y);
                super.update();
            }
        }

        public void setPos(int pos, int level) {
            this.pos = pos;
            this.level = level;
            this.depth = Dungeon.depth;
            this.branchId = Dungeon.branchId;
        }

        @Override
        public void fx(boolean on) {
            if (on && ShockBombTracker.onFloor(depth, branchId) && (halo == null || halo.parent == null)) {
                halo = new ShockBombVFX();
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                halo.point(p.x, p.y);
                halo.hardlight(0.2f, 1f, 1f);
                GameScene.effect(halo);
                //同上：读档后把预警标记补画一次
                if (turnsLeft <= 0) ShockBombTracker.markBlastArea(pos, PathFinder.CIRCLE7x, 3);
            }
            super.fx(on);
        }

        // 序列化相关
        private static final String POS = "pos";
        private static final String DEPTH = "depth";
        private static final String BRANCH_ID = "branch_id";
        private static final String LEVEL = "level";
        private static final String LEFT = "left";

        private static final String DAMAGE = "damage";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(POS, pos);
            if (branchId != null) {
                bundle.put(DEPTH, depth);
                bundle.put(BRANCH_ID, branchId);
            }
            bundle.put(LEVEL, level);
            bundle.put(LEFT, turnsLeft);
            bundle.put(DAMAGE, damage);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            pos = bundle.getInt(POS);
            if (bundle.contains(BRANCH_ID)) {
                depth = bundle.getInt(DEPTH);
                branchId = bundle.getString(BRANCH_ID);
            }
            level = bundle.getInt(LEVEL);
            turnsLeft = bundle.getInt(LEFT);
            damage = bundle.getInt(DAMAGE);
        }

    }
}
