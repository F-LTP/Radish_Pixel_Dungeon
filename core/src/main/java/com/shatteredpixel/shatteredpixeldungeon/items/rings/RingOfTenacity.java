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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfTenacity extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_TENACITY;
	}

	public String statsInfo() {
		if (isIdentified()){
			if (!cursed)
				return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.05f, soloBuffedBonus()) - 1f)),new DecimalFormat("#.##").format(100f*(1.1f-Math.max(0.11f,Math.pow(0.944, soloBuffedBonus()-1f)))));
			else
				return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.05f, soloBuffedBonus()) - 1f)),new DecimalFormat("#.##").format(100f*(1f-Math.pow(0.944, soloBuffedBonus()))));
		} else {
			return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(5f),new DecimalFormat("#.##").format(10f));
		}
	}

	@Override
	protected RingBuff buff( ) {
		return new Tenacity();
	}

	public static float damageMultiplier( Char t ){
		int gbb=getBuffedBonus( t, Tenacity.class);
		if (gbb>0)
			return Math.max(0.01f,(float)Math.pow(0.944,gbb-1)-0.1f);
		else
			return (float)(Math.pow(0.944,gbb));
	}
	public static float attackMultiplier( Char t ){
		int gbb=getBuffedBonus( t, Tenacity.class);
		return (float)Math.pow(1.05,gbb);
	}

	public class Tenacity extends RingBuff {
	}
}

