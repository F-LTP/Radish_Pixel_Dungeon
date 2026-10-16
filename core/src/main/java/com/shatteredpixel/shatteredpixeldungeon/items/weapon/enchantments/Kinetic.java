/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfBenediction;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class Kinetic extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing( 0xFFFF00 );
	
	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {

		int conservedDamage = 0;
		ConservedDamage stored = attacker.buff(ConservedDamage.class);
		if (stored != null) {
			conservedDamage = stored.consume();
		}

		// 本次命中用到的恒动武器：留下追踪器，让击杀时能读到真正的最终伤害；
		// conservedDamage 记录本次已经释放掉的量，蓄能结算时扣除（实际加算见 ConservedDamage）
		Buff.affect(attacker, KineticTracker.class).conservedDamage = conservedDamage;

		return damage;
	}

	@Override
	public boolean releasesConservedDamage() {
		return true;
	}

	/** 击杀时的蓄能：把溢出伤害按魔纹/祝福倍率折算后存给攻击者。 */
	@SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
	public static void onTargetKilled(CharFinalDamageEvent event) {
		Char attacker = event.getAttacker();
		if (attacker == null || event.getTarget().alignment != Char.Alignment.ENEMY || event.getHpAfter() >= 0) {
			return;
		}

		KineticTracker tracker = attacker.buff(KineticTracker.class);
		if (tracker == null) {
			return;
		}

		int dmgToAdd = -event.getHpAfter();
		dmgToAdd -= tracker.conservedDamage;
		dmgToAdd = Math.round(dmgToAdd * RingOfArcana.enchantPowerMultiplier(attacker) * RingOfBenediction.periodMultiplier(attacker));
		if (dmgToAdd > 0) {
			Buff.affect(attacker, ConservedDamage.class).setBonus(dmgToAdd);
		}
		tracker.detach();
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return YELLOW;
	}

	public static class KineticTracker extends Buff {

		{
			actPriority = Actor.VFX_PRIO;
		}

		public int conservedDamage;

		@Override
		public boolean act() {
			detach();
			return true;
		}
	};
	
	public static class ConservedDamage extends Buff {

		{
			type = buffType.POSITIVE;
		}

		/**
		 * 释放：储存的恒动伤害以下一次普通攻击的固定加算结算（进基础值桶，会吃暴击与各类乘区）。
		 * 这里只负责加算并标记「已释放」，由释放武器的 proc 在命中时取走并消耗（见 consume）。
		 */
		@Override
		public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
			Item weapon = attacker.attackingWeapon();
			if (!(weapon instanceof Weapon) || !((Weapon) weapon).releasesConservedDamage(attacker)) {
				return;
			}

			info.addFlatModifier(damageBonus(), "kinetic");
			released = true;
		}

		/**
		 * 供释放武器的 proc 调用：返回本次命中已经加算掉的量并消耗本 buff。
		 * 没有真正走普通攻击的 proc（能力直接调用的 weapon.proc）不会取走，储存量留到下一次普通攻击。
		 */
		public int consume() {
			if (!released) {
				return 0;
			}
			int bonus = damageBonus();
			detach();
			return bonus;
		}

		/** 本次普通攻击是否已经把储存量加算进 DamageInfo，等待 proc 取走。 */
		private boolean released;
		
		@Override
		public String icon() {
			return BuffIndicator.WEAPON;
		}
		
		@Override
		public void tintIcon(Image icon) {
			if (preservedDamage >= 10){
				icon.hardlight(1f, 0f, 0f);
			} else if (preservedDamage >= 5) {
				icon.hardlight(1f, 1f - (preservedDamage - 5f)*.2f, 0f);
			} else {
				icon.hardlight(1f, 1f, 1f - preservedDamage*.2f);
			}
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(damageBonus());
		}
		
		private float preservedDamage;
		
		public void setBonus(int bonus){
			preservedDamage = bonus;
		}
		
		public int damageBonus(){
			return (int)Math.ceil(preservedDamage);
		}
		
		@Override
		public boolean act() {
			preservedDamage -= Math.max(preservedDamage*.025f, 0.1f);
			if (preservedDamage <= 0) detach();
			
			spend(TICK);
			return true;
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", damageBonus());
		}
		
		private static final String PRESERVED_DAMAGE = "preserve_damage";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(PRESERVED_DAMAGE, preservedDamage);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			if (bundle.contains(PRESERVED_DAMAGE)){
				preservedDamage = bundle.getFloat(PRESERVED_DAMAGE);
			} else {
				preservedDamage = cooldown()/10;
				spend(cooldown());
			}
		}
	}
}
