package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Cutterhead extends MeleeWeapon {
    //private static ItemSprite.Glowing RED = new ItemSprite.Glowing( 0x660022 );
    {
        image = ItemSpriteSheet.CUTTERHEAD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 5;
    }

    //Basically this weapon only does half damage
    @Override
    public int min(int lvl){
        return Math.round(super.min(lvl) * 0.5f);
    }

    @Override
    public int max(int lvl) {
        return Math.round(4 * (tier + 1) +    //24 base, down from 30
                lvl * (tier) * 0.5f);         //scaling down
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (!(defender.properties().contains(Char.Property.INORGANIC))) {
            Bleeding.finishAllBleedingDamage(defender);
            Buff.affect(defender, Bleeding.class).set(damage, this.getClass());
        }
        return super.proc(attacker, defender, damage);
    }
}
