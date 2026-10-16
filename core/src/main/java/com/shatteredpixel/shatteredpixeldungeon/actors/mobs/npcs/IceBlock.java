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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalSpireSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

//A block of ice that obstructs movement and continuously chills adjacent characters.
//Created by the Potion of Crystal Ice. It is invulnerable and simply melts away.
public class IceBlock extends NPC {

	{
		spriteClass = CrystalSpireSprite.Blue.class;
	}

	public int lifespan = 10;

	@Override
	protected boolean act() {
		if (lifespan <= 0 || !isAlive()) {
			HP = 0;
			destroy();
			sprite.die();
			return true;
		}

		for (int i : PathFinder.NEIGHBOURS8) {
			Char ch = Actor.findChar(pos + i);
			if (ch != null && !ch.isImmune(Chill.class)) {
				Buff.affect(ch, Chill.class, 3f);
			}
		}

		if (Dungeon.level.heroFOV[pos]) {
			CellEmitter.get(pos).burst(Speck.factory(Speck.BLIZZARD), 1);
		}

		lifespan--;
		spend(TICK);
		return true;
	}

	@Override
	public int defenseSkill(Char enemy) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage(DamageInfo info) {
		//invulnerable, it only melts with time
	}

	@Override
	public boolean add(Buff buff) {
		return false;
	}

	@Override
	public boolean interact(Char c) {
		return true;
	}

	private static final String LIFESPAN = "lifespan";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LIFESPAN, lifespan);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lifespan = bundle.getInt(LIFESPAN);
	}
}
