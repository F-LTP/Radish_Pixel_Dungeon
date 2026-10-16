package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Combos extends Weapon.Enchantment {
    private static ItemSprite.Glowing COMBOS = new ItemSprite.Glowing( 0xD8BFD8 );
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max( 0, weapon.buffedLvl() );
        float procChance = 0.4f + level * 0.04f;
        procChance *= procChanceMultiplier(attacker);
        procChance = Math.min(1f,procChance);

        if (Random.Float() < procChance){
            defender.damage(DamageInfo.of(attacker.damageRoll()/2-defender.drRoll(), DamageType.PHYSICAL, attacker, weapon));
            procChance /=2 ;
            while(Random.Float() < procChance){
                defender.damage(DamageInfo.of(attacker.damageRoll()/2-defender.drRoll(), DamageType.PHYSICAL, attacker, weapon));
                procChance /= 2;
            }
        }

        return damage;
    }
    @Override
    public ItemSprite.Glowing glowing() {
        return COMBOS;
    }

}
