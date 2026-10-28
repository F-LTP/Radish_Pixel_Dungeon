/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: You can redistribute it and/or modify
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

/**
 * 注定一抽护甲技能
 * 月华在绝境之下请神，获得了操纵随机数的力量。
 * 月华接下来的数次关于伤害、防御、闪避、命中的随机数判定始终取最大值。
 * 充能消耗：30
 */
public class FatedDraw extends ArmorAbility {

	{
		baseChargeUse = 30f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		FatedDrawTracker tracker = Buff.affect(hero, FatedDrawTracker.class);
		tracker.activate(hero);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();

		GLog.p(Messages.get(this, "activated"));
	}

	@Override
	public String icon() {
		return HeroIcon.FATED_DRAW;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.FATED_TWICE, Talent.LOOT_GROUND, Talent.TIME_PAUSE, Talent.HEROIC_ENERGY};
	}

	/**
	 * 注定一抽追踪器 Buff
	 * remainingChecks 为唯一资源，每次消耗都取最大值
	 */
	public static class FatedDrawTracker extends Buff {

		public int remainingChecks = 0;

		@Override
		public String icon() {
			return BuffIndicator.BLESS;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.9f, 0.7f, 0.2f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", remainingChecks);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(remainingChecks);
		}

		@Override
		public boolean act() {
			if (remainingChecks <= 0) {
				detach();
				GLog.w(Messages.get(this, "expired"));
			}
			spend(TICK);
			return true;
		}

		public void activate(Hero hero) {
			remainingChecks = 4 + hero.pointsInTalent(Talent.FATED_TWICE);
		}

		/**
		 * 消耗一次掷骰次数并取最大值
		 * @param type 消耗类型：attack_hit, attack_damage, defense_evasion, defense_block
		 * @return 是否成功消耗（有剩余次数时返回 true）
		 */
		public boolean consume(String type) {
			if (remainingChecks > 0) {
				remainingChecks--;
				//TheCatist 2026/8/5 这个太吵了！不要
//				GLog.i(Messages.get(this, "consume_" + type, remainingChecks));
				return true;
			}
			return false;
		}

		public Item tryGenLootGroundDrop(Hero hero) {
			if (remainingChecks > 0 && hero.hasTalent(Talent.LOOT_GROUND)) {
				remainingChecks--;
				int ringLevel = hero.pointsInTalent(Talent.LOOT_GROUND) * 2;
				GLog.p(Messages.get(this, "loot_drop", ringLevel));
				return RingOfWealth.genConsumableDrop(ringLevel - 1);
			}
			return null;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("remainingChecks", remainingChecks);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			remainingChecks = bundle.getInt("remainingChecks");
		}
	}
}