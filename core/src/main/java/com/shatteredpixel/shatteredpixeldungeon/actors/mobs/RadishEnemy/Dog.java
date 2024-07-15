package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DogSprite;
import com.watabou.utils.Random;

public class Dog extends Mob {
    {
        spriteClass = DogSprite.class;

        HP = HT = 15;
        defenseSkill = 4;

        baseSpeed = 1.5f;

        EXP = 4;
        maxLvl = 10;

        loot = new MysteryMeat();
        lootChance = 0.25f;
    }


    public int damageRoll() {
        return Random.NormalIntRange( 2, 5 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 12;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 2);
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
    }
}
