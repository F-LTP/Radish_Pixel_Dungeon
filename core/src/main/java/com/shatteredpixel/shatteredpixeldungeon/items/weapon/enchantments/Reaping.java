package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;

/**
 * 收割附魔：用这把武器击杀生命值仅剩 1 点的敌人时，永久提高本武器的伤害。
 * 每层提供 +1 伤害，本次攻击也会吃到新获得的层数。
 */
public class Reaping extends Weapon.Enchantment {

    private static final ItemSprite.Glowing DARK_RED = new ItemSprite.Glowing( 0x8B0000 );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (!(attacker instanceof Hero) || defender == null) {
            return damage;
        }

        ReapingTracker tracker = attacker.buff( ReapingTracker.class );

        if (defender.isAlive() && defender.alignment == Char.Alignment.ENEMY
                && defender.HP <= 1) {
            tracker = Buff.affect( attacker, ReapingTracker.class );
            if (tracker.stacks < 10) {
                tracker.stacks++;
            }
        }

        int bonus = tracker == null ? 0 : tracker.stacks;
        return damage + bonus;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return DARK_RED;
    }

    @Override
    public int panelFlatBonus( Weapon weapon, Char owner ) {
        ReapingTracker tracker = owner == null ? null : owner.buff( ReapingTracker.class );
        return tracker == null ? 0 : tracker.stacks;
    }

    /** 收割层数，永久保留。 */
    public static class ReapingTracker extends Buff {

        {
            type = buffType.POSITIVE;
            announced = false;
        }

        public int stacks = 0;

        private static final String STACKS = "stacks";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( STACKS, stacks );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            stacks = bundle.getInt( STACKS );
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.UPGRADE;
        }
    }
}
