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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ScorpionCrossbow;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Trident extends MissileWeapon {
	
	{
		image = ItemSpriteSheet.TRIDENT;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;
		
		tier = 5;
	}
	private static ScorpionCrossbow bow;
	@Override
	public int max(int lvl){
		int sm=super.max(lvl);
		if (bow !=null){
			sm+=15+bow.buffedLvl()*5;
		}
		return sm;
	}
	@Override
	public int min(int lvl){
		int sm=super.min(lvl);
		if (bow !=null){
			sm+=3+bow.buffedLvl();
		}
		return sm;
	}
	private void updateCrossbow(){
		if (Dungeon.hero.belongings.weapon() instanceof ScorpionCrossbow){
			bow = (ScorpionCrossbow) Dungeon.hero.belongings.weapon();
		} else {
			bow = null;
		}
	}
	@Override
	public int throwPos(Hero user, int dst) {
		updateCrossbow();
		return super.throwPos(user, dst);
	}
	@Override
	protected void onThrow(int cell) {
		updateCrossbow();
		super.onThrow(cell);
	}
	@Override
	public void throwSound() {
		updateCrossbow();
		if (bow != null) {
			Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW, 1, Random.Float(0.87f, 1.15f));
		} else {
			super.throwSound();
		}
	}
	@Override
	public float accuracyFactor(Char owner, Char target){
		return super.accuracyFactor(owner,target)*(bow!=null?1.5f:1f);
	}
	@Override
	public String info() {
		updateCrossbow();
		if (bow != null && !bow.isIdentified()){
			int level = bow.level();
			//temporarily sets the level of the bow to 0 for IDing purposes
			bow.level(0);
			String info = super.info();
			bow.level(level);
			return info;
		} else {
			return super.info();
		}
	}
}
