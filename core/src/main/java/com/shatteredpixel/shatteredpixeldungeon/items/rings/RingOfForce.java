/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class RingOfForce extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_FORCE;
	}

	@Override
	protected RingBuff buff( ) {
		return new Force();
	}

	public static int armedDamageBonus( Char ch ){
		return getBuffedBonus( ch, Force.class);
	}


	// *** Weapon-like properties ***

	private static float tier(int str){
		float tier = Math.max(1, (str - 8)/2f);
		//each str point after 18 is half as effective
		if (tier > 5){
			tier = 5 + (tier - 5) / 2f;
		}
		return tier;
	}

	public static int damageRoll( Char wearer ){
		if (wearer.buff(Force.class) != null) {
			int level = getBuffedBonus(wearer, Force.class);
			float tier = tier(wearer instanceof Hero ? ((Hero) wearer).STR() : 10 + RingOfMight.strengthBonus(wearer));
			return Random.NormalIntRange(min(level, tier), max(level, tier));
		} else {
			//attack without any ring of force influence
			int strength = wearer instanceof Hero ? ((Hero) wearer).STR() : 10 + RingOfMight.strengthBonus(wearer);
			return Random.NormalIntRange(1, Math.max(strength - 8, 1));
		}
	}

	//same as equivalent tier weapon
	public static int min(int lvl, float tier){
		if (lvl <= 0) tier = 1; //tier is forced to 1 if cursed

		return Math.max( 0, Math.round(
				tier +  //base
						lvl     //level scaling
		));
	}

	//same as equivalent tier weapon
	public static int max(int lvl, float tier){
		if (lvl <= 0) tier = 1; //tier is forced to 1 if cursed

		return Math.max( 0, Math.round(
				5*(tier+1) +    //base
						lvl*(tier+1)    //level scaling
		));
	}

	@Override
	public String statsInfo() {
		float tier = tier(Dungeon.hero != null ? Dungeon.hero.STR() : 10);
		if (isIdentified()) {
			int level = soloBuffedBonus();
			String info = Messages.get(this, "stats", min(level, tier), max(level, tier), level);
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				level = combinedBuffedBonus(Dungeon.hero);
				info += "\n\n" + Messages.get(this, "combined_stats", min(level, tier), max(level, tier), level);
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", min(1, tier), max(1, tier), 1);
		}
	}

	@Override
	public String upgradeStat1(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		float tier = tier(Dungeon.hero != null ? Dungeon.hero.STR() : 10);
		return min(level+1, tier) + "-" + max(level+1, tier);
	}

	@Override
	public String upgradeStat2(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Integer.toString(level+1);
	}


	public class Force extends RingBuff {
	}
}
