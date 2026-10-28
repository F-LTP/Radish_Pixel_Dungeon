package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class LockChain extends MeleeWeapon {

    {
        image = ItemSpriteSheet.LOCK_CHAIN;
        tier = 3;
        RCH = 3;
    }

    private boolean switchEffect = false;

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if(Dungeon.level.distance( defender.pos, attacker.pos ) <= 1){
            if(!switchEffect && Random.Float()>=0.5f){
                Buff.affect(defender, Weakness.class, 2f+level());
                switchEffect = true;
            } else if(switchEffect && Random.Float()>=0.5f) {
                Buff.affect(defender, Bleeding.class).set(2f*level());
                switchEffect = false;
            }
        }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        return 4+lvl;
    }


    @Override
    public int max(int lvl) {
        return 20 + lvl * 4;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("svs", switchEffect);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        switchEffect = bundle.getBoolean("svs");
    }

}
