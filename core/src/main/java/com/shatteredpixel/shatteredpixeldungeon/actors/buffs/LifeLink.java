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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.HashSet;

public class LifeLink extends FlavourBuff {

	public int object = 0;

	private static final String OBJECT    = "object";

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	/**
	 * 伤害分摊：把 victim 本次受到的伤害按链接数均摊，自身留下向上取整的那一份，其余各个链接目标
	 * 按同一基数走各自的承伤管线（因此各自抗性/护盾独立结算）。
	 * 返回 victim 应继续结算的伤害；已失效的链接会顺带清理。
	 */
	public static int shareDamage(Char victim, int dmg, DamageType type) {
		HashSet<LifeLink> links = victim.buffs(LifeLink.class);
		for (LifeLink link : links.toArray(new LifeLink[0])) {
			if (Actor.findById(link.object) == null) {
				links.remove(link);
				link.detach();
			}
		}

		int shared = (int) Math.ceil(dmg / (float) (links.size() + 1));
		for (LifeLink link : links) {
			Char ch = (Char) Actor.findById(link.object);
			if (ch != null) {
				ch.damage(DamageInfo.of(shared, type, victim, link));
				if (!ch.isAlive()) {
					link.detach();
				}
			}
		}
		return shared;
	}

	@Override
	public void detach() {
		super.detach();
		Char ch = (Char)Actor.findById(object);
		if (!target.isActive() && ch != null){
			for (LifeLink l : ch.buffs(LifeLink.class)){
				if (l.object == target.id()){
					l.detach();
				}
			}
		}
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( OBJECT, object );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		object = bundle.getInt( OBJECT );
	}

	@Override
	public String icon() {
		return BuffIndicator.HERB_HEALING;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1, 0, 1);
	}

}
