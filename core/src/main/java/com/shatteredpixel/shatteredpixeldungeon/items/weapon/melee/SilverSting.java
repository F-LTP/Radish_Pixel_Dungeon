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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ai.WeaponAITag;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class SilverSting extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SILVER_STING;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1f;

		tier = 2;
		// 怪物持有时获得门后伏击 AI（行为见 actors.mobs.ai.AmbushAI）
		aiTag = WeaponAITag.AMBUSH;
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //12 base, down from 15
				lvl*(tier+1);   //scaling unchanged
	}

	@Override
	public int damageRoll(Char owner) {
		Char enemy = Char.enemyOf(owner);
		if (Char.isSurpriseAttack(owner, enemy)) {
			//deals 35% toward max to max on surprise, instead of min to max.
			int diff = max() - min();
			int damage = augment.damageFactor(Random.NormalIntRange(
					min() + Math.round(diff*0.35f),
					max()));
			int exStr = owner instanceof Hero ? ((Hero) owner).STR() - STRReq() : 0;
			if (enemy.properties().contains(Char.Property.UNDEAD) || enemy.properties().contains(Char.Property.DEMONIC)){
				damage*=1.25f;
			}
			if (exStr > 0) {
				damage += Random.IntRange(0, exStr);
			}
			return damage;
		}
		return super.damageRoll(owner);
	}

}