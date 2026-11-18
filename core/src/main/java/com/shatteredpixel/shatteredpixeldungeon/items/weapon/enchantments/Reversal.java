package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 逆转附魔：命中时伤害归零，未命中时反而正常造成一次武器伤害。
 */
public class Reversal extends Weapon.Enchantment {

    private static final ItemSprite.Glowing VIOLET = new ItemSprite.Glowing( 0x8A2BE2 );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @Override
    public void modifyDamageInfo( Weapon weapon, Char attacker, Char defender, DamageInfo info ) {
        // 命中分支：伤害归零
        info.addFinalMultModifier( 0f, "reversal" );
    }

    /** 未命中分支：补一次正常武器伤害。 */
    @SubscribeEvent(event = AttackMissEvent.class, priority = 0)
    public static void onMiss( AttackMissEvent event ) {
        if (!(event.getAttackerWeapon() instanceof Weapon)) {
            return;
        }
        Weapon weapon = (Weapon) event.getAttackerWeapon();
        if (!(weapon.enchantment instanceof Reversal)) {
            return;
        }

        Char attacker = event.getAttacker();
        Char defender = event.getDefender();
        if (defender == null || !defender.isAlive() || attacker == null || !attacker.isAlive()) {
            return;
        }
        //魔法免疫期间附魔整体失效：命中分支的伤害归零已经不生效，这里也不能再补伤害
        if (attacker.buff( MagicImmune.class ) != null) {
            return;
        }

        int dmg = Math.max( 1, weapon.damageRoll( attacker ) );
        defender.damage( new DamageInfo( dmg, DamageType.PHYSICAL, attacker, weapon, attacker ) );

        //这次伤害不走普通攻击的流程，那边看到的是「没打中」，不会做死亡结算。拿到这把武器的怪物
        //（活化石像等）靠这一下打死英雄时要在这里结算，否则不记排行榜、也没有死因提示
        if (defender == Dungeon.hero && !defender.isAlive()) {
            Dungeon.fail( attacker.getClass() );
            GLog.n( Messages.capitalize( Messages.get( Char.class, "kill", attacker.name() ) ) );
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return VIOLET;
    }
}
