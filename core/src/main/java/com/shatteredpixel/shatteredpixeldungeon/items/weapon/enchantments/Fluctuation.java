package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * 波动附魔：提高武器最大伤害、降低最小伤害。
 * 最大伤害提高 (50 + 5*等级)%，最小伤害降低该值的 66%。
 */
public class Fluctuation extends Weapon.Enchantment {

    private static final ItemSprite.Glowing PURPLE = new ItemSprite.Glowing( 0x9966CC );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @Override
    public void modifyDamageInfo( Weapon weapon, Char attacker, Char defender, DamageInfo info ) {
        float swing = 0.50f + 0.05f * Math.max( 0, weapon.buffedLvl() );
        float roll = Random.Float( -0.66f * swing, swing );
        info.addFinalMultModifier( 1f + roll, "fluctuation" );
    }

    @Override
    public float panelDamageMultiplier( Weapon weapon, Char owner, boolean max ) {
        float swing = 0.50f + 0.05f * Math.max( 0, weapon.buffedLvl() );
        return max ? 1f + swing : 1f - 0.66f * swing;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return PURPLE;
    }
}
