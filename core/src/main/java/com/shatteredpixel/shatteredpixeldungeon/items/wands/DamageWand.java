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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
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

	// 骰子法师 4-2 鸡蛋篮子：最小伤害变为 1，最大伤害增加「最小值降低量 × 125% / 150% / 175% / 200%」
	private static float eggBasketFactor(){
		if (!(curUser instanceof Hero)) return 0f;
		Hero caster = (Hero) curUser;
		if (caster.subClass != HeroSubClasses.DICE_MAGE) return 0f;
		int points = caster.pointsInTalent(Talent.EGG_BASKET);
		return points > 0 ? 1f + 0.25f * points : 0f;
	}

	public int rollMin(int lvl){
		int min = min(lvl);
		return eggBasketFactor() > 0 && min > 1 ? 1 : min;
	}

	public int rollMax(int lvl){
		int min = min(lvl), max = max(lvl);
		float factor = eggBasketFactor();
		return factor > 0 && min > 1 ? max + Math.round((min - 1) * factor) : max;
	}

	public int damageRoll(){
		return damageRoll(buffedLvl());
	}

	@SuppressWarnings("DefaultLocale")
    public int damageRoll(int lvl){
		int dmg = Char.combatRoll(rollMin(lvl), rollMax(lvl));

		Hero caster = curUser instanceof Hero ? (Hero) curUser : null;
		if (caster == null) {
			return dmg;
		}

		if(caster.hasTalent(Talent.FANATICISM_MAGIC)){
			if (dmg > 0){
				Berserk berserk = Buff.affect(caster, Berserk.class);
				berserk.damage(dmg/2);
			}
		}

		RiverCrystal riverGlass = caster.belongings.getItem(RiverCrystal.class);
		if(riverGlass != null){
			dmg = Integer.MAX_VALUE;
			for (int i = 0; i < riverGlass.judgeTimes(); i++){
				dmg = Math.min(dmg, Char.combatRoll(rollMin(lvl), rollMax(lvl)));
			}
		}


		LightKing lightKing = caster.belongings.getItem(LightKing.class);
		if (lightKing != null) {
			int lkLvl = lightKing.buffedLvl();
			float[] thresholds = {0.9f, 0.85f, 0.8f, 0.75f};
			float[] damageModifiers = {1.25f, 1.33f, 1.41f, 1.50f};

			float hpPercentage = (float) caster.HP / caster.HT;
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

		WandEmpower emp = caster.buff(WandEmpower.class);
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
			return Messages.get(this, "stats_desc", rollMin(buffedLvl()), rollMax(buffedLvl()));
		else
			return Messages.get(this, "stats_desc", rollMin(0), rollMax(0));
	}

	@Override
	public String upgradeStat1(int level) {
		return rollMin(level) + "-" + rollMax(level);
	}
}
