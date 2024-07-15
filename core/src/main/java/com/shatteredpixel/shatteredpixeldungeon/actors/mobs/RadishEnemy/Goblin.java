package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinSprite;
import com.watabou.utils.Random;

public class Goblin extends Mob {
    {
        spriteClass = GoblinSprite.class;

        HP = HT = 20;
        defenseSkill = 4;

        EXP = 5;
        maxLvl = 10;

    }


    public int damageRoll() {
        return Random.NormalIntRange( 2, 5 );
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
        super.die( cause );
    }
}
