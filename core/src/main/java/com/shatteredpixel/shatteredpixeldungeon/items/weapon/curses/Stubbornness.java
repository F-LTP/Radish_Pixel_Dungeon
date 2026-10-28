package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

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

}
