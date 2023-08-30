package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class RatArmor extends Armor{

    {
        image = ItemSpriteSheet.ARMOR_RAT;
    }

    public RatArmor() {
        super( 2 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new Plague();
    }
    @Override
    public int DRMax(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 3 + lvl + augment.defenseFactor(lvl);
        }

        return 2 * (3 + lvl) + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 1;
        }

        return lvl + 1;
    }
    public class Plague extends ArmorBuff {

        @Override
        public boolean act() {
            spend( TICK*50 );
            switch (Random.Int(5)){
                case 0: default: Buff.affect(target, Hex.class,5f);break;
                case 1: Buff.affect(target, Vertigo.class,5f);break;
                case 2: Buff.affect(target, Weakness.class,5f);break;
                case 3: Buff.affect(target, Vulnerable.class,5f);break;
                case 4: Buff.affect(target, Poison.class).set(3f);break;
            }
            return true;
        }
    }
}
