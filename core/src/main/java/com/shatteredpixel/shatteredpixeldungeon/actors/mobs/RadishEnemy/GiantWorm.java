package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GiantWormSprite;
import com.watabou.utils.Random;


public class GiantWorm extends Mob {

    {
        spriteClass = GiantWormSprite.class;

        HP = HT = 10;
        defenseSkill = 1;

        EXP = 1;
        maxLvl = 5;

    }



    public int damageRoll() {
        return Random.NormalIntRange( 1, 3 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 8;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        int healthSteal = Random.Int(1,3);
        HP += healthSteal;
        this.sprite.emitter().burst(Speck.factory(Speck.HEALING), healthSteal);
        this.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", healthSteal);
        return damage;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 2);
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
    }

}
