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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 闪晶 (Flash Crystal)
 * 可以对+3以下的装备直接使用，有33%概率使其上升一级，也有33%概率使其下降一级。0级装备会被摧毁。
 * 豺狼萨满与豺狼狂信徒13%掉落1～2个，双王100%掉落2个。各种矮人21%掉落1～2个。每种怪物最多掉落6个。
 * 可以转换为3点炼金能量。
 */
public class FlashCrystal extends Item {

	public static final String AC_APPLY = "APPLY";

	{
		image = ItemSpriteSheet.FLASH_CRYSTAL;

		stackable = true;

		defaultAction = AC_APPLY;

		bones = true;
	}

	private static final String AC_APPLY_ = AC_APPLY;

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_APPLY);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_APPLY)) {
			curUser = hero;
			GameScene.selectItem(itemSelector);
		}
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
	public int value() {
		return 40 * quantity;
	}

	@Override
	public int energyVal() {
		return 3 * quantity;
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(FlashCrystal.class, "prompt");
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Bag.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof EquipableItem
					&& item.level() < 3
					&& !item.cursed;
		}

		@Override
		public void onSelect(Item item) {
			if (item == null || !(item instanceof EquipableItem)) return;

			detach(curUser.belongings.backpack);

			curUser.sprite.operate(curUser.pos);
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			curUser.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3);

			float roll = Random.Float();
			boolean destroyed = false;

			if (roll < 0.33f) {
				// 33% 上升一级
				item.upgrade();
				Item.updateQuickslot();
				GLog.p(Messages.get(FlashCrystal.class, "up", item.name(), item.level()));
			} else if (roll < 0.66f) {
				// 33% 下降一级
				if (item.trueLevel() == 0) {
					destroyed = true;
				} else {
					item.degrade();
					Item.updateQuickslot();
					GLog.w(Messages.get(FlashCrystal.class, "down", item.name(), item.level()));
				}
			} else {
				// 33% 无事发生
				GLog.i(Messages.get(FlashCrystal.class, "nothing"));
			}

			// 0级装备降级失败时会被直接摧毁
			if (destroyed) {
				GLog.n(Messages.get(FlashCrystal.class, "destroyed", item.name()));
				if (item.isEquipped(curUser)) {
					// Use the normal unequip path so equipment buffs and slot references are cleared.
					if (((EquipableItem) item).doUnequip(curUser, false, false)) {
						curUser.spend(-curUser.cooldown());
					}
				} else {
					item.detachAll(curUser.belongings.backpack);
				}
				Item.updateQuickslot();
			}

			curUser.spendAndNext(1f);
		}
	};

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
}
