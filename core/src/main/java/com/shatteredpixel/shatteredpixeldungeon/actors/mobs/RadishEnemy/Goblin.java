package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;

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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.damage.OrdinaryAttackDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterImage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CloakofGreyFeather;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FogSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.TheGreatDead;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MakeshiftSlingshot;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Goblin extends Mob {
    {
        spriteClass = GoblinSprite.class;

        HP = HT = 20;
        defenseSkill = 4;

        EXP = 5;
        maxLvl = 10;

        loot = Generator.Category.MIS_T2;
        lootChance = 0.1f;
    }

    private static final String STONEREMAIN	= "StoneRemain";
    public Goblin(){
        super();
        StoneRemain = 2;
    }
    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( STONEREMAIN, StoneRemain );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        StoneRemain = bundle.getInt(STONEREMAIN);
    }

    private int StoneRemain;
    private boolean canZap = true;
    @Override
    protected boolean canAttack( Char enemy ) {
        return new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos && (canZap || Dungeon.level.adjacent(pos,enemy.pos));
    }
    protected boolean doAttack(Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )) {

            return super.doAttack( enemy );

        } else{
            StoneRemain --;
            StoneRemain = Math.max(StoneRemain, 0);
            canZap = StoneRemain > 0;
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                StoneRemain --;
                StoneRemain = Math.max(StoneRemain, 0);
                canZap = StoneRemain > 0;
                zap();
                return true;
            }
        }
    }

    private void zap() {
        spend(1f);
        Invisibility.dispel(this);
        if (hit(this, enemy, true)) {
            int dmg = Random.NormalIntRange(2, 4);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            dmg = defenseProc(enemy,dmg);
            dmg -= enemy.drRoll();
            enemy.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, this, new ThrowingStone()));

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Dungeon.fail(getClass());
                GLog.n(Messages.get(this, "rock_punk_kill"));
            }
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
            Item dropStone = new ThrowingStone();
            Item greatStone = new TheGreatDead();
//            if(Dungeon.hero != null){
//                if (Dungeon.hero.heroClass == HeroClasses.WARRIOR && Random.Int(0,10)>8)
//                    Dungeon.level.drop(greatStone,enemy.pos).sprite.drop();
//                else
//                    Dungeon.level.drop(dropStone,enemy.pos).sprite.drop();
//            }
//            else
//                Dungeon.level.drop(dropStone,enemy.pos).sprite.drop();
                Dungeon.level.drop(dropStone,enemy.pos).sprite.drop();
        }
    }


    public void onZapComplete() {
        zap();
        next();
    }

    public int damageRoll() {
        return Random.NormalIntRange( 2, 3 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 14;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 3);
    }

    @Override
    public void die( Object cause ) {
        // 简易投石索 10% 掉落
        if(Random.Float() < 0.1f){
            Dungeon.level.drop(new MakeshiftSlingshot(), pos).sprite.drop();
        }
        // Random drop
        if(Random.Float(0,10)<5) loot=Generator.Category.MIS_T3;
        super.die( cause );
    }

    @Override
    public String description() {
        String desc = super.description();
        desc += Messages.get(this, "stone_remain", StoneRemain );
        return desc;
    }
    @Override
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

            OrdinaryAttackDamage.DamageRoll damageRoll = OrdinaryAttackDamage.rollBaseDamage(this);
            OrdinaryAttackDamage.CriticalRoll criticalRoll = OrdinaryAttackDamage.rollCritical(this, enemy, damageRoll.damage);
            Preparation prep = damageRoll.preparation;
            DamageInfo attackDamage = OrdinaryAttackDamage.build(this, enemy, Math.round(criticalRoll.damage), criticalRoll.critical,
                    criticalRoll.multiplier, dmgMulti, dmgBonus);

            int effectiveDamage = OrdinaryAttackDamage.foldPostProcessing(this, enemy, attackDamage);

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

            if(criticalRoll.critical){
                enemy.sprite.showStatus(CharSprite.NEGATIVE,Messages.get(this,"crit"));
            }
            enemy.damage(attackDamage);

            if (buff(FireImbue.class) != null)  buff(FireImbue.class).proc(enemy);
            if (buff(FrostImbue.class) != null) buff(FrostImbue.class).proc(enemy);

            if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
                enemy.HP = 0;
                if (!enemy.isAlive()) {
                    enemy.die(this);
                } else {
                    //helps with triggering any on-damage effects that need to activate
                    enemy.damage(DamageInfo.of(-1, DamageType.TRUE, this, this));
                    DeathMark.processFearTheReaper(enemy);
                }
                enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
            }

            enemy.sprite.bloodBurstA( sprite.center(), effectiveDamage );
            enemy.sprite.flash();

            if (!enemy.isAlive() && visibleFight && Dungeon.hero != null) {
                if (enemy == Dungeon.hero) {

                    Dungeon.fail( getClass() );
                    GLog.n( Messages.capitalize(Messages.get(Goblin.class, "rankings_desc")) );

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
}
