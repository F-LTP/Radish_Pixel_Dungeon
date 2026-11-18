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
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

//Permanent bonus from the Potion of Mastery. Increases the damage dealt with
//melee and thrown weapons by (10 - weapon tier)%, so low tier weapons benefit more.
public class Mastery extends Buff {

	{
		type = buffType.POSITIVE;
		announced = true;
		revivePersists = true;
	}

	@Override
	public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (attacker != target) {
			return;
		}

		Item weapon = attacker.attackingWeapon();
		int tier;
		if (weapon instanceof MeleeWeapon) {
			tier = ((MeleeWeapon) weapon).tier;
		} else if (weapon instanceof MissileWeapon) {
			tier = ((MissileWeapon) weapon).tier;
		} else {
			return;
		}

		int bonus = Math.max(0, 10 - tier);
		if (bonus > 0) {
			info.addStackMultModifier(1f + bonus / 100f, "mastery");
		}
	}

	@Override
	public String icon() {
		return BuffIndicator.UPGRADE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
}
