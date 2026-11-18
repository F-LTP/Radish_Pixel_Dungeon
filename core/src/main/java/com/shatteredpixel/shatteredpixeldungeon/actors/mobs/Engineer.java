/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BrokenArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Blueprint;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EngineerSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Engineer extends Mob {//专门恶心叠甲流 :)

	private float prickCD = 0;

	{
		spriteClass = EngineerSprite.class;

		HP = HT = 75;
		defenseSkill = 18;

		EXP = 12;
		maxLvl = 23;

		loot = new Blueprint();
		lootChance = 1/3f;

		//根据情节，他们宁愿自杀也要保全自由意志，所以不带有亡灵属性

	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 20, 25 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 27;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(4, 10);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (prickCD <= 0){
			Buff.affect(enemy, BrokenArmor.class, 5f);
			damage = Math.round(damage * 1.5f);
			spend(0.5f);
			prickCD = Random.NormalFloat(7, 10);
			if (Dungeon.level.heroFOV[pos]) Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
		}

		return damage;
	}

	@Override
	protected void spend( float time ) {
		prickCD -= time;
		super.spend( time );
	}

	private static String PRICK_COOLDOWN = "prick_cooldown";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put(PRICK_COOLDOWN, prickCD );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		prickCD = bundle.getInt(PRICK_COOLDOWN);
	}
}