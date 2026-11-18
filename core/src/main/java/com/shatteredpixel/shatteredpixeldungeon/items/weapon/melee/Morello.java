package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

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
        // 装备可能失败（手里是脱不下来的诅咒武器、村正狂热），确认装上了再挂 buff
        if (ParentDoEquip) {
            Buff.affect(hero,MorelloCabala.class).updateMaxCharge(hero);
        }
        return ParentDoEquip;
    }


    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
       boolean ParentDoUnEquip =  super.doUnequip(hero,collect,single);
       // 同理，秘典被诅咒卸不下来时 buff 不能摘
       if (ParentDoUnEquip) {
           if(hero.buff(MorelloCabala.class) != null)
               Buff.detach(hero,MorelloCabala.class);
           // 法杖只在「身上还有秘典」时才会重新计算充能上限。
           // 把秘典从手上直接丢到地上之后就再没人算了，加出来的上限会一直留着，所以卸下时主动重算一遍
           for (Wand wand : hero.belongings.getAllItems(Wand.class)) {
               wand.updateLevel();
           }
       }
       return ParentDoUnEquip;
    }
    public static class MorelloCabala extends Buff {

        {
            type = buffType.POSITIVE;
            announced = true;
        }

        private int buffCharge = 0;

       public void updateMaxCharge(Hero hero){
           Morello morello = com.shatteredpixel.shatteredpixeldungeon.items.Fusion.fusedItem(hero.belongings.weapon, Morello.class);
           if(morello != null){
           			this.buffCharge = 2 + (int)Math.ceil(morello.buffedLvl() * 0.5f);
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
        public String icon() {
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
