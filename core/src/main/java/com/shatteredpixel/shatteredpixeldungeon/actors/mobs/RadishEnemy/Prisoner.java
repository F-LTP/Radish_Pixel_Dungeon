package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.PrisonerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class Prisoner extends Mob {
    {
        spriteClass = PrisonerSprite.class;

        HP = HT = 80;
        defenseSkill = 0;

        baseSpeed = 1f;

        EXP = 5;
        maxLvl = 12;

        loot = Random.oneOf(Generator.Category.SEED,Generator.Category.STONE);
        lootChance =0.4f;
    }
    public int damageRoll() {
        return Random.NormalIntRange( 2, 12 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 15;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 4);
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
    public Item createLoot() {
        if(super.createLoot() instanceof Plant.Seed){
            super.createLoot().quantity((int) (1f/Random.Float(0.4f,1.1f)));
        }
        return super.createLoot();
    }
}
