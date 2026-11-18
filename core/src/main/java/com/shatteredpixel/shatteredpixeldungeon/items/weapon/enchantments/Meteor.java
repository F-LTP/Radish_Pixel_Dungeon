package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.events.CharUnprocedDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 流星附魔：从高处坠落后，接下来 5 回合的攻击伤害提高 50%。
 */
public class Meteor extends Weapon.Enchantment {

    private static final ItemSprite.Glowing ORANGE_RED = new ItemSprite.Glowing( 0xFF4500 );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    /** 浮空结束（落地）时也触发，不再只依赖坠落伤害。 */
    public static void onLanding( Char ch ) {
        if (!(ch instanceof Hero)) {
            return;
        }
        Hero hero = (Hero) ch;
        if (!(hero.belongings.weapon() instanceof Weapon)) {
            return;
        }
        Weapon weapon = (Weapon) hero.belongings.weapon();
        if (weapon.enchantment instanceof Meteor) {
            Buff.prolong( hero, MeteorBuff.class, 5f );
        }
    }

    @SubscribeEvent(event = CharUnprocedDamageEvent.class, priority = 0)
    public static void onFallDamage( CharUnprocedDamageEvent event ) {
        if (event.getType() != DamageType.FALL) {
            return;
        }
        if (!(event.getTarget() instanceof Hero)) {
            return;
        }
        Hero hero = (Hero) event.getTarget();
        if (!(hero.belongings.weapon() instanceof Weapon)) {
            return;
        }
        Weapon weapon = (Weapon) hero.belongings.weapon();
        if (weapon.enchantment instanceof Meteor) {
            Buff.prolong( hero, MeteorBuff.class, 5f );
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return ORANGE_RED;
    }

    /** 坠落后的爆发期：攻击伤害提高 50%。 */
    public static class MeteorBuff extends FlavourBuff {

        {
            type = buffType.POSITIVE;
            announced = false;
        }

        @Override
        public void modifyOutgoingAttackDamage( Char attacker, Char defender, DamageInfo info ) {
            info.addFinalMultModifier( 1.5f, "meteor" );
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.UPGRADE;
        }
    }
}
