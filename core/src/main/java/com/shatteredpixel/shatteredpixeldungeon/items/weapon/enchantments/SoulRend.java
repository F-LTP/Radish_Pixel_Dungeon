package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 夺灵附魔：攻击额外造成相当于伤害 10% 的真实伤害，
 * 无视护甲、抗性与伤害上限，因此对处于减伤/限伤状态的目标仍能造成微弱伤害。
 */
public class SoulRend extends Weapon.Enchantment {

    private static final ItemSprite.Glowing SPIRIT = new ItemSprite.Glowing( 0x7FFFD4 );

    private static final float TRUE_DAMAGE_RATIO = 0.10f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (damage <= 0 || defender == null || !defender.isAlive()) {
            return damage;
        }
        int extra = Math.max( 1, Math.round( damage * TRUE_DAMAGE_RATIO ) );
        defender.damage( new DamageInfo( extra, DamageType.TRUE, attacker, weapon, this ) );
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return SPIRIT;
    }
}
