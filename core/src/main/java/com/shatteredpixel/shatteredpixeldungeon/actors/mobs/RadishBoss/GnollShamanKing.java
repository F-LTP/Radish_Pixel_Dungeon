package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.FlashCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollShamanKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollShamanKing extends Mob implements Callback {

    private boolean toughnessTriggered = false;

    public int summonCooldownLimit;

    private int summonCooldown;

    {
        spriteClass = GnollShamanKingSprite.class;
        HT = HP = 150;
        defenseSkill = 12;

        maxLvl = 30;

        properties.add(Property.BOSS);
        properties.add(Property.GNOLL);
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

    private static final String CHECK_HP = "check_hp";
    private static final String SUMMON_COOLDOWNLIMIT = "summonCooldownLimit";
    private static final String SUMMON_COOLDOWN= "summonCooldown";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        bundle.put(CHECK_HP,toughnessTriggered);

        bundle.put(SUMMON_COOLDOWNLIMIT,summonCooldownLimit);
        bundle.put(SUMMON_COOLDOWN,summonCooldown);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        if (state != SLEEPING) BossHealthBar.assignBoss(this);
        if ((HP*2 <= HT) || toughnessTriggered) BossHealthBar.bleed(this,true);

        summonCooldownLimit = bundle.getInt(SUMMON_COOLDOWNLIMIT);
        summonCooldown = bundle.getInt(SUMMON_COOLDOWN);

        toughnessTriggered = bundle.getBoolean(CHECK_HP);
    }

    @Override
    protected boolean act() {
        if(buff(LaserAttack.class) == null){
            Buff.prolong( this, LaserAttack.class, 16f );
            summonGnollShadow(pos);
        }

        return super.act();
    }

    private void summonGnollShadow(int pos){
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if(mob instanceof GnollKing){
                if (((GnollKing) mob).summonCooldownLimit <= 0) {
                    ((GnollShamanKingSprite) sprite).shadow(pos);
                    Dungeon.hero.interrupt();
                    spend(1f);

                    Mob gs1 = new GnollKing.GnollGuardShadow();
                    gs1.state = gs1.HUNTING;
                    GameScene.add(gs1);
                    ScrollOfTeleportation.appear(gs1, 178);

                    Mob gs2 = new GnollKing.GnollGuardShadow();
                    gs2.state = gs2.HUNTING;
                    GameScene.add(gs2);
                    ScrollOfTeleportation.appear(gs2, 182);
                    ((GnollKing) mob).summonCooldownLimit = 2;
                } else {
                    boolean isRe = false;
                    for (Mob mob2 : Dungeon.level.mobs.toArray(new Mob[0])) {
                        if (mob2 instanceof GnollKing.GnollGuardShadow) {
                           ((GnollKing.GnollGuardShadow) mob2).reachAttack = true;
                           if(!isRe){
                               isRe = true;
                               GLog.n(Messages.get(this,"reach_attack"));
                           }
                        }
                    }
                }
            } else if(mob instanceof GnollShamanKing) {
                boolean isKingAlive = false;
                for (Mob mob4 : Dungeon.level.mobs.toArray(new Mob[0])){
                    if (mob4 instanceof GnollKing) {
                        isKingAlive = true;
                        break;
                    }
                }
                if(summonCooldownLimit <= 0 && !isKingAlive){
                    ((GnollShamanKingSprite) sprite).shadow(pos);
                    Dungeon.hero.interrupt();
                    spend(1f);
                    Mob gs1 = new GnollKing.GnollGuardShadow();
                    gs1.state = gs1.HUNTING;
                    GameScene.add(gs1);
                    ScrollOfTeleportation.appear(gs1,178);

                    Mob gs2 = new GnollKing.GnollGuardShadow();
                    gs2.state = gs2.HUNTING;
                    GameScene.add(gs2);
                    ScrollOfTeleportation.appear(gs2,182);
                    summonCooldownLimit  = 2;
                } else if(!isKingAlive) {
                    boolean isRe = false;
                    for (Mob mob2 : Dungeon.level.mobs.toArray(new Mob[0])) {
                        if (mob2 instanceof GnollKing.GnollGuardShadow) {
                            ((GnollKing.GnollGuardShadow) mob2).reachAttack = true;
                            if(!isRe){
                                isRe = true;
                                GLog.n(Messages.get(this,"reach_attack"));
                            }
                        }
                    }
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
            lock.addTime(info.getDamage()*1f);
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
            float baseReduction = 0.75f;

            int lostHealth = HT - HP;
            float extraReductionPercent = ((float) lostHealth / 10) * 2;
            float totalReductionFactor = baseReduction * (1 - extraReductionPercent / 100);

            float scaleFactor = AscensionChallenge.statModifier(this);
            int scaledDmg = Math.round(originalDmg / scaleFactor);

            int damageAfterScale = (int) (scaledDmg * scaleFactor);
            int damageAfterToughness = Math.round(damageAfterScale * totalReductionFactor);

            return Math.max(damageAfterToughness, 1);
        } else {
            return originalDmg;
        }
    }


    private boolean isPhysicalDamage(Object src) {
        return src instanceof Char;
    }

    private void checkFirstHalfHealthTrigger() {
        if (!toughnessTriggered && HP < HT * 0.5f) {
            toughnessTriggered = true;
            PotionOfCleansing.cleanse(this, 5f);
            Buff.affect(this, Haste.class,10);
            BossHealthBar.bleed(this,true);
        }
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
    public int drRoll() {
        return super.drRoll() + Char.combatRoll(2, 6);
    }

    @Override
    public int damageRoll() {
        return Char.combatRoll( 4, 12 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
    }


    @Override
    protected boolean canAttack( Char enemy ) {
        return super.canAttack(enemy)
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }

    protected boolean doAttack(Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

            return super.doAttack( enemy );

        } else {

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }
    }


    public void onZapComplete() {
        zap();
        next();
    }

    @Override
    public void call() {
        next();
    }

    public static class WitchLock extends FlavourBuff {}
    public static class RockDrops extends FlavourBuff { }
    public static class LaserAttack extends FlavourBuff {}

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        if(buff(GnollShamanKing.RockDrops.class) == null){
            Buff.prolong( this, GnollShamanKing.RockDrops.class, 20f );
            dropRocks(enemy);
            yell(Messages.get(this,"rock_down"));
        }
        return damage;
    }

    private void zap() {
        spend( 1f );

        Invisibility.dispel(this);
        Char enemy = this.enemy;
        if (hit( this, enemy, true )) {

            if(buff(RockDrops.class) == null){
                Buff.prolong( this, RockDrops.class, 20f );
                dropRocks(enemy);
                yell(Messages.get(this,"rock_down"));
            }

            if (buff(WitchLock.class) == null) {
                Buff.affect( enemy, Hex.class,        5f );
                Buff.affect( enemy, Vulnerable.class, 5f );
                Buff.affect( enemy, Weakness.class,   5f );

                Buff.prolong( this, WitchLock.class, 10f );
                ((GnollShamanKingSprite) sprite).eye(pos);
                if (enemy == Dungeon.hero) Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
            }

            int dmg = Char.combatRoll( 5, 20 );
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            enemy.damage(DamageInfo.of(dmg, DamageType.MAGICAL, this, new Shaman.EarthenBolt()));

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Badges.validateDeathFromEnemyMagic();
                Dungeon.fail( this );
                GLog.n( Messages.get(this, "bolt_kill") );
            }
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }

    public void dropRocks( Char target ) {

        ((GnollShamanKingSprite)sprite).rock( pos );
        Dungeon.hero.interrupt();
        final int rockCenter;

        //knock back 2 tiles if adjacent
        if (Dungeon.level.adjacent(pos, target.pos)){
            int oppositeAdjacent = target.pos + (target.pos - pos);
            Ballistica trajectory = new Ballistica(target.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, 2, false, false, this);
            if (target == Dungeon.hero){
                Dungeon.hero.interrupt();
            }
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 2));

            //knock back 1 tile if there's 1 tile of space
        } else if (fieldOfView[target.pos] && Dungeon.level.distance(pos, target.pos) == 2) {
            int oppositeAdjacent = target.pos + (target.pos - pos);
            Ballistica trajectory = new Ballistica(target.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, 1, false, false, this);
            if (target == Dungeon.hero){
                Dungeon.hero.interrupt();
            }
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 1));

            //otherwise no knockback
        } else {
            rockCenter = target.pos;
        }

        int safeCell;
        do {
            safeCell = rockCenter + PathFinder.NEIGHBOURS8[Random.Int(8)];
        } while (safeCell == pos
                || (Dungeon.level.solid[safeCell] && Random.Int(2) == 0)
                || (Blob.volumeAt(safeCell, CavesBossLevel.PylonEnergy.class) > 0 && Random.Int(2) == 0));

        ArrayList<Integer> rockCells = new ArrayList<>();

        int start = rockCenter - Dungeon.level.width() * 3 - 3;
        int pos;
        for (int y = 0; y < 7; y++) {
            pos = start + Dungeon.level.width() * y;
            for (int x = 0; x < 7; x++) {
                if (!Dungeon.level.insideMap(pos)) {
                    pos++;
                    continue;
                }
                //add rock cell to pos, if it is not solid, and isn't the safecell
                if (!Dungeon.level.solid[pos] && pos != safeCell && Random.Int(Dungeon.level.distance(rockCenter, pos)) == 0) {
                    rockCells.add(pos);
                }
                pos++;
            }
        }
        for (int i : rockCells){
            sprite.parent.add(new TargetedCell(i, 0xFF0000));
        }
        //don't want to overly punish players with slow move or attack speed
        Buff.append(this, DM300.FallingRockBuff.class, GameMath.gate(TICK, (int)Math.ceil(target.cooldown()), 3*TICK)).setRockPositions(rockCells);

    }

}
