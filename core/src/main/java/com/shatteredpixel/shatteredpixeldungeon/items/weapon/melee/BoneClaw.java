package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class BoneClaw extends MeleeWeapon{
    {
        image = ItemSpriteSheet.BONE_CLAW;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1f;

        tier = 2;
    }

    @Override
    public int max(int lvl) {
        return  4*(tier+1) +    //12 base, down from 15
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public int min(int lvl) {
        return tier+lvl+1;
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        if (defender.buff(first_mark.class)==null){
            damage+=4+buffedLvl();
            Buff.affect(defender, first_mark.class);
        }
        return super.proc(attacker,defender,damage);
    }

    public static class first_mark extends Buff {

    }
}
