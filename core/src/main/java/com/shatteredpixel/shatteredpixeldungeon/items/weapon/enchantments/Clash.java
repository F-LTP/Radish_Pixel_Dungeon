package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 交锋附魔：武器伤害提高 40%，但被命中的敌人下一次攻击伤害提高 50%。
 */
public class Clash extends Weapon.Enchantment {

    private static final ItemSprite.Glowing RED = new ItemSprite.Glowing( 0xCC3333 );

    private static final float BONUS_DAMAGE = 0.40f;
    private static final float ENEMY_BONUS = 0.50f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (defender != null && defender.isAlive()) {
            Buff.affect( defender, ClashMark.class );
        }
        return Math.round( damage * (1f + BONUS_DAMAGE) );
    }

    @Override
    public float panelDamageMultiplier( Weapon weapon, Char owner, boolean max ) {
        return 1f + BONUS_DAMAGE;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return RED;
    }

    /** 敌人下一次攻击的伤害提高，攻击后消耗。 */
    public static class ClashMark extends Buff {

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        public int left = 3;

        @Override
        public boolean act() {
            if (--left <= 0) {
                detach();
                return true;
            }
            spend( TICK );
            return true;
        }

        @Override
        public void modifyOutgoingAttackDamage( Char attacker, Char defender, DamageInfo info ) {
            info.addDirectMultModifier( 1f + ENEMY_BONUS, "clash" );
            detach();
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.VULNERABLE;
        }
    }
}
