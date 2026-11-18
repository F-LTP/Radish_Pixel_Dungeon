package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.TheGreatDead;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MakeshiftSlingshot;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
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

    @Override
    protected boolean canAttack( Char enemy ) {
        // 相邻、挑战装备的武器距离、精英（索敌 / 巨型）的额外攻击距离都由父类判定，
        // 不调父类的话石头丢完后的索敌精英只能贴身打
        return super.canAttack( enemy ) || canThrowAt( enemy );
    }

    // 能否向目标投掷：直接由存档里的剩余数量推出，读档后不会凭空多丢一次
    protected boolean canThrowAt( Char enemy ) {
        return StoneRemain > 0
                && new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
    }

    @Override
    protected boolean doAttack(Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos ) || !canThrowAt( enemy )) {

            // 相邻，或没法投掷但仍在攻击距离内（精英的隔空攻击）：走父类近战流程
            return super.doAttack( enemy );

        } else{
            StoneRemain --;
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                // 回合由 GoblinSprite.zap 的投掷物落地回调 onZapComplete() 里的 next() 结束
                sprite.zap( enemy.pos );
                return false;
            } else {
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
            // 防御结算（护甲刻印等）属于被砸中的一方，不是哥布林自己
            dmg = enemy.defenseProc(this, dmg);
            // 与 Char.attack 一致：防御结算可能已让目标死亡，不再继续结算
            if (!enemy.isAlive()) {
                return;
            }
            dmg = Math.max(0, dmg - enemy.drRoll());
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
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(Goblin.class, "rankings_desc"));
    }
}
