package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DM175_Sprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class DM175 extends Mob {
    {
        spriteClass = DM175_Sprite.class;

        HP = HT = 20;
        defenseSkill = 14;


        EXP = 7;
        maxLvl = 17;

        properties.add(Property.INORGANIC);
        properties.add(Property.LARGE);
        properties.add(Property.HEADLESS);

        loot = Generator.Category.SCROLL;
        lootChance = 0.1f;
    }


    public int damageRoll() {
        return Random.NormalIntRange( 4, 15 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 16;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 6);
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
            GLog.n('\n'+ Messages.get(this, "rankings_desc"));
        }
        return isAttack;
    }
}
