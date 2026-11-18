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

/**
 * 追寻附魔：命中后标记目标，使施加标记的那把武器可以无视距离追击目标。
 * 标记与武器绑定，最长持续 20 回合；换下武器或任一方死亡时消失。
 */
public class Seeking extends Weapon.Enchantment {

    private static final ItemSprite.Glowing SEEKING = new ItemSprite.Glowing( 0xF0FFF0 );

    /** 标记的绝对上限，不随附魔倍率延长。 */
    public static final float MAX_DURATION = 20f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        SeekingBuff buff = Buff.affect( defender, SeekingBuff.class );
        buff.bind( attacker, weapon );
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return SEEKING;
    }

    public static class SeekingBuff extends Buff {

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        public int attackerID = -1;

        //标记绑定的武器（仅内存中有效，读档后按类名重新建立）
        public transient Weapon weapon;

        //读档时记下的武器类名。读档那一刻角色还没注册进回合系统，找不到攻击者，
        //只能等之后第一次用到时再和英雄手里的武器对上
        private String savedWeaponClass = null;

        //剩余标记回合数，最多为 MAX_DURATION
        public float left = 0;

        private static final String ATTACKER_ID = "attacker_id";
        private static final String LEFT = "left";
        private static final String WEAPON_CLASS = "weapon_class";

        /** 与施加标记的武器绑定，并把持续时间重置为 20 回合上限。 */
        public void bind( Char attacker, Weapon weapon ) {
            this.attackerID = attacker.id();
            this.weapon = weapon;
            this.savedWeaponClass = null;
            this.left = MAX_DURATION;
        }

        public boolean isBoundWeapon( KindOfWeapon w ) {
            //读档后第一次比对：类名对得上就重新绑定
            if (weapon == null && savedWeaponClass != null
                    && w instanceof Weapon && w.getClass().getName().equals( savedWeaponClass )) {
                weapon = (Weapon) w;
                savedWeaponClass = null;
            }
            return w == weapon;
        }

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( ATTACKER_ID, attackerID );
            bundle.put( LEFT, left );
            if (weapon != null) {
                bundle.put( WEAPON_CLASS, weapon.getClass().getName() );
            } else if (savedWeaponClass != null) {
                //读档后还没来得及重新绑定就又存档
                bundle.put( WEAPON_CLASS, savedWeaponClass );
            }
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            attackerID = bundle.getInt( ATTACKER_ID );
            left = Math.min( MAX_DURATION, bundle.getFloat( LEFT ) );
            //武器对象没有稳定 id，读档后按类名在英雄当前武器中重新绑定（见 isBoundWeapon）；
            //对不上时 act() 会移除标记
            weapon = null;
            savedWeaponClass = bundle.contains( WEAPON_CLASS ) ? bundle.getString( WEAPON_CLASS ) : null;
        }

        @Override
        public boolean act() {
            Char attacker = (Char) Actor.findById( attackerID );
            if (attacker == null || !attacker.isAlive() || !target.isAlive()) {
                detach();
                return true;
            }

            //只有英雄的标记与武器绑定：换下这把武器则标记消失
            if (attacker instanceof Hero) {
                KindOfWeapon current = ((Hero) attacker).belongings.attackingWeapon();
                if (current == null || !isBoundWeapon( current )) {
                    detach();
                    return true;
                }
            }

            left -= 1f;
            if (left <= 0) {
                detach();
                return true;
            }

            spend( TICK );
            return true;
        }

        @Override
        public String icon() {
            return BuffIndicator.FORESIGHT;
        }

        @Override
        public String desc() {
            return Messages.get( this, "desc" );
        }
    }
}
