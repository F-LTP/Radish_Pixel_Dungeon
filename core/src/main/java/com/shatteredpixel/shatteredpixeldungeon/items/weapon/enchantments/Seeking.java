package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Seeking extends Weapon.Enchantment {
    private static ItemSprite.Glowing SEEKING = new ItemSprite.Glowing( 0xF0FFF0 );

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
        SeekingBuff buff = Buff.affect(defender, SeekingBuff.class);
        buff.attackerID = attacker.id();
        return damage;
    }
    @Override
    public ItemSprite.Glowing glowing() {
        return SEEKING;
    }

    public static class SeekingBuff extends Buff{
        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        public int attackerID = -1;

        private static final String ATTACKER_ID = "attacker_id";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(ATTACKER_ID, attackerID);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            attackerID = bundle.getInt(ATTACKER_ID);
        }

        @Override
        public boolean act() {
            // 如果攻击者或目标（被标记者）任一方死亡，移除标记
            Char attacker = (Char) Actor.findById(attackerID);
            if (attacker == null || !attacker.isAlive() || !target.isAlive()) {
                detach();
            }
            spend(TICK);
            return true;
        }

        @Override
        public String icon() {
            return BuffIndicator.FORESIGHT;
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }

    }
}
