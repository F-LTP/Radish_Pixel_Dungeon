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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

/**
 * 手持重炮 (Heavy Cannon)
 * 四阶，力量需求16，初始4~20，成长1~5。
 * 可以选择用此武器发射各种炸弹，造成1.5+武器等级*0.2倍伤害的同时会使炸弹立刻爆炸。
 * 矮人炮手的武器。杀死矮人炮手有10%概率掉落。只有掉落或嬗变而来2种获取条件。
 */
public class HeavyCannon extends MeleeWeapon {

	public static final String AC_FIRE = "FIRE";

	{
		image = ItemSpriteSheet.HEAVY_CANNON;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 0.8f;

		tier = 4;
	}

	// 暂存待发射的炸弹
	private Bomb pendingBomb;

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_FIRE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_FIRE)) {
			curUser = hero;
			curItem = this;

			// 选择要发射的炸弹
			GameScene.selectItem(bombSelector);
		}
	}

	private final WndBag.ItemSelector bombSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(HeavyCannon.class, "prompt");
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return MagicalHolster.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Bomb;
		}

		@Override
		public void onSelect(Item item) {
			if (item == null) return;
			pendingBomb = (Bomb) item;
			GameScene.selectCell(launcher);
		}
	};

	private final CellSelector.Listener launcher = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell == null || curUser == null || curItem == null || pendingBomb == null) return;

			Bomb bomb = pendingBomb;
			pendingBomb = null;

			// 消耗炸弹
			bomb.detach(curUser.belongings.backpack);

			curUser.spendAndNext(1f);
			curUser.sprite.zap(cell, new com.watabou.utils.Callback() {
				@Override
				public void call() {
					// 1.5 + 武器等级*0.2 倍爆炸伤害
					float mult = 1.5f + buffedLvl() * 0.2f;
					int baseDmg = Char.combatRoll(5 + Dungeon.scalingDepth(), 10 + Dungeon.scalingDepth()*2);
					int dmg = Math.round(baseDmg * mult);

					Sample.INSTANCE.play(Assets.Sounds.BLAST);
					// 对目标格周围的敌人造成加成爆炸伤害（炸弹立刻爆炸）
					bomb.explodeMobs(cell, dmg);
				}
			});
		}

		@Override
		public String prompt() {
			return Messages.get(HeavyCannon.class, "target_prompt");
		}
	};

	@Override
	public int STRReq(int lvl) {
		return 9 + tier * 2; // 4阶=16
	}

	@Override
	public int min(int lvl) {
		return 4 + lvl;
	}

	@Override
	public int max(int lvl) {
		return 20 + lvl * 5;
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}
}

