package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GnollZealotSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.Collections;
import java.util.Set;


public class GnollZealot extends Mob {
    {
        spriteClass = GnollZealotSprite.class;

        HP = HT = 40;
        defenseSkill = 15;


        EXP = 8;
        maxLvl = 18;

        loot = new Gold();
        lootChance = 0.25f;
    }

    private boolean isFirstSeen = false;
    @Override
    protected boolean act() {
        if(Dungeon.hero != null && fieldOfView!= null){
            if(!isFirstSeen && fieldOfView[Dungeon.hero.pos]){
                isFirstSeen = true;
                GLog.n('\n'+ Messages.get(GnollZealot.class, "steal_healing",name()));
            }
            if((!Dungeon.hero.buffs(Healing.class).isEmpty() || !Dungeon.hero.buffs(Sungrass.Health.class).isEmpty()) && fieldOfView[Dungeon.hero.pos]){
                Set<Mob> mobs = Collections.synchronizedSet(Dungeon.level.mobs);
                for(Mob mob:mobs){
                    Buff.affect(mob,Healing.class).setHeal(8,1f,0);
                }
            }
        }
        return super.act();
    }

    public int damageRoll() {
        return Random.NormalIntRange( 5, 10 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 18;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 6);
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
    }
    @Override
    public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
        boolean isAttack = super.attack(enemy, dmgMulti, dmgBonus, accMulti);
        if(!enemy.isAlive() && enemy== Dungeon.hero){
            Dungeon.fail(getClass());
            GLog.n('\n'+ Messages.get(this, "kill"));
        }
        return isAttack;
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }

    protected boolean doAttack(Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )) {

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

    private void zap() {
        spend( 1f );

        Invisibility.dispel(this);
        if (hit( this, enemy, true )) {

            int dmg = Random.NormalIntRange( 6, 15 );
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            enemy.damage( dmg, new Shaman.EarthenBolt() );

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Badges.validateDeathFromEnemyMagic();
                Dungeon.fail( getClass() );
                GLog.n( Messages.get(this, "crazy_dance_kill") );
            }
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }
}
