package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * 水晶附魔：命中时有概率使目标结晶，结晶期间目标受到的伤害提高 15%。
 */
public class Crystal extends Weapon.Enchantment {

    private static final ItemSprite.Glowing CRYSTAL = new ItemSprite.Glowing( 0xAFEEEE );

    private static final float PROC_CHANCE = 0.20f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (defender != null && defender.isAlive()
                && Random.Float() < PROC_CHANCE * procChanceMultiplier( attacker )) {
            Buff.prolong( defender, Crystallized.class, 3f );
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return CRYSTAL;
    }

    /** 结晶：受到的伤害提高。 */
    public static class Crystallized extends FlavourBuff {

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        @Override
        public void modifyIncomingAttackDamage( Char attacker, Char defender, DamageInfo info ) {
            info.addFinalMultModifier( 1.15f, "crystal" );
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.FROST;
        }
    }
}
