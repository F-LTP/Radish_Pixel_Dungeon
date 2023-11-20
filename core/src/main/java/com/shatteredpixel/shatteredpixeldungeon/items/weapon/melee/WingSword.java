package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class WingSword extends MeleeWeapon{
    {
        image = ItemSpriteSheet.WINGSWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 3;
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        if (Random.Float()<0.4+0.04*buffedLvl())
            Buff.affect(attacker, Levitation.class,8+buffedLvl());
        return super.proc(attacker,defender,damage);
    }
}
