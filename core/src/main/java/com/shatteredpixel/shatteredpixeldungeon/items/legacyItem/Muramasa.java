package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

// 村正
public class Muramasa extends MeleeWeapon {
    {
        image = ItemSpriteSheet.KATANA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = 1;
    }

    @Override
    public int min(int lvl) {
        return 1;
    }

    @Override
    public int max(int lvl) {
        return 30+lvl*8;
    }
    @Override
    public boolean doEquip( Hero hero ) {
        boolean ParentDoEquip = super.doEquip(hero);
        Buff.affect(hero, MuramasaMania.class);
        return ParentDoEquip;
    }


//    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
//        boolean ParentDoUnEquip =  super.doUnequip(hero,collect,single);
//        if(hero.buff(MuramasaMania.class) != null)
//            Buff.detach(hero, MuramasaMania.class);
//        return ParentDoUnEquip;
//    }

    public static class MuramasaMania extends Buff {
        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        @Override
        public void detach() {
            super.detach();
            updateQuickslot();
        }

        @Override
        public String icon() {
            return BuffIndicator.AMOK;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.2f, 0.6f, 1f);
        }

    }
}
