package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 精制附魔：装备性能提高 50%，但无法再通过升级卷轴升级。
 */
public class Refined extends Weapon.Enchantment {

    //暂不进灵能弓的附魔池
    @Override
    public boolean canEnchant( Weapon weapon ){
        return !isRangedWeapon( weapon );
    }

    private static final ItemSprite.Glowing PLATINUM = new ItemSprite.Glowing( 0xE5E4E2 );

    private static final float PERFORMANCE_BONUS = 0.50f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @Override
    public void modifyDamageInfo( Weapon weapon, Char attacker, Char defender, DamageInfo info ) {
        info.addFinalMultModifier( 1f + PERFORMANCE_BONUS, "refined" );
    }

    @Override
    public float panelDamageMultiplier( Weapon weapon, Char owner, boolean max ) {
        return 1f + PERFORMANCE_BONUS;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return PLATINUM;
    }
}
