package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 脉冲附魔：持有这把武器时，每隔数回合获得一次攻击与精准度强化。
 * 间隔随武器等级缩短（最低 2 回合），强化持续 3 回合。
 */
public class Pulse extends Weapon.Enchantment {

    private static final ItemSprite.Glowing YELLOW = new ItemSprite.Glowing( 0xFFFF66 );

    public static final float DAMAGE_BONUS = 0.25f;
    public static final float ACCURACY_BONUS = 1.25f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        PulseTracker tracker = Buff.affect( attacker, PulseTracker.class );
        tracker.interval = Math.max( 2, 5 - Math.max( 0, weapon.buffedLvl() ) / 2 );

        if (attacker.buff( PulseBoost.class ) != null) {
            damage = Math.round( damage * (1f + DAMAGE_BONUS) );
        }
        return damage;
    }

    @Override
    public float panelDamageMultiplier( Weapon weapon, Char owner, boolean max ) {
        return owner != null && owner.buff( PulseBoost.class ) != null ? 1f + DAMAGE_BONUS : 1f;
    }

    /** 装备该武器时即可开始计时，不必先打中敌人。 */
    public static void onHeroReady( Hero hero ) {
        if (hero == null || !(hero.belongings.weapon() instanceof Weapon)) {
            return;
        }
        Weapon weapon = (Weapon) hero.belongings.weapon();
        if (weapon.enchantment instanceof Pulse) {
            PulseTracker tracker = Buff.affect( hero, PulseTracker.class );
            tracker.interval = Math.max( 2, 5 - Math.max( 0, weapon.buffedLvl() ) / 2 );
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return YELLOW;
    }

    /** 常驻计时器，每回合推进，到点授予一次强化。 */
    public static class PulseTracker extends Buff {

        {
            type = buffType.POSITIVE;
            announced = false;
        }

        public int turns = 0;
        public int interval = 5;

        @Override
        public boolean act() {
            if (target instanceof Hero) {
                KindOfWeapon weapon = ((Hero) target).belongings.weapon();
                if (!(weapon instanceof Weapon) || !(((Weapon) weapon).enchantment instanceof Pulse)) {
                    detach();
                    return true;
                }
            }
            turns++;
            if (turns >= Math.max( 2, interval )) {
                turns = 0;
                Buff.prolong( target, PulseBoost.class, 1f );
            }
            spend( TICK );
            return true;
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.NONE;
        }
    }

    /** 强化期间：伤害与精准度提高。 */
    public static class PulseBoost extends FlavourBuff {

        {
            type = buffType.POSITIVE;
            announced = false;
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.HASTE;
        }
    }
}
