package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Darksword extends MeleeWeapon {
    //private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );
    {
        image = ItemSpriteSheet.DARKSWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 4;
    }
    @Override
    public int min(int lvl) {
        return  1 +    //1 base
                lvl;
    }
    @Override
    public int max(int lvl) {
        return  3*tier +        //12 base
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        int negcnt=0;
        for (Buff b : defender.buffs()){
            if (b.type == Buff.buffType.NEGATIVE) negcnt++;
        }
        int dmgbonus=(12+3*buffedLvl())*negcnt;
        return super.proc(attacker,defender,damage+dmgbonus);
    }

}
