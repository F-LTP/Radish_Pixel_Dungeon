package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.StoneSpiritSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class StoneSpirit extends Mob {
    {
        spriteClass = StoneSpiritSprite.class;

        HP = HT = 120;
        defenseSkill = 5;


        EXP = 9;
        maxLvl = 19;

        properties.add(Property.INORGANIC);

        loot = new ThrowingStone().quantity(Random.Int(1,5));
        lootChance = 1f;
    }

    private boolean isNoticed = false;

    private boolean standOnPure = true;

    public int damageRoll() {
        return standOnPure?Random.NormalIntRange( 10, 25 ):Random.NormalIntRange(4,8);
    }
    @Override
    public int drRoll() {
        return standOnPure?Random.NormalIntRange(5, 10):Random.NormalIntRange(0,5);
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
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
    protected boolean act() {
        if(Dungeon.level.map[pos]== Terrain.EMPTY){
            standOnPure = true;
            if(Dungeon.hero != null && fieldOfView != null)
                if(fieldOfView[Dungeon.hero.pos] && !isNoticed){
                    GLog.n('\n'+ Messages.get(this, "boost"));
                    isNoticed = true;
                }
        }
        else {
            standOnPure=false;
        }
        return super.act();
    }
}
