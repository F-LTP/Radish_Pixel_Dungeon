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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Blueprint extends Scroll {

	{
		image = ItemSpriteSheet.BLUEPRINT;
		talentChance = 0;
	}

	private static final int CRAFT_COST = 15;

	@Override
	public void doRead() {
		LiquidMetal metal = curUser.belongings.getItem(LiquidMetal.class);
		if (metal != null && metal.quantity() >= CRAFT_COST){
			DM100 dm100 = new DM100();

			ArrayList<Integer> spawnPos = new ArrayList<>();

			for (int i : PathFinder.NEIGHBOURS8) {
				if (Actor.findChar(i + curUser.pos) == null
						&& (Dungeon.level.passable[i + curUser.pos]
						|| (Dungeon.level.avoid[i + curUser.pos] && !Dungeon.level.pit[i + curUser.pos])))
					spawnPos.add(i + curUser.pos);
			}

			if (spawnPos.isEmpty()){
				GLog.w(Messages.get(this, "crowded"));
				return;
			}

			Buff.affect(dm100, ScrollOfSirensSong.Enthralled.class);
			dm100.pos = Random.element(spawnPos);
			GameScene.add(dm100);
			Dungeon.level.occupyCell(dm100);
			dm100.sprite.centerEmitter().burst( SparkParticle.FACTORY, 5 );

			if (metal.quantity() <= CRAFT_COST) metal.detachAll(curUser.belongings.backpack);
			else metal.quantity(metal.quantity() - CRAFT_COST);

			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
			detach(curUser.belongings.backpack);
			Invisibility.dispel();
			curUser.spendAndNext( TIME_TO_READ*2 );
			// 只播挥手动画：不带回调的 attack() 播完会进 Hero.onAttackComplete()，
			// 对上一次攻击过的目标再结算一次真实攻击并多 spend 一次攻击耗时
			final com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero user = curUser;
			user.sprite.attack(dm100.pos, () -> user.sprite.idle());
			Catalog.countUse(getClass());

		} else GLog.w(Messages.get(this, "no_metal"));
	}

	@Override
	public boolean isKnown() {
		return true;
	}
}