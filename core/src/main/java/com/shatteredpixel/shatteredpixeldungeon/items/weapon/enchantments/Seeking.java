package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Seeking extends Weapon.Enchantment {
    private static ItemSprite.Glowing SEEKING = new ItemSprite.Glowing( 0xF0FFF0 );

    //基础标记持续回合数，会乘算附魔倍率（含奥术之戒等）
    private static final float BASE_DURATION = 20f;

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
        SeekingBuff buff = Buff.affect(defender, SeekingBuff.class);
        if (buff != null) {
            buff.attackerID = attacker.id();
            buff.weapon = weapon;
            buff.left = BASE_DURATION * genericProcChanceMultiplier(attacker);
        }
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

        //标记绑定的武器（仅内存中有效，存档后通过武器类重新建立）
        public transient Weapon weapon;

        //剩余标记回合数
        public float left = 0;

        private static final String ATTACKER_ID = "attacker_id";
        private static final String LEFT = "left";
        private static final String WEAPON_CLASS = "weapon_class";

        //判断给定武器是否为施加标记的那把武器
        public boolean isBoundWeapon( KindOfWeapon w ) {
            return w == weapon;
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(ATTACKER_ID, attackerID);
            bundle.put(LEFT, left);
            if (weapon != null) {
                bundle.put(WEAPON_CLASS, weapon.getClass().getName());
            }
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            attackerID = bundle.getInt(ATTACKER_ID);
            left = bundle.getFloat(LEFT);
            // 非唯一武器没有稳定 id，存档后无法直接恢复对象引用；
            // 用武器类名在英雄当前装备中重新建立绑定，找不到则下次 act 时移除
            weapon = null;
            if (bundle.contains(WEAPON_CLASS)) {
                String cls = bundle.getString(WEAPON_CLASS);
                Char attacker = (Char) Actor.findById(attackerID);
                if (attacker instanceof Hero) {
                    KindOfWeapon current = ((Hero) attacker).belongings.attackingWeapon();
                    if (current != null && current.getClass().getName().equals(cls) && current instanceof Weapon) {
                        weapon = (Weapon) current;
                    }
                }
            }
        }

        @Override
        public boolean act() {
            // 攻击者或目标（被标记者）任一方死亡，移除标记
            Char attacker = (Char) Actor.findById(attackerID);
            if (attacker == null || !attacker.isAlive() || !target.isAlive()) {
                detach();
                return true;
            }

            // 只有攻击者是英雄时，标记才与武器绑定：换下这把武器则标记消失
            if (attacker instanceof Hero) {
                KindOfWeapon current = ((Hero) attacker).belongings.attackingWeapon();
                if (current == null || !isBoundWeapon(current)) {
                    detach();
                    return true;
                }
            }

            // 20 回合（乘倍率）上限
            left -= 1f;
            if (left <= 0) {
                detach();
                return true;
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
