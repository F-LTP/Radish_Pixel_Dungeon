package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Axe_D extends MeleeWeapon{
    {
        image = ItemSpriteSheet.AXE_D;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 5;
    }

    @Override
    public int max(int lvl) {
        return  5*tier +
                lvl*(tier-1);
    }
    @Override
    public int min(int lvl) {
        return  3*tier +
                lvl*(tier-2);
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        Buff.affect(defender, Executed.class);
        return super.proc(attacker,defender,damage);
    }
    @Override
    public int STRReq(int lvl) {
        return STRReq(tier+1, lvl)-1; //19 base strength req, up from 18
    }
    public static class Executed extends Buff {
        {
            type=buffType.NEGATIVE;
        }
    }
}
