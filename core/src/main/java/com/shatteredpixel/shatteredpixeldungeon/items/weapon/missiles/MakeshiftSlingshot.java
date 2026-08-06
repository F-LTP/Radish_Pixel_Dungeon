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
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 简易投石索 (Makeshift Slingshot)
 * 哥布林10%概率掉落
 * 使用时消耗一颗石头，伤害提升4倍，投石索和石头一并摧毁
 */
public class MakeshiftSlingshot extends Item {

	public static final String AC_USE = "USE";

	{
		defaultAction = AC_USE;
		usesTargeting = true;

		// 暂时使用石子贴图，后续需要添加专用贴图
		image = ItemSpriteSheet.THROWING_STONE;
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
		public void onSelect(Integer cell) {
			if (cell == null || curUser == null || curItem == null) return;
			
			// 获取石头
			ThrowingStone stone = curUser.belongings.getItem(ThrowingStone.class);
			if (stone == null) {
				GLog.w(Messages.get(MakeshiftSlingshot.class, "no_stone"));
				return;
			}

			// 消耗石头和投石索
			stone.detach(curUser.belongings.backpack);
			curItem.detach(curUser.belongings.backpack);

			curUser.spendAndNext(1f);
			
			// 播放投掷音效
			Sample.INSTANCE.play(Assets.Sounds.HIT);
			
			// 执行投掷动画
			curUser.sprite.zap(cell, new Callback() {
				@Override
				public void call() {
					// 投掷到达目标
					onThrowReached(cell);
				}
			});
		}

		@Override
		public String prompt() {
			return Messages.get(MakeshiftSlingshot.class, "prompt");
		}
	};

	protected static void onThrowReached(int cell) {
		if (curUser == null) return;
		
		Char enemy = Actor.findChar(cell);
		
		// 计算基础伤害（石头的伤害）
		ThrowingStone stone = new ThrowingStone();
		int baseDamage = stone.damageRoll(curUser);
		
		// 4倍伤害
		int slingshotDamage = baseDamage * 4;

		if (enemy != null && enemy != curUser) {
			// 对敌人造成伤害
			enemy.damage(slingshotDamage, curUser);
			enemy.sprite.showStatus(CharSprite.NEGATIVE, 
				Messages.get(MakeshiftSlingshot.class, "damage_bonus", 4));
			
			// 检查击杀
			if (!enemy.isAlive()) {
				if (enemy == Dungeon.hero) {
					Dungeon.fail(MakeshiftSlingshot.class);
					GLog.n(Messages.get(MakeshiftSlingshot.class, "kill_desc"));
				}
			}
		} else {
			// 没有命中敌人，在地面上显示消息
			GLog.i(Messages.get(MakeshiftSlingshot.class, "use"));
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
