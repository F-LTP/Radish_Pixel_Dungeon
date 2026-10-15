package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.JailerSprite;
import com.watabou.utils.Random;

public class Jailer extends Mob {
    {
        spriteClass = JailerSprite.class;

        HP = HT = 30;
        defenseSkill = 6;

        baseSpeed = 4f;

        EXP = 5;
        maxLvl = 12;

        properties.add(Property.UNDEAD);
        properties.add(Property.JAIL_INHABITANT);

        state = WANDERING;

    }
    @Override
    protected boolean act() {
        if(fieldOfView != null && Dungeon.hero!= null){
            if(fieldOfView[Dungeon.hero.pos]){
                baseSpeed = 1f;
            }
        }
        return super.act();
    }
    public int damageRoll() {
        return Random.NormalIntRange( 2, 8 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 12;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 3);
    }

    @Override
    public void die( Object cause ) {
        if(Random.Float(0f,1f)<0.3f){
            EXP *= 2;
        }
        super.die( cause );
    }

    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(Jailer.class, "kill"));
    }

    // add if it really hate prisoner
    @Override
    protected Char chooseEnemy() {
        for (Mob mob : Dungeon.level.mobs) {
            if (!(mob == this)
                    && mob.alignment != Alignment.NEUTRAL
                    && !mob.isInvulnerable(getClass())
                    && !(alignment == Alignment.ALLY && mob.alignment == Alignment.ALLY)
                    && mob instanceof Prisoner
                    && fieldOfView[mob.pos]) {
               return mob;
            }
        }
        return super.chooseEnemy();
    }
}
