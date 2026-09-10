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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Chill extends FlavourBuff {

	public static final float DURATION = 10f;

	//the maximum speed reduction chill can inflict (66%)
	public static final float MAX_SLOW = 0.66f;

	//turns of chill needed to reach the maximum slow
	public static final float MAX_TURNS = MAX_SLOW * 10f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public boolean attachTo(Char target) {
		Buff.detach( target, Burning.class );

		return super.attachTo(target);
	}

	//reduces speed by 10% for every turn remaining, capping at 66%
	public float speedFactor(){
		return Math.max(1 - MAX_SLOW, 1 - cooldown()*0.1f);
	}

	//applies chill up to the slow cap, freezing the target if the chill would go beyond it.
	//used by frost effects that can freeze (chilling enchantment, wand of frost)
	public static void chillOrFreeze(Char target, float duration){
		if (target.buff(Frost.class) != null) return;
		if (target.isImmune(Chill.class)) return;

		float resist = target.resist(Chill.class);
		Chill existing = target.buff(Chill.class);
		float current = existing == null ? 0f : existing.cooldown();

		float room = MAX_TURNS - current;
		if (room <= 0f){
			freeze(target);
		} else if (duration * resist >= room){
			//fill the remaining chill, then freeze
			Buff.affect(target, Chill.class, room / resist);
			freeze(target);
		} else {
			Buff.affect(target, Chill.class, duration);
		}
	}

	private static void freeze(Char target){
		if (!target.isImmune(Frost.class)){
			Buff.affect(target, Frost.class, Frost.DURATION);
		}
	}

	@Override
	public String icon() {
		return BuffIndicator.FROST;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.CHILLED);
		else target.sprite.remove(CharSprite.State.CHILLED);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(), Messages.decimalFormat("#.##", (1f-speedFactor())*100f));
	}
}
