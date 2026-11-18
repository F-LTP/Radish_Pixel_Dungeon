package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;

/**
 * 心眼附魔：攻击未暴击时积累暴击率与暴击伤害，直到下一次攻击暴击后清空。
 * 每层 +3% 暴击率、+5% 暴击伤害，暴击率加成最多 60%。
 */
public class Insight extends Weapon.Enchantment {

    private static final ItemSprite.Glowing LIGHT_BLUE = new ItemSprite.Glowing( 0x66CCFF );

    public static final float CRIT_CHANCE_PER_STACK = 5f;
    public static final float CRIT_DAMAGE_PER_STACK = 0.10f;
    public static final float MAX_CRIT_CHANCE = 60f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        //实际效果在 OrdinaryAttackDamage.rollCritical 中结算
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return LIGHT_BLUE;
    }

    /** 未暴击的积累层数。 */
    public static class MindEyeTracker extends Buff {

        {
            type = buffType.POSITIVE;
            announced = false;
        }

        public int stacks = 0;
        public float critChance = 0f;
        public float critDamage = 0f;

        private static final String STACKS = "stacks";
        private static final String CRIT_CHANCE = "crit_chance";
        private static final String CRIT_DAMAGE = "crit_damage";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( STACKS, stacks );
            bundle.put( CRIT_CHANCE, critChance );
            bundle.put( CRIT_DAMAGE, critDamage );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            stacks = bundle.getInt( STACKS );
            critChance = bundle.getFloat( CRIT_CHANCE );
            critDamage = bundle.getFloat( CRIT_DAMAGE );
        }

        @Override
        public boolean act() {
            if (target instanceof Hero) {
                KindOfWeapon w = ((Hero) target).belongings.weapon();
                if (!(w instanceof Weapon) || !(((Weapon) w).enchantment instanceof Insight)) {
                    detach();
                    return true;
                }
            }
            spend( TICK );
            return true;
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.COMBO;
        }
    }
}
