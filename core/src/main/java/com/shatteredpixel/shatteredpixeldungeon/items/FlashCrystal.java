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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
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
			//等级和诅咒都要是玩家已经知道的：按真实值变灰的话，打开这个窗口（不花回合、不耗闪晶）
			//就能看出哪件未鉴定装备被诅咒或已经 +3 以上
			//还要是能被升级的装备：灵能短弓、神器这类 isUpgradable() 为 false 的物品显示的等级不是 trueLevel()，
			//灵能短弓的 trueLevel() 恒为 0，掷到「下降」那 33% 就直接被摧毁，哪怕它显示的是 +2
			return item instanceof EquipableItem
					&& item.isUpgradable()
					&& item.levelKnown && item.cursedKnown
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
				// 成叠的投掷武器 upgrade() 只分出一支去升级并返回它，提示要用返回的那一支
				Item upgraded = item.upgrade();
				Item.updateQuickslot();
				GLog.p(Messages.get(FlashCrystal.class, "up", upgraded.name(), upgraded.level()));
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
				//护甲上贴着的破损纹章先拆下来还给玩家：它是战士唯一的职业物品，不能跟着护甲一起消失。
				//必须在 doUnequip 之前拆，那之后护甲连同附着物就被丢弃了
				if (item instanceof Armor && ((Armor) item).checkSeal() != null) {
					Armor armor = (Armor) item;
					BrokenSeal seal = armor.checkSeal();
					armor.detachSeal(curUser);
					if (!seal.collect()) {
						Dungeon.level.drop(seal, curUser.pos).sprite.drop();
					}
				}
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
