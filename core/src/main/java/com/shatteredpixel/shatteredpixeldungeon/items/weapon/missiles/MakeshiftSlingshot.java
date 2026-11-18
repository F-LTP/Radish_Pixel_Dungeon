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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 简易投石索 (Makeshift Slingshot)
 * 哥布林10%概率掉落
 * 使用时消耗一颗石头，伤害提升4倍，石头以更快的速度发射，投石索和石头一并摧毁
 */
public class MakeshiftSlingshot extends Item {

	public static final String AC_USE = "USE";

	{
		defaultAction = AC_USE;
		usesTargeting = true;

		image = ItemSpriteSheet.MAKESHIFT_SLINGSHOT;
		stackable = true;
		levelKnown = true;
		cursedKnown = true;

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
			
			// 检查背包中是否有石头
			ThrowingStone stone = hero.belongings.getItem(ThrowingStone.class);
			if (stone == null) {
				GLog.w(Messages.get(this, "no_stone"));
				return;
			}

			// 选择目标进行投掷
			GameScene.selectCell(thrower);
		}
	}

	// 目标选择器
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null || curUser == null || curItem == null) return;
			
			// 获取石头
			ThrowingStone stone = curUser.belongings.getItem(ThrowingStone.class);
			if (stone == null) {
				GLog.w(Messages.get(MakeshiftSlingshot.class, "no_stone"));
				return;
			}

			// 和普通投掷一样沿弹道飞到第一个挡住它的格子。直接拿玩家点的那一格当落点的话，
			// 隔着墙、隔着别的怪都能打中
			final int cell = stone.throwPos(curUser, target);
			// 石头飞行期间英雄不能再行动，回合在落地回调里结算
			curUser.busy();

			// 消耗一颗石头和一个投石索
			stone.detach(curUser.belongings.backpack);
			curItem.detach(curUser.belongings.backpack);

			// 石头飞行期间英雄不可行动；回合在 onThrowReached 里结束
			curUser.busy();

			Sample.INSTANCE.play(Assets.Sounds.HIT);
			MissileSprite missile = (MissileSprite) curUser.sprite.parent.recycle(MissileSprite.class);
			// The consumed stone supplies the projectile sprite.
			// 结束回合放在回调里，保证动画走完一定解除忙碌状态
			missile.reset(curUser.sprite, cell, stone, () -> onThrowReached(cell), 2f);
		}

		@Override
		public String prompt() {
			return Messages.get(MakeshiftSlingshot.class, "prompt");
		}
	};

	protected static void onThrowReached(int cell) {
		final Hero user = curUser;
		if (user == null) return;
		
		try {
			Char enemy = Actor.findChar(cell);

			if (enemy != null && enemy != user) {
				// 走标准射击流程，命中率/强化/附魔/暴击都会正常计算
				ThrowingStone shot = new ThrowingStone();
				// 4倍伤害通过层数体现，保持与说明一致
				shot.level(Math.max(0, shot.level() + 4));
				user.shoot(enemy, shot);
				// 检查击杀
				if (!enemy.isAlive()) {
					if (enemy == Dungeon.hero) {
						Dungeon.fail(MakeshiftSlingshot.class);
						GLog.n(Messages.get(MakeshiftSlingshot.class, "kill_desc"));
					}
				}
			}
		} finally {
			// 无论如何都要结束回合，否则动画/异常会让英雄卡在忙碌状态
			user.spendAndNext(1f);
		}
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
		return 25;
	}
}
