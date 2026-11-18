package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * 流光附魔：攻击后获得短暂加速，持续时间与武器攻速挂钩（武器越快，加速越久）。
 */
public class FlowingLight extends Weapon.Enchantment {

    //暂不进灵能弓的附魔池
    @Override
    public boolean canEnchant( Weapon weapon ){
        return !isRangedWeapon( weapon );
    }

    private static final ItemSprite.Glowing CYAN = new ItemSprite.Glowing( 0x66FFFF );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (!(attacker instanceof Hero)) {
            return damage;
        }
        Hero hero = (Hero) attacker;
        if (Random.Float() >= 0.25f * procChanceMultiplier( attacker )) {
            return damage;
        }

        float delay = Math.max( 0.4f, weapon.delayFactor( hero ) );
        float duration = 1f + 1.5f / delay;
        Buff.prolong( hero, Haste.class, duration );
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return CYAN;
    }
}
