package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.ShieldMageSprite;
import com.watabou.utils.Random;

import java.util.Collections;
import java.util.Set;

public class ShieldMage extends Mob {
    {
        spriteClass = ShieldMageSprite.class;

        HP = HT = 70;
        defenseSkill = 22;


        EXP = 9;
        maxLvl = 21;

        properties.add(Property.UNDEAD);

        loot = new PotionOfShielding();
        lootChance = 0.15f;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc(enemy, damage);
        if(Random.Int(0,4)<1){
            Buff.affect(enemy, Slow.class,2f);
        }
        return damage;
    }


        public int damageRoll() {
        return Random.NormalIntRange( 10, 16 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 30;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 6);
    }

    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(ShieldMage.class, "kill"));
    }
    protected boolean canAttack( Char enemy ) {
        // 相邻、挑战装备的武器距离、精英（索敌 / 巨型）的额外攻击距离都由父类判定
        return super.canAttack( enemy )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }
    protected boolean doAttack(Char enemy ) {

        // 弹道被挡住还能进到这里，说明是靠父类的攻击距离够到的，和相邻一样走普通攻击
        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

            return super.doAttack( enemy );

        } else {
            int zapPos = pos;
            if(Dungeon.hero != null && fieldOfView != null){
                if(fieldOfView[Dungeon.hero.pos] && !Dungeon.level.adjacent(pos,Dungeon.hero.pos)){
                    Set<Mob> mobs = Collections.synchronizedSet(Dungeon.level.mobs);
                    Mob weakestMob = this;
                    for(Mob mob:mobs){
                        // 只给友方（同为敌人阵营）套盾，避免治疗玩家或其他敌对生物
                        if (mob.alignment != alignment) continue;
                        if(mob.HP < weakestMob.HP && fieldOfView[mob.pos]){
                            weakestMob = mob;
                            zapPos = mob.pos;
                        }
                    }
                }
            }
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( zapPos );
                return false;
            } else {
                zap(zapPos);
                return true;
            }
        }
    }
    public void onZapComplete(int cell) {
        zap(cell);
        next();
    }

    private void zap(int cell) {
        spend( 1f );

        Char mob = Actor.findChar(cell);

        // 只给友方套盾，绝不治疗敌对目标
        if (mob != null && mob.alignment == alignment)
            Buff.affect(mob, Barrier.class).setShield(15);

        Invisibility.dispel(this);
    }
    @Override
    protected boolean act() {
        boolean isAct = super.act();
        return isAct;
    }
}
