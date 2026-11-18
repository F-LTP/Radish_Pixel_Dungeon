package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 明灯附魔：照亮持有者的视野，使其在未失明时至少拥有 3+等级 格的视野。
 * 代价是更容易被敌人发现（由视野扩大自然体现）。
 */
public class Lamp extends Weapon.Enchantment {

    //暂不进灵能弓的附魔池
    @Override
    public boolean canEnchant( Weapon weapon ){
        return !isRangedWeapon( weapon );
    }

    private static final ItemSprite.Glowing WARM = new ItemSprite.Glowing( 0xFFD27F );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return WARM;
    }
}
