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

package com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.IceBlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

//Thrown flask that creates an icy field and several blocks of ice that obstruct
//movement and continuously chill nearby characters.
public class PotionOfSnapFreeze extends ExoticPotion {

	{
		icon = ItemSpriteSheet.Icons.POTION_SNAPFREEZ;
	}

	@Override
	public void shatter(int cell) {

		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			identify();

			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		}

		for (int offset : PathFinder.NEIGHBOURS9){
			int c = cell + offset;
			if (Dungeon.level.insideMap(c) && !Dungeon.level.solid[c]) {

				GameScene.add(Blob.seed(c, 10, Freezing.class));

				if (!Dungeon.level.pit[c]
						&& Dungeon.level.passable[c]
						&& Actor.findChar(c) == null) {
					IceBlock ice = new IceBlock();
					ice.lifespan = 10;
					ice.pos = c;
					GameScene.add(ice);
					Dungeon.level.occupyCell(ice);

					if (Dungeon.level.heroFOV[c]) {
						CellEmitter.get(c).burst(Speck.factory(Speck.BLIZZARD), 4);
					}
				}
			}
		}
	}
}
