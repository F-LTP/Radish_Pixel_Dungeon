package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.BonePile;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PrisonArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneClaw;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.ClusteredSkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class ClusteredSkeleton extends Mob {
    {
        spriteClass = ClusteredSkeletonSprite.class;

        HP = HT = 100;
        defenseSkill = 10;


        EXP = 7;
        maxLvl = 17;

        properties.add(Property.UNDEAD);
        properties.add(Property.INORGANIC);

        loot = new BoneClaw();
        lootChance = 0.25f;
    }

    private boolean isDead = false;


    @Override
    public float attackDelay() {
        return super.attackDelay() * 1.5f;
    }

    public int damageRoll() {
        return Random.NormalIntRange( 20, 35 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 35;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(2, 6);
    }

    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(ClusteredSkeleton.class, "kill"));
    }

    @Override
    public Item createLoot() {
        Dungeon.LimitedDrops.CLUSTERED_SKELETON_WEP.count++;
        loot = Random.oneOf(new BoneClaw());
        if(super.createLoot() instanceof Weapon)
            super.createLoot().getCurse(true);
        return super.createLoot();
    }
    @Override
    public float lootChance() {
        return super.lootChance() * ((float) 1 /(1+Dungeon.LimitedDrops.GRUDGE_WEP.count));
    }
    @Override
    public synchronized boolean isAlive() {
        if (super.isAlive()){
            return true;
        } else {
            if (!isDead){
                triggerDeathClock();
            }
            return !buffs(ClusteredSkeleton.DeathClock.class).isEmpty();
        }
    }
    protected void triggerDeathClock(){
        Buff.affect(this, ClusteredSkeleton.DeathClock.class).setShield(20);
        state = PASSIVE;
        spend( TICK );
        isDead = true;

        if (Dungeon.level.heroFOV[pos]) {
            for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                if (!Dungeon.level.solid[pos + PathFinder.NEIGHBOURS8[i]]) {
                    sprite.parent.add(new TargetedCell(pos + PathFinder.NEIGHBOURS8[i], 0xFF0000));
                }
            }
            GLog.w(Messages.get(this, "death_warning"));
        }
    }
    @Override
    public void die( Object cause ) {

        super.die( cause );

        if (cause == Chasm.class) return;

        // 聚合骷髅 100% 掉落 2～4 个残骨堆
        if(Dungeon.hero != null){
            Dungeon.level.drop(new BonePile().quantity(Random.Int(2, 4)), pos).sprite.drop();
        }

        CellEmitter.get(pos).burst(Speck.factory(Speck.BONE), 10);

        boolean heroKilled = false;
        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
            CellEmitter.get(pos + PathFinder.NEIGHBOURS8[i]).burst(Speck.factory(Speck.BONE), 5);
            Char ch = findChar( pos + PathFinder.NEIGHBOURS8[i] );
            if (ch != null && ch.isAlive()) {
                int damage = Math.round(Random.NormalIntRange(30, 55));
                if (ch.buff(PrisonArmor.myMask.class)!=null) damage=0;
                damage = Math.round( damage * AscensionChallenge.statModifier(this));
                damage = Math.max( 0,  damage - (ch.drRoll() + ch.drRoll()) );
                ch.damage( DamageInfo.of(damage, DamageType.PHYSICAL, this, this) );
                if (ch == Dungeon.hero && !ch.isAlive()) {
                    heroKilled = true;
                }
            }
            int spawnPos = pos + PathFinder.NEIGHBOURS8[i];
            if (Terrain.flags[Dungeon.level.map[spawnPos]] == Terrain.PASSABLE
                    && findChar(spawnPos) == null) {
                if (Random.Int(3) < 2) {
                    Necromancer.NecroSkeleton mySkeleton = new Necromancer.NecroSkeleton();
                    mySkeleton.pos = spawnPos;
                    GameScene.add(mySkeleton);
                    Dungeon.level.occupyCell(mySkeleton);
                }
            }
        }

        if (Dungeon.level.heroFOV[pos]) {
            Sample.INSTANCE.play( Assets.Sounds.BONES );
        }

        if (heroKilled) {
            Dungeon.fail( getClass() );
            GLog.n( Messages.get(this, "hart_hanson_kill") );
        }
    }
    public static class DeathClock extends ShieldBuff {

        {
            type = buffType.POSITIVE;
        }

        @Override
        public boolean act() {

            if (target.HP > 0){
                detach();
                return true;
            }

            absorbDamage( 10 );

            if (shielding() <= 0){
                target.die(null);
            }

            spend( TICK );

            return true;
        }
        public String desc () {
            return Messages.get(this, "desc", shielding());
        }

        @Override
        public String icon() {
            return BuffIndicator.TERROR;
        }

        {
            immunities.add(Terror.class);
        }
    }
}
