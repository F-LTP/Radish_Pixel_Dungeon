package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ToxicImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ZikkSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class BigSnake_Zikk extends Mob {

    private boolean LastHP = false;

    {
        spriteClass = ZikkSprite.class;
        HT = HP = 80;
        defenseSkill = 10;
        maxLvl = 5;

        properties.add(Property.BOSS);
        properties.add(Property.DEMONIC);
        properties.add(Property.ACIDIC);
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        Buff.affect(enemy, Poison.class).extend(2);
        return super.attackProc(enemy, damage);
    }

    @Override
    public boolean act() {
        boolean bleeding = (HP*2 <= HT);
        if(bleeding && !LastHP){
            Buff.affect(this, ToxicImbue.class).set(ToxicImbue.DURATION);
            sprite.emitter().burst(PoisonParticle.SPLASH, 10);
            GLog.n(Messages.get(this,"poison"));
            LastHP = true;
        }

        return super.act();
    }

    @Override
    public void die( Object cause ) {

        super.die( cause );

        Dungeon.level.unseal();

        GameScene.bossSlain();
        Dungeon.level.drop( new SkeletonKey( Dungeon.depth ), pos ).sprite.drop();
        Dungeon.level.drop( new PotionOfToxicGas(), pos ).sprite.drop();

        if(Random.Float()<0.25f){
            Dungeon.level.drop( new ElixirOfToxicEssence(), pos ).sprite.drop();
        }

        Statistics.bossScores[0] += 1000;

        yell( Messages.get(this, "defeated") );
    }

    @Override
    public void notice() {
        super.notice();
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            Dungeon.level.seal();
            yell(Messages.get(this, "notice"));
            for (Char ch : Actor.chars()){
                if (ch instanceof DriedRose.GhostHero){
                    ((DriedRose.GhostHero) ch).sayBoss();
                }
            }
        }
    }

    public static class PoisonBolt{}

    private void zap() {
        spend( 1f );

        if(enemy == null){
            return;
        }

        if (hit( this, enemy, true )) {
            int dmg = 0;
            enemy.damage(DamageInfo.of(dmg, DamageType.POISON, this, new PoisonBolt()));
            Buff.affect(enemy, Poison.class).extend(4);
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }

        if (enemy == Dungeon.hero && !enemy.isAlive()) {
            Dungeon.fail( getClass() );
            GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
        }
    }

    public void onZapComplete() {
        zap();
        next();
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (enemy != null) {
            // 如果敌人中毒，完全不能进行任何攻击
            if (enemy.buff(Poison.class) != null) {
                return false;
            }
            // 大蛇与敌人距离为3格但敌人未中毒时，进行毒液攻击
            if (Dungeon.level.distance(pos, enemy.pos) == 3) {
                return true;
            }
        }
        // 其他情况下进行近战攻击
        return Dungeon.level.adjacent(pos, enemy.pos);
    }

    @Override
    public void damage(DamageInfo info) {
        if (!BossHealthBar.isAssigned()){
            BossHealthBar.assignBoss( this );
            Dungeon.level.seal();
        }
        super.damage(info);
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(info.getSource().getClass()) && !isInvulnerable(info.getSource().getClass())){
            if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(info.getDamage());
            else                                                    lock.addTime(info.getDamage()*1.5f);
        }
    }

    @Override
    protected boolean getCloser(int target) {
        if (state == HUNTING) {
            if (enemy != null && enemy.buff(Poison.class) != null) {
                return getFurther(target);
            } else if (enemy != null && enemy.buff(Poison.class) == null) {
                return super.getCloser(target);
            }
            return super.getCloser(target);
        } else {
            return super.getCloser(target);
        }
    }



    @Override
    protected boolean doAttack(Char enemy) {
        if (enemy != null && Dungeon.level.distance(pos, enemy.pos) == 3 && enemy.buff(Poison.class) != null) {
            return false;
        } else {
            return super.doAttack(enemy);
        }
    }

    @Override
    public int damageRoll() {
        return Char.combatRoll( 2, 6 );
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Char.combatRoll(1, 2);
    }

    @Override
    public int attackSkill( Char target ) {
        return 10;
    }

    private final String LASTHP = "lasthp";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put(LASTHP , LastHP );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        if (state != SLEEPING) BossHealthBar.assignBoss(this);
        if ((HP*2 <= HT)) BossHealthBar.bleed(true);
        LastHP = bundle.getBoolean( LASTHP );
    }

}
