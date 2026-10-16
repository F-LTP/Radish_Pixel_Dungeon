package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.FlashCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class GnollKing extends Mob {

    private int stickCooldown;
    public int summonCooldownLimit;
    private int summonCooldown;
    private boolean toughnessTriggered = false;

    {
        spriteClass = GnollKingSprite.class;
        HT = HP = 150;
        defenseSkill = 15;

        maxLvl = 30;

        HUNTING = new Hunting();

        properties.add(Property.BOSS);
        properties.add(Property.GNOLL);
    }

    private int lastEnemyPos = -1;

    @Override
    public float attackDelay() {
        return 1.5f;
    }

    @Override
    protected boolean act() {

        boolean result = super.act();

        if (state == WANDERING){
            leapPos = -1;
            charging = false;
        }

        AiState lastState = state;

        if (paralysed <= 0) leapCooldown --;

        if (!(lastState == WANDERING && state == HUNTING)) {
            if (enemy != null) {
                lastEnemyPos = enemy.pos;
            } else {
                lastEnemyPos = Dungeon.hero.pos;
            }
        }

        if(stickCooldown <= 0){
            boolean isRe = false;
            for ( Mob mob : Dungeon.level.mobs.toArray(new Mob[0]) ){
                if( mob != null ){
                    if( mob.buff(EyeAttack.class) == null ){
                        Buff.affect(mob, EyeAttack.class,5f);
                        stickCooldown = 15;
                        if(!isRe){
                            isRe = true;
                            GLog.n(Messages.get(this,"eye"));
                        }

                    }
                }
            }
        } else {
            stickCooldown--;
        }

        if(summonCooldown <= 0){
            summonCooldown = 25;
            summonGnollShadow(pos);
        } else {
            summonCooldown--;
        }


        return result;
    }

    private void summonGnollShadow(int pos){
        if(summonCooldownLimit <= 0){
            ((GnollKingSprite)sprite).shadow( pos );
            Mob gs1 = new GnollGuardShadow();
            gs1.state = gs1.HUNTING;
            GameScene.add(gs1);
            ScrollOfTeleportation.appear(gs1,178);

            Mob gs2 = new GnollGuardShadow();
            gs2.state = gs2.HUNTING;
            GameScene.add(gs2);
            ScrollOfTeleportation.appear(gs2,182);
            summonCooldownLimit  = 2;
        } else {
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if (mob instanceof GnollGuardShadow) {
                    Buff.affect(mob, Barrier.class).setShield( 20 );
                }
            }
        }

    }

    public static class EyeAttack extends FlavourBuff {

        public static final float DURATION = 5f;

        {
            announced = true;
            type = buffType.NEGATIVE;
        }

        @Override
        public String icon() {
            return BuffIndicator.MIND_VISION;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(Window.RADISH);
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }


        @Override
        public boolean act() {
            if(target instanceof Mob) {
                ((Mob) target).beckon(Dungeon.hero.pos);
            }
            return super.act();
        }
    }

    public static class GnollGuardShadow extends GnollGuard {

        public boolean reachAttack = false;

        @Override
        protected boolean isDamageable() {
            return true;
        }

        {
            spriteClass = GnollGuardSprite.GnollGuardShadowSprite.class;
            maxLvl = -1;
        }

        @Override
        protected boolean act() {
            if(Statistics.gnoll_boss >= 2){
                damage(DamageInfo.of(1000, DamageType.LIGHTNING, this, new DM100.LightningBolt()));
            }
            return super.act();
        }

        private static final String REACH_ATTACK = "REACH_ATTACK";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(REACH_ATTACK,reachAttack);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            reachAttack = bundle.getBoolean(REACH_ATTACK);
        }

        @Override
        public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {
            boolean result = super.attack( enemy, dmgMulti, dmgBonus, accMulti );
            if(reachAttack){
                reachAttack = false;
            }
            return result;
        }

        @Override
        public void damage(DamageInfo info) {
            super.damage(info);
            LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
            if (lock != null && !isImmune(info.getSource().getClass()) && !isInvulnerable(info.getSource().getClass())){
                lock.addTime(info.getDamage()*1.25f);
            }
        }

        @Override
        protected boolean getCloser( int target ) {
            if (state == HUNTING && reachAttack) {
                return enemySeen && getFurther( target );
            } else {
                return super.getCloser( target );
            }
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            if(reachAttack){
                return !Dungeon.level.adjacent( pos, enemy.pos )
                        && (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
            } else {
                return super.canAttack(enemy);
            }
        }

        @Override
        public void die( Object cause ) {
            super.die(cause);
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if (mob instanceof GnollKing) {
                    ((GnollKing) mob).summonCooldownLimit--;
                } else if(mob instanceof GnollShamanKing){
                    ((GnollShamanKing) mob).summonCooldownLimit--;
                }
            }
        }

    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Char.combatRoll(0, 6);
    }

    @Override
    public int damageRoll() {
        return Char.combatRoll( 10, 25 );
    }

    private static final String STICK_COOLDOWN = "stick_cooldown";
    private static final String LAST_ENEMY_POS = "last_enemy_pos";
    private static final String LEAP_POS = "leap_pos";
    private static final String LEAP_CD = "leap_cd";
    private static final String CHARGING = "charging";
    private static final String CHECK_HP = "check_hp";
    private static final String SUMMON_COOLDOWNLIMIT = "summonCooldownLimit";
    private static final String SUMMON_COOLDOWN= "summonCooldown";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STICK_COOLDOWN, stickCooldown);

        bundle.put(LAST_ENEMY_POS, lastEnemyPos);
        bundle.put(LEAP_POS, leapPos);
        bundle.put(LEAP_CD, leapCooldown);
        bundle.put(CHARGING, charging);

        bundle.put(CHECK_HP,toughnessTriggered);

        bundle.put(SUMMON_COOLDOWNLIMIT,summonCooldownLimit);
        bundle.put(SUMMON_COOLDOWN,summonCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stickCooldown = bundle.getInt(STICK_COOLDOWN);

        lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
        leapPos = bundle.getInt(LEAP_POS);
        leapCooldown = bundle.getFloat(LEAP_CD);
        charging = bundle.getBoolean(CHARGING);

        toughnessTriggered = bundle.getBoolean(CHECK_HP);

        summonCooldownLimit = bundle.getInt(SUMMON_COOLDOWNLIMIT);
        summonCooldown = bundle.getInt(SUMMON_COOLDOWN);
        if (state != SLEEPING) BossHealthBar.assignBoss(this);
        if ((HP*2 <= HT) || toughnessTriggered) BossHealthBar.bleed(this,true);
    }

    @Override
    public boolean canAttack(Char target) {
        return Dungeon.level.trueDistance(pos, target.pos) <= 2 || super.canAttack(target);
    }

    @Override
    protected boolean getCloser( int target ) {
        if (Dungeon.level.trueDistance(pos, target) <= 2) {
            return false;
        }
        return super.getCloser(target);
    }

    private int leapPos = -1;
    private float leapCooldown = 0;
    private boolean charging = false;
    private List<Char> leapTargets = new ArrayList<>();
    private List<Integer> leapPath = new ArrayList<>();

    public class Hunting extends Mob.Hunting {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {

            if (leapPos != -1){

                leapCooldown = 8;

                if (rooted){
                    leapPos = -1;
                    charging = false;
                    leapTargets.clear();
                    leapPath.clear();
                    return true;
                }

                Ballistica b = new Ballistica(pos, leapPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                int targetPos = b.collisionPos;
                int distance = (int) Math.min(4, Dungeon.level.trueDistance(pos, targetPos));
                List<Integer> path = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).subPath(1, distance);
                int endPos = path.get(path.size() - 1);

                // 保存突进路径
                leapPath.clear();
                leapPath.addAll(path);

                // 收集沿途的所有目标
                leapTargets.clear();
                for (int p : path) {
                    Char victim = Actor.findChar(p);
                    if (victim != null && victim != GnollKing.this) {
                        leapTargets.add(victim);
                    }
                }

                sprite.visible = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[targetPos] || Dungeon.level.heroFOV[endPos];
                ((GnollKingSprite)sprite).stick( endPos );
                sprite.jump(pos, leapPos, 0f, 0.5f, new Callback() {
                    @Override
                    public void call() {
                        for (Char victim : leapTargets) {
                            if(!(victim instanceof GnollShamanKing)){
                                int damage = 25;
                                victim.damage(DamageInfo.of(damage, DamageType.PHYSICAL, GnollKing.this, GnollKing.this));
                                victim.sprite.flash();
                                Sample.INSTANCE.play(Assets.Sounds.HIT);

                                if (victim.isAlive()) {
                                    victim.sprite.showStatus(CharSprite.NEUTRAL, victim.defenseVerb());
                                }
                            }
                        }
                        leapTargets.clear();
                        leapPath.clear();

                        // 检查终点是否有其他角色
                        Char endPosChar = Actor.findChar(leapPos);
                        if (endPosChar != null && endPosChar != GnollKing.this) {
                            // 找到终点周围8个方向中可用的随机位置
                            int newPos = -1;
                            List<Integer> validPositions = new ArrayList<>();
                            for (int i : PathFinder.NEIGHBOURS8) {
                                int p = leapPos + i;
                                // 检查坐标是否在地图范围内、可通行且没有角色
                                if (p >= 0 && p < Dungeon.level.length() &&
                                        Dungeon.level.passable[p] &&
                                        Actor.findChar(p) == null) {
                                    validPositions.add(p);
                                }
                            }

                            if (!validPositions.isEmpty()) {
                                // 随机选择一个有效的位置
                                newPos = validPositions.get(Random.Int(validPositions.size()));
                                Actor.add(new Pushing(GnollKing.this, leapPos, newPos));
                            } else {
                                // 如果没有可用位置，就不移动
                                newPos = pos;
                            }

                            pos = newPos;
                        } else {
                            // 如果终点没有其他角色，直接移动到终点
                            pos = leapPos;
                        }

                        Dungeon.level.occupyCell(GnollKing.this);
                        leapPos = -1;
                        charging = false;
                        sprite.idle();
                        next();
                    }
                });
                return false;
            }

            enemySeen = enemyInFOV;
            if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

                return doAttack( enemy );

            } else {

                if (enemyInFOV) {
                    target = enemy.pos;
                } else if (enemy == null) {
                    state = WANDERING;
                    target = Dungeon.level.randomDestination( GnollKing.this );
                    return true;
                }

                if (leapCooldown <= 0 && enemyInFOV && !rooted
                        && Dungeon.level.distance(pos, enemy.pos) >= 1) {

                    int targetPos = enemy.pos;
                    if (lastEnemyPos != enemy.pos){
                        int closestIdx = 0;
                        for (int i = 1; i < PathFinder.CIRCLE8.length; i++){
                            if (Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[i])
                                    < Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[closestIdx])){
                                closestIdx = i;
                            }
                        }
                        targetPos = enemy.pos + PathFinder.CIRCLE8[(closestIdx+4)%8];
                    }

                    Ballistica b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    if (b.collisionPos != targetPos && targetPos != enemy.pos){
                        targetPos = enemy.pos;
                        b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    }
                    if (b.collisionPos == targetPos){
                        int distance = (int) Math.min(4, Dungeon.level.trueDistance(pos, targetPos));
                        List<Integer> path = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).subPath(1, distance);
                        int actualEndPos = path.get(path.size() - 1);

                        if (!charging) {
                            charging = true;
                            leapPos = actualEndPos;

                            leapPath.clear();
                            leapPath.addAll(path);

                            spend(TICK);

                            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos]){
                                GLog.w(Messages.get(GnollKing.this, "leap"));

                                for (int p : leapPath) {
                                    sprite.parent.addToBack(new TargetedCell(p, 0xFF0000));
                                }

                                ((GnollKingSprite)sprite).stickAttack( leapPos );
                                Dungeon.hero.interrupt();
                            }
                            return true;
                        } else {
                            spend(GameMath.gate(attackDelay(), (int)Math.ceil(enemy.cooldown()), 3*attackDelay()));
                            return true;
                        }
                    }
                }

                int oldPos = pos;
                if (target != -1 && getCloser( target )) {

                    spend( 1 / speed() );
                    return moveSprite( oldPos,  pos );

                } else {
                    spend( TICK );
                    if (!enemyInFOV) {
                        sprite.showLost();
                        state = WANDERING;
                        target = Dungeon.level.randomDestination( GnollKing.this );
                    }
                    return true;
                }
            }
        }

    }


    @Override
    public void damage(DamageInfo info) {
        int finalDmg = calculateToughnessDamage(info.getDamage(), info.getSource());
        super.damage(DamageInfo.of(finalDmg, info.getType(), info.getAttacker(), info.getSource()));
        checkFirstHalfHealthTrigger();

        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(info.getSource().getClass()) && !isInvulnerable(info.getSource().getClass())){
            lock.addTime(info.getDamage()*1.5f);
        }
    }

    /**
     * 计算坚韧不拔被动的减伤后伤害
     * @param originalDmg 原始伤害值
     * @param src 伤害来源
     * @return 减伤后的最终伤害
     */
    private int calculateToughnessDamage(int originalDmg, Object src) {
        if (!isPhysicalDamage(originalDmg)) {
            return originalDmg;
        }

        float baseReduction = 0.75f;

        int lostHealth = HT - HP;
        float extraReductionPercent = ((float) lostHealth / 10) * 2;
        float totalReductionFactor = baseReduction * (1 - extraReductionPercent / 100);

        float scaleFactor = AscensionChallenge.statModifier(this);
        int scaledDmg = Math.round(originalDmg / scaleFactor);

        int damageAfterScale = (int)(scaledDmg * scaleFactor);
        int damageAfterToughness = Math.round(damageAfterScale * totalReductionFactor);

        return Math.max(damageAfterToughness, 1);
    }

    /**
     * 检查是否首次血量低于50%，触发被动效果：5回合全面净化 + 30点恒动
     */
    private void checkFirstHalfHealthTrigger() {
        if (!toughnessTriggered && HP < HT * 0.5f) {
            toughnessTriggered = true;
            PotionOfCleansing.cleanse(this, 5f);
            Buff.affect(this, Kinetic.ConservedDamage.class).setBonus(30);
            BossHealthBar.bleed(this,true);
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    Music.INSTANCE.fadeOut(0.5f, new Callback() {
                        @Override
                        public void call() {
                            Music.INSTANCE.play(Assets.Music.CAVES_BOSS_FINALE, true);
                        }
                    });
                }
            });
        }
    }

    /**
     * 判断是否为物理伤害（需根据项目实际逻辑实现）
     * @param src 伤害来源
     * @return true=物理伤害，false=魔法/元素等其他伤害
     */
    private boolean isPhysicalDamage(Object src) {
        return src instanceof Char;
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );

        // 双王各 100% 掉落2个闪晶
        if(Dungeon.LimitedDrops.FLASH_CRYSTAL_KING.count < 6){
            Dungeon.LimitedDrops.FLASH_CRYSTAL_KING.count++;
            Dungeon.level.drop(new FlashCrystal().quantity(2), pos).sprite.drop();
        }

        Statistics.gnoll_boss++;

        if(Statistics.gnoll_boss >= 2){
            Badges.validateRectorUnlock();
            GameScene.bossSlain();
            Dungeon.level.unseal();
            Dungeon.level.drop( new Gold( 1500 ), pos ).sprite.drop();
            Dungeon.level.drop( new ScrollOfUpgrade(), pos ).sprite.drop();
            Statistics.bossScores[2] += 3000;
        }

        yell( Messages.get(this, "defeated") );
    }

    @Override
    public void notice() {
        super.notice();
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            yell(Messages.get(this, "notice"));
            for (Char ch : Actor.chars()){
                if (ch instanceof DriedRose.GhostHero){
                    ((DriedRose.GhostHero) ch).sayBoss();
                }
            }
        }
    }


}
