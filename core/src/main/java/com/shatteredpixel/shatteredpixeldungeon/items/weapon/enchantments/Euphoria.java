package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;


/*
    Class Name  : Euphoria
    Description : Weapon enchantment 4 Radish Dungeon
    Created by DoggingDog on 2024_10_06
*/
public class Euphoria extends Weapon.Enchantment {
    private static ItemSprite.Glowing EUPHORIA = new ItemSprite.Glowing( 0xFF4400 );
    public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
        if(damage > defender.HP && defender.isAlive()){
            Buff.affect(attacker, Adrenaline.class, (float) Math.ceil(6f + weapon.buffedLvl() * procChanceMultiplier(attacker)));
        }
        return damage;
    }
    @Override
    public ItemSprite.Glowing glowing() {
        return EUPHORIA;
    }
}
