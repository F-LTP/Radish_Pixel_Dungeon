package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Cutterhead  extends MeleeWeapon{
    private static ItemSprite.Glowing RED = new ItemSprite.Glowing( 0x660022 );
    {
        image = ItemSpriteSheet.GREATAXE;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 5;
    }
    @Override
    public int max(int lvl) {
        return  4*(tier+1) +    //24 base, down from 30
                lvl*(tier);   //scaling down
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        int dmgbonus=0;
        if (!(defender.properties().contains(Char.Property.INORGANIC))) {
            dmgbonus += damage / 4;
            Buff.affect( defender, Bleeding.class).set( 3+0.5f*buffedLvl(), this.getClass());
        }
        return super.proc(attacker,defender,damage+dmgbonus);
    }
    @Override
    public ItemSprite.Glowing glowing() {
        return RED;
    }
}
