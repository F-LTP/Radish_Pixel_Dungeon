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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.LightKing;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RiverCrystal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

//for wands that directly damage a target
//wands with AOE or circumstantial direct damage count here (e.g. fireblast, transfusion), but wands with indirect damage do not (e.g. corrosion)
public abstract class DamageWand extends Wand{

	public int min(){
		return min(buffedLvl());
	}

	public abstract int min(int lvl);

	public int max(){
		return max(buffedLvl());
	}

	public abstract int max(int lvl);

	public int damageRoll(){
		return damageRoll(buffedLvl());
	}

	@SuppressWarnings("DefaultLocale")
    public int damageRoll(int lvl){
		int dmg = Char.combatRoll(min(lvl), max(lvl));

		if(hero.hasTalent(Talent.FANATICISM_MAGIC)){
			if (dmg > 0){
				Berserk berserk = Buff.affect(hero, Berserk.class);
				berserk.damage(dmg/2);
			}
		}

		RiverCrystal riverGlass = hero.belongings.getItem(RiverCrystal.class);
		if(riverGlass != null){
			int originalDamage = Char.combatRoll(min(lvl), max(lvl));
			int secondRoll = Char.combatRoll(min(lvl), max(lvl));
			dmg = Math.min(originalDamage, secondRoll);
		}


		LightKing lightKing = hero.belongings.getItem(LightKing.class);
		if (lightKing != null) {
			int lkLvl = lightKing.buffedLvl();
			float[] thresholds = {0.9f, 0.85f, 0.8f, 0.75f};
			float[] damageModifiers = {1.25f, 1.33f, 1.41f, 1.50f};

			float hpPercentage = (float) hero.HP / hero.HT;
			int originalDamage = dmg;

			if (hpPercentage >= thresholds[lkLvl]) {
				float modifiedDamage = dmg * damageModifiers[lkLvl];
				int bonusDamage = Math.round(modifiedDamage - dmg);
				if (bonusDamage < 1) {
					bonusDamage = 1;
				}
				dmg = dmg + bonusDamage;
			} else {
				dmg = Math.round(dmg / damageModifiers[lkLvl]);
			}
		}

		WandEmpower emp = hero.buff(WandEmpower.class);
		if (emp != null){
			dmg += emp.dmgBoost;
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}
		return dmg;
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max());
		else
			return Messages.get(this, "stats_desc", min(0), max(0));
	}

	@Override
	public String upgradeStat1(int level) {
		return min(level) + "-" + max(level);
	}
}
