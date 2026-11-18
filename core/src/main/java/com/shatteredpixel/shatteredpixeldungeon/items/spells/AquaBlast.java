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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfStormClouds;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class AquaBlast extends TargetedSpell {
	
	{
		image = ItemSpriteSheet.AQUA_BLAST;
		usesTargeting = true;
	}
	
	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {
		int cell = bolt.collisionPos;

		GeyserTrap geyser = new GeyserTrap();
		geyser.pos = cell;
		geyser.source = getClass();
		//落点就是自己脚下时不指定击退方向：起点终点相同的弹道会退化成一条指向左上的斜线，
		//指定了方向还会跳过间歇泉「英雄只往安全方向冲」的保护，左上是深渊就直接掉下去
		if (cell != hero.pos && bolt.path.size() > bolt.dist+1) {
			geyser.centerKnockBackDirection = bolt.path.get(bolt.dist + 1);
		}
		geyser.activate();
	}
	
	@Override
	public int value() {
		//prices of ingredients, divided by output quantity
		return Math.round(quantity * ((60 + 40) / 8f));
	}
	
	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfStormClouds.class, ArcaneCatalyst.class};
			inQuantity = new int[]{1, 1};
			
			cost = 2;
			
			output = AquaBlast.class;
			outQuantity = 8;
		}
		
	}
}
