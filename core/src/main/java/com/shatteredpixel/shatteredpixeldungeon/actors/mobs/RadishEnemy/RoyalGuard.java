package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.damage.OrdinaryAttackDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterImage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CloakofGreyFeather;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FogSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.RoyalGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class RoyalGuard extends Mob {
    {
        spriteClass = RoyalGuardSprite.class;

        HP = HT = 80;
        defenseSkill = 30;


        EXP = 10;
        maxLvl = 21;

        properties.add(Property.UNDEAD);

        loot = Generator.Category.WEP_T4;
        lootChance = 0.2f;
    }
    public Weapon equipment;
    private static final String ROYAL_GUARD_WEAPON	= "royal_guard_weapon";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        if(equipment != null)
            bundle.put( ROYAL_GUARD_WEAPON, equipment );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        equipment = (Weapon)bundle.get( ROYAL_GUARD_WEAPON );
        if(equipment == null)
            equipment = (Weapon) Generator.randomUsingDefaults(Random.oneOf(Generator.Category.WEP_T4, Generator.Category.WEP_T5));
    }

    public RoyalGuard(){
        super();
        equipment = (Weapon) Generator.randomUsingDefaults(Random.oneOf(Generator.Category.WEP_T4, Generator.Category.WEP_T5));
//        equipment = new FogSword();
    }

    @Override
    public String description() {
        String desc = super.description();
        desc += "\n\n";
        desc += Messages.get(this, "equipment", equipment.name() );
        return desc;
    }
    @Override
    public Item createLoot() {
        Dungeon.LimitedDrops.ROYALGUARD_WEP.count++;
        int Ug = Random.Int(0,100)<50?0:(Random.Int(0,50)<40?1:2);
        if(equipment != null && equipment instanceof MeleeWeapon)
            loot = equipment.upgrade(Ug);
        return super.createLoot();
    }
    @Override
    public float lootChance() {
        return (float) (super.lootChance() * ((float) 1 /(Math.pow(2, Dungeon.LimitedDrops.ROYALGUARD_WEP.count))));
    }

    @Override
    public int damageRoll() {
        if(equipment != null)
            return equipment.damageRoll(this);
        else
            return Random.Int(8,8);
    }

    @Override
    public int attackSkill( Char target ) {
        int accuracy = 30;
        if(equipment != null && equipment instanceof MeleeWeapon){
            accuracy *= equipment.accuracyFactor( this, target );
        }
        return accuracy;
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * equipment.delayFactor(this);
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        if(equipment != null && equipment instanceof MeleeWeapon)
            return super.canAttack(enemy) || equipment.canReach(this, enemy.pos);
        else
            return super.canAttack(enemy);
    }
    @Override
    public int drRoll() {
        if (wieldsCircleSword()) return 0;
        return Random.NormalIntRange(1, 8) + equipment.defenseFactor(this);
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
                    GLog.n( Messages.capitalize(Messages.get(DM175.class, "kill")) );

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

            if (equipment instanceof FogSword) {
                Buff.affect(this, Invisibility.class,1f);
            }

            if (visibleFight) {
                Sample.INSTANCE.play(Assets.Sounds.MISS);
            }

            return false;

        }
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        damage = equipment.proc( this, enemy, damage );
        if (!enemy.isAlive() && enemy == Dungeon.hero){
            Dungeon.fail(getClass());
            GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
        }
        return damage;
    }


}
