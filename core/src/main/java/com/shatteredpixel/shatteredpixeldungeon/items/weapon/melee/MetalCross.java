package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class MetalCross extends MeleeWeapon {


    int count = 0;

    {
        count = Math.round(2.5f);
        image = ItemSpriteSheet.HOLYANKH;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1.1f;
        tier = 1;
        bones = false;
    }

    public int proc(Char attacker, Char defender, int damage ) {

        if (defender.HP <= damage && attacker instanceof Hero){
            ((Hero) attacker).earnExp( 1, getClass() );
            hero.sprite.showStatusWithIcon(Window.TITLE_COLOR, String.valueOf(1), FloatingText.EX_EXP);
        }

        return super.proc(attacker, defender, damage);
    }

    @Override
    public int min(int lvl) {
        return  1 + lvl;
    }

    @Override
    public int max(int lvl) {
        return  8 + lvl * 2;
    }

}

