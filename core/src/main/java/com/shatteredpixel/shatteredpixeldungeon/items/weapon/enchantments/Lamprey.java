package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;


/*
    Class Name  : Lamprey
    Description : Weapon enchantment 4 Radish Dungeon
    Created by DoggingDog on 2024_10_06
*/
public class Lamprey extends Weapon.Enchantment {
    private static ItemSprite.Glowing LAMPREY = new ItemSprite.Glowing( 0x0000FF );
    public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
        if(damage > defender.HP && defender.isAlive()){
            if(Dungeon.hero != null){
                Wand wand = Dungeon.hero.belongings.getItem(Wand.class);
                if(wand != null){
                    wand.partialCharge += Math.floor(procChanceMultiplier(attacker));
                    wand.partialCharge = Math.min(wand.maxCharges,wand.partialCharge);
                }
            }
        }
        return damage;
    }
    @Override
    public ItemSprite.Glowing glowing() {
        return LAMPREY;
    }
}
