package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class KnightGS extends MeleeWeapon {

    {
        image = ItemSpriteSheet.KNIGHT_GS;
        tier = 5;
    }

//    @Override
//    public boolean doEquip( Hero hero ) {
//        Buff.affect(hero, CritChance.class);
//        return super.doEquip(hero);
//    }
//
//    @Override
//    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
//        Buff.detach(hero, CritChance.class);
//        return super.doUnequip(hero, collect, single);
//    }

    @Override
    public int min(int lvl) {
        return 4+lvl;
    }

    @Override
    public int max(int lvl) {
        return 40 + lvl * 4;
    }

}
