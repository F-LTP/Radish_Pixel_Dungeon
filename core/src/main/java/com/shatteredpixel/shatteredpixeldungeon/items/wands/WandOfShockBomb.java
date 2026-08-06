package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
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
        return 3+buffedLvl();
    }

    @Override
    public int max(int lvl) {
        return 10+buffedLvl()*4;
    }

    @Override
    public String statsDesc() {
        if (levelKnown)
            return Messages.get(this, "stats_desc", min(),max());
        else
            return Messages.get(this, "stats_desc", min(0),max(0));
    }

    @Override
    public void onZap(Ballistica attack) {
        int targetPos = attack.collisionPos;

        // 先移除现有的爆炸区域
        ShockBombTracker existing = Dungeon.hero.buff(ShockBombTracker.class);
        if (existing != null) {
            existing.detach();
        }

        // 创建新的爆炸区域
        ShockBombTracker tracker = Buff.affect(Dungeon.hero, ShockBombTracker.class);
        tracker.damage = damageRoll();
        tracker.setPos(targetPos, buffedLvl());
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        float triggerChance = 15 + (float) buffedLvl() /4 + buffedLvl();
        triggerChance = Math.min(triggerChance, 100); // 限制最大概率为100%

        if (Random.Int(100) < triggerChance) {
            // 先移除现有的爆炸区域
            ShockBombTracker existing = Dungeon.hero.buff(ShockBombTracker.class);
            if (existing != null) {
                existing.detach();
            }

            // 创建新的爆炸区域
            ShockBombTracker tracker = Buff.affect(Dungeon.hero, ShockBombTracker.class);
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
        private int level;
        private int turnsLeft = 1;
        private boolean[] fieldOfView;
        private ShockBombVFX halo;

        @Override
        public boolean act() {
            if (turnsLeft > 0) {
                // 显示倒计时
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                FloatingText.show(p.x, p.y, pos, "WARNING!!!", CharSprite.NEGATIVE);

                // 显示圆形范围标记
                for (int i = 0; i < PathFinder.CIRCLE5x.length; i++) {
                    int cell = pos + PathFinder.CIRCLE5x[i];
                    if (Dungeon.level.insideMap(cell)) {
                        if (target != null && target.sprite != null) {
                            target.sprite.parent.add(new TargetedCell(cell, Window.RADISH));
                        }
                    }
                }

                // 添加视觉效果
                if (halo == null) {
                    halo = new ShockBombVFX();
                    halo.point(p.x, p.y);
                    halo.hardlight(0.5f, 0.2f, 0f);
                    GameScene.effect(halo);
                }

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
            for (int i = 0; i < PathFinder.CIRCLE5x.length; i++) {
                int cell = pos + PathFinder.CIRCLE5x[i];
                runExplode(damage, cell);
            }

            Dungeon.observe();
            GameScene.updateFog();
        }

        private static void runExplode(int damage, int cell) {
            if (Dungeon.level.insideMap(cell)) {
                Char ch = Actor.findChar(cell);
                if (ch != null) {
                    if (ch.alignment == Char.Alignment.ENEMY || (ch instanceof Mimic && ch.alignment == Char.Alignment.NEUTRAL)) {
                        ch.damage(damage, new DM100.LightningBolt());
                    }
                    if (ch.alignment == Char.Alignment.ALLY) {
                        int pushTarget = cell;
                        for (int j = 0; j < 2; j++) {
                            int nextCell = cell + PathFinder.CIRCLE5x[Random.Int(8)];
                            if (Dungeon.level.passable[nextCell] && Actor.findChar(nextCell) == null) {
                                pushTarget = nextCell;
                            }
                        }
                        if (pushTarget != cell) {
                            Actor.addDelayed(new Pushing(ch, cell, pushTarget), -1);
                            ch.pos = pushTarget;
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
        }

        @Override
        public void fx(boolean on) {
            if (on && (halo == null || halo.parent == null)) {
                halo = new ShockBombVFX();
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                halo.point(p.x, p.y);
                halo.hardlight(1, 0.2f, 0f);
                GameScene.effect(halo);
            }
            super.fx(on);
        }

        // 序列化相关
        private static final String POS = "pos";
        private static final String LEVEL = "level";
        private static final String LEFT = "left";

        private static final String DAMAGE = "damage";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(POS, pos);
            bundle.put(LEVEL, level);
            bundle.put(LEFT, turnsLeft);
            bundle.put(DAMAGE, damage);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            pos = bundle.getInt(POS);
            level = bundle.getInt(LEVEL);
            turnsLeft = bundle.getInt(LEFT);
            damage = bundle.getInt(DAMAGE);
        }

    }

    public static class ShockMageBombTracker extends Buff {
        public int pos;
        private int level;
        private int turnsLeft = 1;
        private boolean[] fieldOfView;
        private ShockBombVFX halo;

        @Override
        public boolean act() {
            if (turnsLeft > 0) {
                // 显示倒计时
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                FloatingText.show(p.x, p.y, pos, "NUKE!!!", CharSprite.NEGATIVE);

                // 显示圆形范围标记
                for (int i = 0; i < PathFinder.CIRCLE7x.length; i++) {
                    int cell = pos + PathFinder.CIRCLE7x[i];
                    if (Dungeon.level.insideMap(cell)) {
                        if (target != null && target.sprite != null) {
                            target.sprite.parent.add(new TargetedCell(cell, Window.RADISH));
                        }
                    }
                }

                // 添加视觉效果
                if (halo == null) {
                    halo = new ShockBombVFX();
                    halo.point(p.x, p.y);
                    halo.hardlight(0.2f, 0.2f, 0f);
                    GameScene.effect(halo);
                }

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
                ShockBombTracker.runExplode(damage, cell);
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
        }

        @Override
        public void fx(boolean on) {
            if (on && (halo == null || halo.parent == null)) {
                halo = new ShockBombVFX();
                PointF p = DungeonTilemap.raisedTileCenterToWorld(pos);
                halo.point(p.x, p.y);
                halo.hardlight(0.2f, 1f, 1f);
                GameScene.effect(halo);
            }
            super.fx(on);
        }

        // 序列化相关
        private static final String POS = "pos";
        private static final String LEVEL = "level";
        private static final String LEFT = "left";

        private static final String DAMAGE = "damage";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(POS, pos);
            bundle.put(LEVEL, level);
            bundle.put(LEFT, turnsLeft);
            bundle.put(DAMAGE, damage);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            pos = bundle.getInt(POS);
            level = bundle.getInt(LEVEL);
            turnsLeft = bundle.getInt(LEFT);
            damage = bundle.getInt(DAMAGE);
        }

    }
}
