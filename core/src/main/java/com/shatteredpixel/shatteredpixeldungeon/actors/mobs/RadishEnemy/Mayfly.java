package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.MayflySprite;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Mayfly extends Mob {
    {
        spriteClass = MayflySprite.class;

        HP = HT = 12;
        defenseSkill = 4;

        EXP = 2;
        maxLvl = 8;

        loot = Generator.Category.SEED;
        lootChance = 0.5f;
    }
    private ArrayList<Emitter> HealingPos;
    private boolean isAlone = true;


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
    private void zap() {
        spend( 1f );
        Invisibility.dispel(this);
        if (hit( this, enemy, true )) {
//            enemy.damage( damageRoll(), new MagicMissile());
            attack(enemy);
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }
    public void onZapComplete() {
        zap();
        next();
    }

    public int damageRoll() {
        return Random.NormalIntRange( 2, 4 );
    }

    @Override
    protected boolean act() {
        boolean isAct = super.act();
        if(this.HealingPos != null){
            for (Emitter e : HealingPos){
                e.on = false;
            }
        }
        // deprecated on 2024/07/16
//        for(Mob mob:Dungeon.level.mobs){
//            if(isInRange(mob.pos) && mob != this){
//                isAlone = false;
//            }
//            return isAct;
//        }
//        isAlone = true;
        if(isAlone && HP < HT){
            int Heal = damageRoll();
            this.HP += Heal;
            this.HP = Math.min(HP, HT);
            this.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
            this.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", Heal);
        }
        return isAct;
    }
    @Override
    public int attackSkill( Char target ) {
        return 8;
    }


    @Override
    protected boolean getCloser( int target ) {
        if (state == HUNTING && Dungeon.level.distance(pos,enemy.pos)<=2) {
            return enemySeen && getFurther( target );
        } else {
            return super.getCloser( target );
        }
    }

    @Override
    public void aggro(Char ch) {
        if (ch == null || fieldOfView == null || fieldOfView[ch.pos]) {
            super.aggro(ch);
        }
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        for(Mob mob:Dungeon.level.mobs){
            if(isInRange(mob.pos) && mob != this){
                isAlone = false;
                break;
            }
            isAlone = true;
        }
        Ballistica attack = new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT);
        return !isAlone && isInRange(enemy.pos) && attack.collisionPos == enemy.pos && !Dungeon.level.adjacent(pos,enemy.pos);
    }

    public int attackProc( Char enemy, int damage ) {
        if(this.HealingPos != null){
            for (Emitter e : HealingPos){
                e.on = false;
            }
        }
        HealingPos = new ArrayList<Emitter>();
        for (int i = 0; i < Dungeon.level.length(); i++){
            if (!Dungeon.level.solid[i] && Dungeon.level.distance(pos,i) <= 3) {
                Emitter e = CellEmitter.get(i);
//                e.pour(LeafParticle.LEVEL_SPECIFIC, 1f);
                HealingPos.add(e);
            }
        }
        for(Mob mob:Dungeon.level.mobs){
            if(isInRange(mob.pos) && mob != this){
                int healthHalo = damageRoll();
                mob.HP += healthHalo;
                mob.HP = Math.min(mob.HP, mob.HT);
                mob.sprite.emitter().burst(Speck.factory(Speck.HEALING), healthHalo);
                mob.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", healthHalo);
                break;
            }
        }
        return damage;
    }
    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 2);
    }

    private boolean isInRange(int pos){
        return Dungeon.level.distance(this.pos,pos)<=3;
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
        if(this.HealingPos != null){
            for (Emitter e : HealingPos){
                e.on = false;
            }
        }
    }
}
