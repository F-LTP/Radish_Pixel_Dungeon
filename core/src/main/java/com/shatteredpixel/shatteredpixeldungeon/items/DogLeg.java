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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

/**
 * 狗腿 (Dog Leg)
 * 杀死狗后15%掉落。
 * 投向一个格子后会像点燃的炸弹一样留下一个可见的狗腿（无法捡回），
 * 并持续吸引附近(5格)的敌对怪物走向它。第一个触及它的怪物会使其消失，
 * 同时那只怪物会失去当前追击目标（同净化飞镖）。
 */
public class DogLeg extends Item {

	public static final float LURE_RANGE = 5f;

	{
		defaultAction = AC_THROW;
		usesTargeting = true;

		image = ItemSpriteSheet.DOG_LEG;
		stackable = false;

		bones = true;
	}

	//非空表示这是一根已经被投掷到地面、正在生效的诱饵狗腿（不可捡回）
	public Lure lure;

	@Override
	protected void onThrow(int cell) {
		if (Dungeon.level.pit[cell]) {
			//掉进深渊则消失
			super.onThrow(cell);
			return;
		}

		//像点燃的炸弹一样：留一根可见的狗腿在地上，并放置一个持续生效的诱饵 actor
		Heap heap = Dungeon.level.drop(this, cell);
		if (!heap.isEmpty()) {
			heap.sprite.drop(cell);
		}
		lure = new Lure().ignite(this);
		Actor.add(lure);
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		//投掷后的狗腿无法捡回
		if (lure != null) {
			return false;
		}
		return super.doPickUp(hero, pos);
	}

	/**
	 * 诱饵实体：每 turn 吸引附近敌对怪物走向狗腿；一旦有怪物踏上目标格就销毁并净化该怪。
	 */
	public static class Lure extends Actor {

		{ actPriority = BLOB_PRIO + 1; }

		protected DogLeg owner;

		public Lure ignite(DogLeg owner) {
			this.owner = owner;
			return this;
		}

		private int pos() {
			//从 heaps 中找到自己所属的狗腿的位置
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				if (heap.items.contains(owner)) {
					return heap.pos;
				}
			}
			return -1;
		}

		@Override
		protected boolean act() {

			//狗腿已被移除/销毁，清理自己
			if (owner == null || owner.lure != this) {
				Actor.remove(this);
				return true;
			}

			int cell = pos();
			if (cell == -1) {
				//找不到所属狗腿，清理自己
				owner.lure = null;
				Actor.remove(this);
				return true;
			}

			//持续吸引 5 格内的敌对怪物；每个怪物只走向离它最近的诱饵
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (mob.alignment == Char.Alignment.ENEMY
						&& mob.isAlive()
						&& mob.nearestLure() == cell
						&& Dungeon.level.distance(mob.pos, cell) <= LURE_RANGE) {
					mob.beckon(cell);
				}
			}

			//第一个触及目标格的怪物使其消失
			Char trigger = Actor.findChar(cell);
			if (trigger instanceof Mob && ((Mob) trigger).alignment == Char.Alignment.ENEMY && trigger.isAlive()) {
				Sample.INSTANCE.play(Assets.Sounds.HIT);
				Splash.at(cell, 0xFF8C3B, 3);

				Heap heap = Dungeon.level.heaps.get(cell);
				if (heap != null) {
					//只移除这根狗腿，不连带销毁同格的其他物品
					heap.remove(owner);
				}
				owner.lure = null;
				Actor.remove(this);

				//触及的怪物失去当前追击目标（同净化飞镖，延迟一帧以免打断移动）
				cleanse((Mob) trigger);
				return true;
			}

			spend(TICK);
			return true;
		}

		private static void cleanse(final Mob mob) {
			new FlavourBuff() {
				{ actPriority = VFX_PRIO; }

				public boolean act() {
					if (mob.state == mob.HUNTING || mob.state == mob.FLEEING) {
						mob.state = mob.WANDERING;
					}
					mob.clearEnemy();
					mob.beckon(Dungeon.level.randomDestination(mob));
					mob.sprite.showLost();
					return super.act();
				}
			}.attachTo(mob);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("lure", lure);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains("lure")) {
			lure = (Lure) bundle.get("lure");
			//若这是一根地面上的诱饵狗腿，重新把诱饵 actor 挂回时间轴
			if (lure != null) {
				lure.owner = this;
				Actor.add(lure);
			}
		}
	}

	@Override
	public boolean isSimilar(Item item) {
		return super.isSimilar(item) && this.lure == ((DogLeg) item).lure;
	}

	@Override
	public boolean isUpgradable() {
		return false;
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
