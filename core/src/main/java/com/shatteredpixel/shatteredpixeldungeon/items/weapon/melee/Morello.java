package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class Morello extends MeleeWeapon {

    {
        image = ItemSpriteSheet.MORELLO_BOOK;
        tier = 3;
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 16 + lvl * 2;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        boolean ParentDoEquip = super.doEquip(hero);
        Buff.affect(hero,MorelloCabala.class).updateMaxCharge(hero);
        return ParentDoEquip;
    }


    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
       boolean ParentDoUnEquip =  super.doUnequip(hero,collect,single);
       if(hero.buff(MorelloCabala.class) != null)
           Buff.detach(hero,MorelloCabala.class);
       return ParentDoUnEquip;
    }
    public static class MorelloCabala extends Buff {

        {
            type = buffType.POSITIVE;
            announced = true;
        }

        private int buffCharge = 0;

       public void updateMaxCharge(Hero hero){
           if(hero.belongings.weapon instanceof Morello){
               KindOfWeapon morello = hero.belongings.weapon;
               this.buffCharge = 1 + (int)Math.ceil(morello.level() * 0.5f);
           }
       }

        @Override
        public void detach() {
            super.detach();
            updateQuickslot();
        }

        public int buffCharge(){
            return this.buffCharge;
        }

        @Override
        public int icon() {
            return BuffIndicator.UPGRADE;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.2f, 0.6f, 1f);
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", buffCharge());
        }

    }
}
