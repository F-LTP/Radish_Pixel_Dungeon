package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 协调附魔：武器与护甲的等级、阶级差距越小，伤害加成越高。
 */
public class Harmony extends Weapon.Enchantment {

    private static final ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x008080 );

    private static final float MAX_BONUS = 0.30f;
    private static final float PENALTY_PER_GAP = 0.04f;

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
        Armor armor = hero.belongings.armor();

        int gap = 6;
        if (armor != null) {
            gap = Math.abs( weapon.buffedLvl() - armor.buffedLvl() )
                    + Math.abs( weapon.STRReq() - armor.STRReq() );
        }

        float bonus = Math.max( 0f, MAX_BONUS - PENALTY_PER_GAP * gap );
        if (bonus > 0) {
            info.addFinalMultModifier( 1f + bonus, "harmony" );
        }
    }

    @Override
    public float panelDamageMultiplier( Weapon weapon, Char owner, boolean max ) {
        if (!(owner instanceof Hero)) {
            return 1f;
        }
        Hero hero = (Hero) owner;
        Armor armor = hero.belongings.armor();
        int gap = armor == null ? 6
                : Math.abs( weapon.buffedLvl() - armor.buffedLvl() )
                + Math.abs( weapon.STRReq() - armor.STRReq() );
        return 1f + Math.max( 0f, MAX_BONUS - PENALTY_PER_GAP * gap );
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return TEAL;
    }
}
