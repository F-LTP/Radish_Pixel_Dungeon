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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfBenediction;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Healing extends Buff {

	private int healingLeft;
	
	private float percentHealPerTick;
	private int flatHealPerTick;
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
		
		type = buffType.POSITIVE;
	}
	
	@Override
	public boolean act(){

		if (HealingBlocked.isBlocked(target)){
			spend( TICK );
			return true;
		}

		if (target instanceof Hero) {
			com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToyEffects.heal((Hero) target, healingThisTick());
			if (target.HP == target.HT) ((Hero) target).resting = false;
		} else if (target.HP < target.HT) {
				target.HP = Math.min(target.HT, target.HP + healingThisTick());
				target.sprite.showStatusWithIcon(
						CharSprite.POSITIVE, Integer.toString(healingThisTick()), FloatingText.HEALING);
		}

		healingLeft -= healingThisTick();
		
		if (healingLeft <= 0){
			if (target instanceof Hero) {
				((Hero) target).resting = false;
			}
			detach();
		}
		
		spend( TICK );
		
		return true;
	}
	



	private int healingThisTick(){
		int heal = (int)GameMath.gate(1, Math.round(healingLeft * percentHealPerTick) + flatHealPerTick,
				healingLeft);

		if (Dungeon.isChallenged(Challenges.DAMAGE_NO)){
			heal = 1;
		}
		return heal;
	}

	public void setHeal(int amount, float percentPerTick, int flatPerTick){
		if (target == Dungeon.hero){
			Buff ben=Dungeon.hero.buff(RingOfBenediction.Benediction.class);
			if (ben!=null){
				amount*= (int) RingOfBenediction.periodMultiplier(target);
			}
		}
		healingLeft = amount;
		percentHealPerTick = percentPerTick;
		flatHealPerTick = flatPerTick;
	}
	
	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add( CharSprite.State.HEALING );
		else    target.sprite.remove( CharSprite.State.HEALING );
	}
	
	private static final String LEFT = "left";
	private static final String PERCENT = "percent";
	private static final String FLAT = "flat";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, healingLeft);
		bundle.put(PERCENT, percentHealPerTick);
		bundle.put(FLAT, flatHealPerTick);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		healingLeft = bundle.getInt(LEFT);
		percentHealPerTick = bundle.getFloat(PERCENT);
		flatHealPerTick = bundle.getInt(FLAT);
	}
	
	@Override
	public String icon() {
		return BuffIndicator.HEALING;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(healingLeft);
	}
	
	@Override
	public String desc() {
		return Messages.get(this, "desc", healingThisTick(), healingLeft);
	}

	public static class StarHealing extends Healing{

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(Window.SHPX_COLOR);
		}

		@Override
		public String icon() {
			return BuffIndicator.HERB_HEALING;
		}
	}

}
