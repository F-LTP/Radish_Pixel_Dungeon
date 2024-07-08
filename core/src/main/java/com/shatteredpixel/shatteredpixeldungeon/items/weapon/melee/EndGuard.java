package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class EndGuard extends MeleeWeapon {

    {
        image = ItemSpriteSheet.ENDDAY_KILL;
        tier  = 4;
    }

    @Override
    public int min(int lvl) {
        return 4 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 16+ lvl * 4;
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
            desc = Messages.get(this, "desc",( 20 * (level()+1) ));
        } else {
            desc = Messages.get(this, "normal_desc",50);
        }

        return desc;
    }

}
