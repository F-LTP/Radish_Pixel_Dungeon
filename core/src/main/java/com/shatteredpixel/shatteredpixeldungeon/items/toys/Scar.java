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

package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ScarBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 伤疤 — 提升最大生命值 10 点
 */
public class Scar extends ItemArmorAttachable {

	public static final int MAX_HP_BONUS = 10;

	{
		sndImageName = "scar";
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, ScarBuff.class);
		hero.updateHT(false);
	}

	@Override
	public void removeEffect(Hero hero) {
		ScarBuff buff = hero.buff(ScarBuff.class);
		if (buff != null) buff.detach();
		hero.updateHT(false);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", MAX_HP_BONUS);
	}
}
