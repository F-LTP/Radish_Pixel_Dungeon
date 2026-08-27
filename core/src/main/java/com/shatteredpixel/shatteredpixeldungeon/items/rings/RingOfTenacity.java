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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CircleSword;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfTenacity extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_TENACITY;
	}

	public String statsInfo() {
		if (isIdentified()){
			if (wieldingCircleSword()){
				int effLevel = 2*soloBuffedBonus() + 1;
				return Messages.get(this, "stats_sword", new DecimalFormat("#.##").format(100f * (Math.pow(1.05f, effLevel) - 1f)));
			}
			if (!cursed)
				return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.05f, soloBuffedBonus()) - 1f)),new DecimalFormat("#.##").format(100f*(1.1f-Math.max(0.11f,Math.pow(0.944, soloBuffedBonus()-1f)))));
			else
				return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.05f, soloBuffedBonus()) - 1f)),new DecimalFormat("#.##").format(100f*(1f-Math.pow(0.944, soloBuffedBonus()))));
		} else {
			return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(5f),new DecimalFormat("#.##").format(10f));
		}
	}

	private boolean wieldingCircleSword(){
		return Dungeon.hero != null && Dungeon.hero.belongings.weapon() instanceof CircleSword;
	}

	private static boolean wieldingCircleSword( Char t ){
		return t != null && t.attackingWeapon() instanceof CircleSword;
	}

	@Override
	protected RingBuff buff( ) {
		return new Tenacity();
	}


	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Messages.decimalFormat("#.##", 100f * (1f - Math.pow(0.85f, level+1))) + "%";
	}

	public static float damageMultiplier( Char t ){
		if (wieldingCircleSword(t)) return 1f;
		int gbb=getBuffedBonus( t, Tenacity.class);
		if (gbb>0)
			return Math.max(0.01f,(float)Math.pow(0.944,gbb-1)-0.1f);
		else
			return (float)(Math.pow(0.944,gbb));
	}
	public static float attackMultiplier( Char t ){
		int gbb=getBuffedBonus( t, Tenacity.class);
		if (wieldingCircleSword(t)) {
			gbb = 2*gbb + 1;
		}
		return (float)Math.pow(1.05,gbb);
	}

	public class Tenacity extends RingBuff {

		@Override
		public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
			info.addDirectMultModifier(attackMultiplier(attacker), "tenacity", this);
		}
	}
}
