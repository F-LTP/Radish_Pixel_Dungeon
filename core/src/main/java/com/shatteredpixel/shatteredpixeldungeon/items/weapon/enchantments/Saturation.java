package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 饱和附魔：力量超出武器需求越多，伤害加成越高；高阶武器额外提升。
 */
public class Saturation extends Weapon.Enchantment {

    private static final ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFFA500 );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @Override
    public void modifyDamageInfo( Weapon weapon, Char attacker, Char defender, DamageInfo info ) {
        if (!(attacker instanceof Hero)) {
            return;
        }
        Hero hero = (Hero) attacker;
        int surplus = Math.max( 0, hero.STR() - weapon.STRReq() );
        int highTier = Math.max( 0, weapon.STRReq() - 8 );
        float bonus = Math.min( 1.0f, 0.05f * surplus + 0.02f * highTier );
        if (bonus > 0) {
            info.addFinalMultModifier( 1f + bonus, "saturation" );
        }
    }

    @Override
    public float panelDamageMultiplier( Weapon weapon, Char owner, boolean max ) {
        if (!(owner instanceof Hero)) {
            return 1f;
        }
        Hero hero = (Hero) owner;
        int surplus = Math.max( 0, hero.STR() - weapon.STRReq() );
        int highTier = Math.max( 0, weapon.STRReq() - 8 );
        return 1f + Math.min( 1.0f, 0.05f * surplus + 0.02f * highTier );
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return ORANGE;
    }
}
