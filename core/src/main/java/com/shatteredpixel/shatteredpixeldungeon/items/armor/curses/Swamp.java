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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.curses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Swamp extends Armor.Glyph {

	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage ) {

		//no proc effect, see armor.speedfactor and burning.reignite
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}

	@Override
	public boolean curse() {
		return true;
	}

	//踩到水时获得湿润buff，令减速效果暂时失效
	@SubscribeEvent(event = HeroMoveEvent.class)
	public static void onHeroMove( HeroMoveEvent event ) {
		Hero hero = event.getHero();
		Armor armor = hero.belongings.armor();
		if (armor != null
				&& armor.hasGlyph(Swamp.class, hero)
				&& !hero.flying
				&& Dungeon.level.water[event.getToCell()]) {
			Buff.prolong(hero, Waterlogged.class, 30f);
		}
	}

	public static class Waterlogged extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public String icon() {
			return BuffIndicator.HOLD_BREATH;
		}
	}

}
