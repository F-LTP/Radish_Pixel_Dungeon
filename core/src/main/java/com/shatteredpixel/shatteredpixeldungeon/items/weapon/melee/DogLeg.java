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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

/**
 * 狗腿 (Dog Leg)
 * 杀死狗后15%掉落一个。
 * 可以对1距离的敌人使用，视为一次伤害为16且必中的近战攻击。使用后消失。
 */
public class DogLeg extends Item {

	public static final String AC_USE = "USE";

	{
		defaultAction = AC_USE;
		usesTargeting = true;

		image = ItemSpriteSheet.DOG_LEG;
		stackable = false;

		bones = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_USE)) {
			curUser = hero;
			curItem = this;

			// 只能对 1 距离的敌人使用
			GameScene.selectCell(attacker);
		}
	}

	protected static CellSelector.Listener attacker = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell == null || curUser == null || curItem == null) return;

			if (cell < 0 || cell >= Dungeon.level.length() || Dungeon.level.solid[cell]) {
				GLog.w(Messages.get(DogLeg.class, "too_far"));
				return;
			}

			// 消耗狗腿
			curItem.detach(curUser.belongings.backpack);
			Heap lure = Dungeon.level.drop(new DogLeg(), cell);
			LureBuff.start(cell, lure);
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (Dungeon.level.distance(mob.pos, cell) <= 5) {
					mob.beckon(cell);
					Buff.affect(mob, LureBuff.class);
				}
			}
			curUser.spendAndNext(1f);
			Sample.INSTANCE.play(Assets.Sounds.ALERT);
		}

		@Override
		public String prompt() {
			return Messages.get(DogLeg.class, "prompt");
		}
	};

	public static class LureBuff extends Buff {
		private static int lurePos = -1;
		private static Heap lure;

		public static void start(int pos, Heap heap) {
			lurePos = pos;
			lure = heap;
		}

		public static boolean isActive(int pos) {
			return lurePos == pos && lure != null;
		}

		@Override
		public boolean act() {
			if (target.pos == lurePos) {
				if (lure != null) lure.destroy();
				for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
					LureBuff buff = mob.buff(LureBuff.class);
					if (buff != null) buff.detach();
					mob.notice();
				}
				lure = null;
				lurePos = -1;
			} else {
				spend(1f);
			}
			return true;
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if (LureBuff.isActive(pos)) return false;
		return super.doPickUp(hero, pos);
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	@Override
	public int value() {
		return 30;
	}
}
