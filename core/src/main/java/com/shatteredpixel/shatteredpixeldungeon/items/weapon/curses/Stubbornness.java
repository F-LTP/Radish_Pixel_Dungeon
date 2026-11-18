package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;

public class Stubbornness extends Weapon.Enchantment {

        private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing(0x000000);

        private boolean removedOnce = false;

        public boolean isRemoveOnce(){
            return removedOnce;
        }

        public void setRemovedOnce(){
            removedOnce = true;
        }

        @Override
        public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
            return damage;
        }

        @Override
        public boolean curse() {
            return true;
        }

        @Override
        public ItemSprite.Glowing glowing() {
            return BLACK;
        }

        private static final String REMOVED_ONCE = "removed_once";

        //「已经祛除过一次」要进存档，否则读档后归零，玩家得再用两张祛邪卷轴
        @Override
        public void storeInBundle( Bundle bundle ) {
            bundle.put( REMOVED_ONCE, removedOnce );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            removedOnce = bundle.contains( REMOVED_ONCE ) && bundle.getBoolean( REMOVED_ONCE );
        }

}
