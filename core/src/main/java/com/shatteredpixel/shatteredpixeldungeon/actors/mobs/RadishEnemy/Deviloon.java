package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterImage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CloakofGreyFeather;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FogSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DeviloonSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class Deviloon extends Mob {
    {
        spriteClass = DeviloonSprite.class;

        HP = HT = 100;
        defenseSkill = 28;

        viewDistance = Light.DISTANCE;

        EXP = 12;
        maxLvl = 26;

        HUNTING = new Deviloon.Hunting();

        properties.add(Property.DEMONIC);
        properties.add(Property.HEADLESS);

        flying = true;

        loot = new ArcaneResin().quantity(Random.Int(1,2));
        lootChance = 0.20f;
    }
    {
        resistances.add( WandOfFireblast.class );
    }

    HashSet<BlastRune> blastRunes = new HashSet<>();

    private Ballistica blastRune;
    private int blastTarget = -1;
    private int blastCooldown;
    public boolean blastCharged;

    @Override
    protected boolean canAttack( Char enemy ) {

        if (blastCooldown == 0) {
            Ballistica aim = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE);

            if (enemy.invisible == 0 && !isCharmedBy(enemy) && fieldOfView[enemy.pos] && aim.subPath(1, aim.dist).contains(enemy.pos)){
                blastRune = aim;
                blastTarget = aim.collisionPos;
                return true;
            } else
                //if the beam is charged, it has to attack, will aim at previous location of target.
                return blastCharged;
        } else
            return super.canAttack(enemy);
    }


    @Override
    protected boolean act() {

        // yep the complexity is O(n^2) , i'll optimize it one day.
        // Date : 2024-08-10
        for(Object i:blastRunes.toArray()){
            int item_cnt = 0;
            for (Heap heap : Dungeon.level.heaps.valueList()) {
                if(heap.pos==((BlastRune)i).drop_pos){
                    item_cnt = heap.size();
                }
            }
            if((Actor.findChar(((BlastRune)i).drop_pos)!=null && !(Actor.findChar(((BlastRune)i).drop_pos)instanceof Deviloon )) || item_cnt>=2){
                Actor.addDelayed(((BlastRune)i).fuse = ((BlastRune)i).fuse.ignite((BlastRune) i), 0);
                blastRunes.remove(i);
            }
        }

        if (blastCharged && state != HUNTING){
            blastCharged = false;
            sprite.idle();
        }
        if (blastRune == null && blastTarget != -1) {
            blastRune = new Ballistica(pos, blastTarget, Ballistica.PROJECTILE);
            sprite.turnTo(pos, blastTarget);
        }
        if (blastCooldown > 0)
            blastCooldown--;
        return super.act();
    }

    public void onZapComplete() {
        callOfRune();
        next();
    }

    @Override
    protected boolean doAttack( Char enemy ) {

        if (blastCooldown > 0) {
            return super.doAttack(enemy);
        } else if (!blastCharged){
            ((DeviloonSprite)sprite).charge( enemy.pos );
            spend( attackDelay() );
            blastCharged = true;
            return true;
        } else {

            spend( attackDelay() );

            blastRune = new Ballistica(pos, blastTarget, Ballistica.PROJECTILE);
            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[blastRune.collisionPos] ) {
                sprite.zap( blastRune.collisionPos );
                return false;
            } else {
                sprite.idle();
                callOfRune();
                return true;
            }
        }

    }

    public void callOfRune(){
        if (!blastCharged || blastCooldown > 0 || blastRune == null)
            return;

        blastCharged = false;
        blastCooldown = 10;

        boolean terrainAffected = false;

        Invisibility.dispel(this);
        for (int pos : blastRune.subPath(1, blastRune.dist)) {

            if (Dungeon.level.flamable[pos]) {

                Dungeon.level.destroy( pos );
                GameScene.updateMap( pos );
                terrainAffected = true;

            }
            Plant plant = Dungeon.level.plants.get( pos );
            if (plant != null){
                plant.wither();
            }
            Char ch = Actor.findChar( pos );
            if (ch == null) {
                continue;
            }

            if (true) {
                HashSet<Integer> blast_pos_set = new HashSet<>();
                while(blast_pos_set.size()<3){
                    blast_pos_set.add(PathFinder.NEIGHBOURS8[Random.Int(0,8)]);
                }
                for(int i:blast_pos_set){
                    int c =  ch.pos + i;
                    if (c >= 0 && c < Dungeon.level.length() && !Dungeon.level.solid[c]) {
                        BlastRune blastRune_tmp = new BlastRune().set_pos(c);
                        blastRunes.add(blastRune_tmp);
                        Dungeon.level.drop(blastRune_tmp,c);
                    }
                }



                if (Dungeon.level.heroFOV[pos]) {
                    ch.sprite.flash();
                    CellEmitter.center( pos ).burst( MagicMissile.ForceParticle.FACTORY, Random.IntRange( 1, 2 ) );
                }

            } else {
                ch.sprite.showStatus( CharSprite.NEUTRAL,  ch.defenseVerb() );
            }
        }

        if (terrainAffected) {
            Dungeon.observe();
        }

        blastRune = null;
        blastTarget = -1;
    }

    private static final String BLAST_TARGET     = "BlastTarget";
    private static final String BLAST_COOLDOWN   = "BlastCooldown";
    private static final String BLAST_CHARGED    = "BlastCharged";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( BLAST_TARGET, blastTarget);
        bundle.put( BLAST_COOLDOWN, blastCooldown );
        bundle.put( BLAST_CHARGED, blastCharged );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(BLAST_TARGET))
            blastTarget = bundle.getInt(BLAST_TARGET);
        blastCooldown = bundle.getInt(BLAST_COOLDOWN);
        blastCharged = bundle.getBoolean(BLAST_CHARGED);
    }


    private class Hunting extends Mob.Hunting{
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            //even if enemy isn't seen, attack them if the beam is charged
            if (blastCharged && enemy != null && canAttack(enemy)) {
                enemySeen = enemyInFOV;
                return doAttack(enemy);
            }
            return super.act(enemyInFOV, justAlerted);
        }
    }


    public int damageRoll() {
        return Random.NormalIntRange( 20, 30 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 10);
    }

    @Override
    public void die( Object cause ) {
        for(BlastRune i:blastRunes){
            Actor.addDelayed(((BlastRune)i).fuse = ((BlastRune)i).fuse.ignite((BlastRune) i), 0);
        }
        super.die( cause );
    }
    public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {

        if (enemy == null) return false;

        boolean visibleFight = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];

        if (enemy.isInvulnerable(getClass())) {

            if (visibleFight) {
                enemy.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "invulnerable") );

                Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
            }

            return false;

        } else if (hit( this, enemy, accMulti, false )) {
            if (enemy.buff(AfterImage.Blur.class)!=null){
                enemy.buff(AfterImage.Blur.class).gainDodge();
            }
            int dr = Math.round(enemy.drRoll() * AscensionChallenge.statModifier(enemy));

            Barkskin bark = enemy.buff(Barkskin.class);
            if (bark != null)   dr += Random.NormalIntRange( 0 , bark.level() );

            //we use a float here briefly so that we don't have to constantly round while
            // potentially applying various multiplier effects
            float dmg;
            Preparation prep = buff(Preparation.class);
            if (prep != null){
                dmg = prep.damageRoll(this);
            } else {
                dmg = damageRoll();
            }
            boolean crit=false;
            boolean surprise =enemy instanceof Mob && ((Mob) enemy).surprisedBy(this);
            float current_crit=critSkill(),current_critdamage=critDamage();

            current_critdamage=Math.min(current_critdamage,critDamageCap);
            if (this.buff(Scythe.scytheSac.class)!=null){
                current_crit+=10f;
                current_critdamage+=0.1f;
            }

            if (this.buff(RingOfTenacity.Tenacity.class)!=null) {current_crit=0;}
            if (Random.Float()*100<current_crit || crit) {
                dmg*=current_critdamage;
                crit = true;
            }

            dmg = Math.round(dmg*dmgMulti);

            Berserk berserk = buff(Berserk.class);
            if (berserk != null) dmg = berserk.damageFactor(dmg);

            if (buff( Fury.class ) != null) {
                dmg *= 1.5f;
            }
            if (buff(RingOfTenacity.Tenacity.class)!=null){
                dmg*=RingOfTenacity.attackMultiplier(this);
            }
            for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
                dmg *= buff.meleeDamageFactor();
            }
            for (ChampionHero buff : buffs(ChampionHero.class)){
                dmg *= buff.meleeDamageFactor();
            }
            dmg *= AscensionChallenge.statModifier(this);

            //flat damage bonus is applied after positive multipliers, but before negative ones
            dmg += dmgBonus;

            //friendly endure
            Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
            if (endure != null) dmg = endure.damageFactor(dmg);

            //enemy endure
            endure = enemy.buff(Endure.EndureTracker.class);
            if (endure != null){
                dmg = endure.adjustDamageTaken(dmg);
            }

            if (enemy.buff(ScrollOfChallenge.ChallengeArena.class) != null){
                dmg *= 0.67f;
            }

            if ( buff(Weakness.class) != null ){
                dmg *= 0.67f;
            }

            int effectiveDamage = enemy.defenseProc( this, Math.round(dmg) );

            // created by DoggingDog on 20240718
            // for Torturer using
            effectiveDamage = Math.max( effectiveDamage - dr, 0 );

            if (enemy.buff(Viscosity.ViscosityTracker.class) != null){
                effectiveDamage = enemy.buff(Viscosity.ViscosityTracker.class).deferDamage(effectiveDamage);
                enemy.buff(Viscosity.ViscosityTracker.class).detach();
            }

            //vulnerable specifically applies after armor reductions
            if ( enemy.buff( Vulnerable.class ) != null){
                effectiveDamage *= 1.33f;
            }

            effectiveDamage = attackProc( enemy, effectiveDamage );

            if (visibleFight) {
                if (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
                    hitSound(Random.Float(0.87f, 1.15f));
                }
            }

            // If the enemy is already dead, interrupt the attack.
            // This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
            if (!enemy.isAlive()){
                return true;
            }

            if(crit){
                enemy.sprite.showStatus(CharSprite.NEGATIVE,Messages.get(this,"crit"));
            }
            enemy.damage( effectiveDamage, this );

            if (buff(FireImbue.class) != null)  buff(FireImbue.class).proc(enemy);
            if (buff(FrostImbue.class) != null) buff(FrostImbue.class).proc(enemy);

            if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
                enemy.HP = 0;
                if (!enemy.isAlive()) {
                    enemy.die(this);
                } else {
                    //helps with triggering any on-damage effects that need to activate
                    enemy.damage(-1, this);
                    DeathMark.processFearTheReaper(enemy);
                }
                enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
            }

            enemy.sprite.bloodBurstA( sprite.center(), effectiveDamage );
            enemy.sprite.flash();

            if (!enemy.isAlive() && visibleFight && Dungeon.hero != null) {
                if (enemy == Dungeon.hero) {

                    Dungeon.fail( getClass() );
                    GLog.n( Messages.capitalize(Messages.get(Deviloon.class, "kill")) );

                }
            }
            return true;
        } else {
            if (enemy.buff(CloakofGreyFeather.hexDodge.class)!=null){
                for (Char ch : Actor.chars()) {
                    if (ch.alignment != enemy.alignment && enemy.fieldOfView[ch.pos] && ch.alignment!= Alignment.NEUTRAL){
                        Buff.affect(ch, Hex.class,2f+0.75f*enemy.buff(CloakofGreyFeather.hexDodge.class).buffedLvl());
                    }
                }
            }
            enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.defenseVerb() );

            if(Dungeon.hero != null){
                if (Dungeon.hero.belongings.weapon() instanceof FogSword) {
                    Buff.affect(Dungeon.hero, Invisibility.class,1f);
                }
            }

            if (visibleFight) {
                Sample.INSTANCE.play(Assets.Sounds.MISS);
            }

            return false;

        }
    }


    public static class DeviloonParticle extends PixelParticle {

        public static final Emitter.Factory ATTRACTING = new Emitter.Factory() {
            @Override
            public void emit( Emitter emitter, int index, float x, float y ) {
                ((DeviloonParticle)emitter.recycle( DeviloonParticle.class )).resetAttract( x, y );
            }
            @Override
            public boolean lightMode() {
                return true;
            }
        };

        public DeviloonParticle() {
            super();

            color( 0xFFA500 );
            lifespan = 0.5f;

            speed.set( Random.Float( -10, +10 ), Random.Float( -10, +10 ) );
        }

        public void reset( float x, float y ) {
            revive();

            this.x = x;
            this.y = y;

            left = lifespan;
        }

        public void resetAttract( float x, float y) {
            revive();

            //size = 8;
            left = lifespan;

            speed.polar( Random.Float( PointF.PI2 ), Random.Float( 16, 32 ) );
            this.x = x - speed.x * lifespan;
            this.y = y - speed.y * lifespan;
        }

        @Override
        public void update() {
            super.update();
            // alpha: 1 -> 0; size: 1 -> 4
            size( 4 - (am = left / lifespan) * 3 );
        }
    }

    public class BlastRune extends Item{
        public Fuse fuse;

        public int drop_pos = -1;

        {
            dropsDownHeap = true;
            unique = true;
            fuse = new Fuse();
            image = ItemSpriteSheet.DARTS+13;
        }

        public BlastRune set_pos(int pos){
            drop_pos = pos;
            return this;
        }

        public class Fuse extends Actor{

            {
                actPriority = BLOB_PRIO+1; //after hero, before other actors
            }

            private BlastRune blastRune;
            private int fuse_pos = -1;

            public Fuse ignite(BlastRune blastRune){
                this.blastRune = blastRune;
                return this;
            }

            public Fuse set_pos(int pos){
                this.fuse_pos = pos;
                return this;
            }

            @Override
            protected boolean act() {

                if (blastRune.fuse != this){
                    Actor.remove( this );
                    return true;
                }

                //look for our bomb, remove it from its heap, and blow it up.
                for (Heap heap : Dungeon.level.heaps.valueList()) {
                    if (heap.items.contains(blastRune)) {

                        heap.remove(blastRune);
                        blastRune.explode(heap.pos);

                        diactivate();
                        Actor.remove(this);
                        return true;
                    }
                }

                //can't find our bomb, something must have removed it, do nothing.
                blastRune.fuse = null;
                Actor.remove( this );
                return true;
            }
        }


        @Override
        public ItemSprite.Glowing glowing() {
            return fuse != null ? new ItemSprite.Glowing( 0xFF0000, 0.6f) : null;
        }

        public boolean explodesDestructively(){
            return true;
        }

        private static final String FUSE = "fuse";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( FUSE, fuse );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            if (bundle.contains( FUSE ))
                Actor.add( fuse = ((Fuse)bundle.get(FUSE)).ignite(this) );
        }

        public void explode(int cell){
            //We're blowing up, so no need for a fuse anymore.
            this.fuse = null;

            Sample.INSTANCE.play( Assets.Sounds.BLAST );

            if (explodesDestructively()) {

                ArrayList<Char> affected = new ArrayList<>();

                if (Dungeon.level.heroFOV[cell]) {
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
                }

                boolean terrainAffected = false;
                for (int n : PathFinder.NEIGHBOURS9) {
                    int c = cell + n;
                    if (c >= 0 && c < Dungeon.level.length()) {
                        if (Dungeon.level.heroFOV[c]) {
                            CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                        }

                        if (Dungeon.level.flamable[c]) {
                            Dungeon.level.destroy(c);
                            GameScene.updateMap(c);
                            terrainAffected = true;
                        }

                        //destroys items / triggers bombs caught in the blast.
                        Heap heap = Dungeon.level.heaps.get(c);
                        if (heap != null){
                            heap.explode();
                            for(Object i:heap.items){
                                if(i instanceof BlastRune){
                                    Actor.addDelayed(((BlastRune)i).fuse = ((BlastRune)i).fuse.ignite((BlastRune) i), 0);
                                    blastRunes.remove(i);
                                }
                            }
                        }

                        Plant plant = Dungeon.level.plants.get( c );
                        if (plant != null){
                            plant.wither();
                        }
                        Char ch = Actor.findChar(c);
                        if (ch != null) {
                            affected.add(ch);
                        }
                    }
                }

                for (Char ch : affected){

                    //if they have already been killed by another bomb
                    if(!ch.isAlive()){
                        continue;
                    }

                    int dmg = Random.NormalIntRange(20,40);

                    if (dmg > 0) {
                        // Deminion boost
                        if(ch.buff(Deminion.Sigil.class)!=null){
                            dmg *= 1.3f;
                        }
                        ch.damage(dmg, this);
                    }
                    if (ch == Dungeon.hero && !ch.isAlive()) {
                        GLog.n(Messages.get(this, "ondeath"));
                        Dungeon.fail(Bomb.class);
                    }

                }

                if (terrainAffected) {
                    Dungeon.observe();
                }
            }
        }
    }


}
