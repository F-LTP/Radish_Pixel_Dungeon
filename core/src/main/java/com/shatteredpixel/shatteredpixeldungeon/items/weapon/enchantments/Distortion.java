package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * 扭曲附魔：命中时有概率扭曲目标，使其攻击伤害与闪避降低，持续 5 回合。
 */
public class Distortion extends Weapon.Enchantment {

    private static final ItemSprite.Glowing DARK_PURPLE = new ItemSprite.Glowing( 0x4B0082 );

    private static final float PROC_CHANCE = 0.20f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (defender != null && defender.isAlive()
                && Random.Float() < PROC_CHANCE * procChanceMultiplier( attacker )) {
            Buff.prolong( defender, Distorted.class, 5f );
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return DARK_PURPLE;
    }

    /** 扭曲：攻击伤害与闪避降低 40%。 */
    public static class Distorted extends FlavourBuff {

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        @Override
        public void modifyOutgoingAttackDamage( Char attacker, Char defender, DamageInfo info ) {
            info.addDirectMultModifier( 0.6f, "distorted" );
        }

        @Override
        public float evasionAndAccuracyFactor() {
            return 0.6f;
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.CRIPPLE;
        }
    }
}
