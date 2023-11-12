package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class RatArmor extends Armor{
    int debuffCount=0;
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
    private static final String DEBUFF_COUNT       = "debuff_count";

    @Override
    public void storeInBundle( Bundle bundle ) {
        bundle.put(DEBUFF_COUNT,debuffCount);
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(DEBUFF_COUNT))
            debuffCount = bundle.getInt(DEBUFF_COUNT);
        else
            debuffCount =0;
    }
    public class Plague extends ArmorBuff {

        @Override
        public boolean act() {
            spend( TICK);
            debuffCount++;
            if (debuffCount>=30) {
                switch (Random.Int(5)) {
                    case 0:
                    default:
                        Buff.affect(target, Hex.class, 5f);
                        break;
                    case 1:
                        Buff.affect(target, Vertigo.class, 5f);
                        break;
                    case 2:
                        Buff.affect(target, Weakness.class, 5f);
                        break;
                    case 3:
                        Buff.affect(target, Vulnerable.class, 5f);
                        break;
                    case 4:
                        Buff.affect(target, Poison.class).set(3f);
                        break;
                }
                debuffCount-=30;
            }
            return true;
        }
    }
}
