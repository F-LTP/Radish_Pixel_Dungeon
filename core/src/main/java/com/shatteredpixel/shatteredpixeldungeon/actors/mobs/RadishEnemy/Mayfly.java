package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
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
        return isAct;
    }
    @Override
    public int attackSkill( Char target ) {
        return 8;
    }


    @Override
    protected boolean getCloser( int target ) {
        if (state == HUNTING && Dungeon.level.adjacent(pos,enemy.pos)) {
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
        Ballistica attack = new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT);
        return isInRange(enemy.pos) && attack.collisionPos == enemy.pos && !Dungeon.level.adjacent(pos,enemy.pos);
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
                e.pour(LeafParticle.LEVEL_SPECIFIC, 1f);
                HealingPos.add(e);
            }
        }
        for(Mob mob:Dungeon.level.mobs){
            if(isInRange(mob.pos)){
                int healthHalo = damageRoll();
                mob.HP += healthHalo;
                mob.HP = Math.min(mob.HP, mob.HT);
                mob.sprite.emitter().burst(Speck.factory(Speck.HEALING), healthHalo);
                mob.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", healthHalo);
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
