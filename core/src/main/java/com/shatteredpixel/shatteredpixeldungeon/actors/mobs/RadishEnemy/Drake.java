package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import static com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave.throwChar;

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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterImage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CloakofGreyFeather;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FogSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DrakeSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Drake extends Mob {
    {
        spriteClass = DrakeSprite.class;

        HP = HT = 80;
        defenseSkill = 10;

        baseSpeed = 0.8f;

        EXP = 9;
        maxLvl = 19;

        alignment = Alignment.NEUTRAL;
        state = PASSIVE;

        loot = new MysteryMeat();
        lootChance = 0.25f;
    }

    private boolean gotClose = false;
    private int ultimatum = 2;

    private boolean isNotice = false;
    private boolean isStopHiding = false;


    public int damageRoll() {
        return Random.NormalIntRange( 8, 15 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(2,4);
    }

    @Override
    public void die( Object cause ) {
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
                    GLog.n( Messages.capitalize(Messages.get(Drake.class, "kill")) );

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


    @Override
    public boolean interact(Char c) {
        if (alignment != Alignment.NEUTRAL || c != Dungeon.hero) {
            return super.interact(c);
        }
        stopHiding();
        if (Dungeon.hero.invisible <= 0
                && Dungeon.hero.buff(Swiftthistle.TimeBubble.class) == null
                && Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) == null){
            return doAttack(Dungeon.hero);
        } else {
            sprite.idle();
            alignment = Alignment.ENEMY;
            Dungeon.hero.spendAndNext(1f);
            return true;
        }
    }

    @Override
    protected boolean act() {
        if (alignment == Alignment.NEUTRAL && state != PASSIVE){
            alignment = Alignment.ENEMY;
        }
        if(Dungeon.hero != null){
            if (alignment == Alignment.NEUTRAL && Dungeon.level.distance(pos,Dungeon.hero.pos)<=2){
//                if(!isNotice){
//                    GLog.n('\n'+ Messages.get(this, "get_close"));
//                    Camera.main.shake(5, 1f);
//                    isNotice = true;
//                }
//                Dungeon.hero.interrupt();
//                Buff.append(this, DM300.FallingRockBuff.class, 0f).setRockPositions(new ArrayList<>(Dungeon.hero.pos));
                if(Dungeon.level.adjacent(pos,Dungeon.hero.pos)){
//                    stopHiding();
                    GLog.n('\n'+ Messages.get(this, "revealing"));
                    gotClose = true;
                }
            }
            if(gotClose){
                ultimatum --;
            }
            if(ultimatum < 0 && !isStopHiding){
                stopHiding();
                isStopHiding = true;
            }
        }
        return super.act();
    }
    @Override
    public CharSprite sprite() {
        DrakeSprite sprite = (DrakeSprite) super.sprite();
        if (alignment == Alignment.NEUTRAL) sprite.hideDrake();
        return sprite;
    }
    @Override
    public void onAttackComplete() {
        super.onAttackComplete();
        if (alignment == Alignment.NEUTRAL){
            alignment = Alignment.ENEMY;
        }
    }

    @Override
    public void damage(int dmg, Object src) {
        if (state == PASSIVE){
            alignment = Alignment.ENEMY;
            stopHiding();
        }
        super.damage(dmg, src);
    }
//    @Override
//    public boolean interact(Char c) {
//        if (alignment != Alignment.NEUTRAL || c != Dungeon.hero){
//            return super.interact(c);
//        }
//        stopHiding();
//
////        Dungeon.hero.busy();
//        if (Dungeon.hero.invisible <= 0
//                && Dungeon.hero.buff(Swiftthistle.TimeBubble.class) == null
//                && Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) == null){
////            return doAttack(Dungeon.hero);
//            sprite.idle();
//            alignment = Alignment.ENEMY;
//            return true;
//        } else {
//            sprite.idle();
//            alignment = Alignment.ENEMY;
//            return true;
//        }
//    }

    @Override
    public boolean add(Buff buff) {
        boolean buffAdd = super.add(buff);
        if (buff.type == Buff.buffType.NEGATIVE && alignment == Alignment.NEUTRAL){
            alignment = Alignment.ENEMY;
            stopHiding();
            if (sprite != null) sprite.idle();
        }
        return buffAdd;
    }
    public void stopHiding(){
        state = HUNTING;
        if (sprite != null) sprite.idle();
        if (Actor.chars().contains(this) && Dungeon.level.heroFOV[pos]) {
//            enemy = Dungeon.hero;
//            target = pos;
//            enemySeen = true;
            GLog.w("\n"+Messages.get(this, "reveal") );
//            CellEmitter.get(pos).burst(Speck.factory(Speck.ROCK), 10);
            Mob drake = this;
            sprite.jump(pos, pos, new Callback() {
                @Override
                public void call() {
//                    move(target);
//                    Dungeon.level.occupyCell(drake);
                    WandOfBlastWave.BlastWave.blast(pos);
                    Sample.INSTANCE.play(Assets.Sounds.BLAST);
//                    Camera.main.shake(100, 0.5f);
                    Dungeon.observe();
//                    GameScene.updateFog();


                    //throws other chars around the center.
                    for (int i  : PathFinder.NEIGNBOURS24){
                        Char ch = Actor.findChar(pos + i);

                        CellEmitter.get(pos+i).burst(Speck.factory(Speck.ROCK), 10);
                        WandOfBlastWave.BlastWave.blast(pos+i);
                        WandOfBlastWave.BlastWave.blast(pos+2*i);
                        WandOfBlastWave.BlastWave.blast(pos+3*i);
                        if (ch != null && ch != drake){
                            if (ch.alignment != Char.Alignment.ALLY) ch.damage(damageRoll(), this);

                            if (ch.pos == pos + i) {
                                Ballistica trajectory = new Ballistica(ch.pos, ch.pos + i, Ballistica.MAGIC_BOLT);
                                int strength = 2;
                                throwChar(ch, trajectory, strength, false, true, getClass());
                                ch.damage(Random.Int(10,25),this);
                                GLog.w("\n"+Messages.get(Drake.class, "shock",ch.name()) );
                                if(Dungeon.hero != null){
                                    if(!Dungeon.hero.isAlive()){
                                        Dungeon.fail(getClass());
                                        GLog.n('\n'+ Messages.get(Drake.class, "rock_punk_kill"));
                                    }
                                }
                            }

                        }
                    }
                    spend(2f*TICK);
                    Dungeon.hero.spendAndNext(1f);
                }
            });
        }
    }
    @Override
    public boolean reset() {
        if (state != PASSIVE) state = WANDERING;
        return true;
    }
}
