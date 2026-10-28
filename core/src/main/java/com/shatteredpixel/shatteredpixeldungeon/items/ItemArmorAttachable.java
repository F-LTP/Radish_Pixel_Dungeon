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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeToyEffects;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 可附着到护甲的玩具基类。
 * 类似于 BrokenSeal，玩具作为 Item 存在，附着到护甲后在战斗逻辑中被检测并应用效果。
 * 子类需要实现 applyEffect(Hero) 和 removeEffect(Hero) 方法。
 */
public abstract class ItemArmorAttachable extends Item {

	public static final String AC_ATTACH = "ATTACH";
	public static final String AC_DETACH = "DETACH";

	{
		stackable = false;
		levelKnown = false;
		image = ItemSpriteSheet.SNAKE_BITE; // 默认贴图
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_ATTACH);

		// 如果已附着在护甲上，提供取下选项
		if (attachedTo != null) {
			actions.add(AC_DETACH);
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_ATTACH) || action.equals(AC_DETACH)) {
			return Messages.get(ItemArmorAttachable.class, "ac_" + action);
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_ATTACH)) {
			curItem = this;
			GameScene.selectItem(armorSelector);
		} else if (action.equals(AC_DETACH)) {
			detachFromArmor(hero);
		}
	}

	@Override
	public void doDrop(Hero hero) {
		if (this instanceof BrokenSeal) {
			super.doDrop(hero);
			return;
		}
		detachAll(hero.belongings.backpack);
		hero.spendAndNext(TIME_TO_DROP);
		vanishOnGround(false, hero.pos);
	}

	@Override
	protected void onThrow(int cell) {
		if (this instanceof BrokenSeal) {
			super.onThrow(cell);
		} else {
			vanishOnGround(false, cell);
		}
	}

	public void vanishOnGround(boolean capacityOverflow, int cell) {
		if (this instanceof BrokenSeal) return;
		if (Dungeon.hero != null && Dungeon.hero.sprite != null && Dungeon.hero.sprite.parent != null) {
			new Flare(6, 32).show(Dungeon.hero.sprite.parent,
					DungeonTilemap.raisedTileCenterToWorld(cell), 2f);
		}
		if (capacityOverflow) GLog.w(Messages.get(ItemArmorAttachable.class, "cannot_hang", name()));
		GLog.w(Messages.get(ItemArmorAttachable.class, "vanished"));
		quantity(0);
		updateQuickslot();
	}

	/**
	 * 将玩具附着到护甲上。由 Armor.attachToy() 调用。
	 */
	public void attachToArmor(Armor armor) {
		this.attachedTo = armor;
	}

	/**
	 * 卸下该玩具时损失的最大/当前生命值。默认 0，子类可覆写。
	 */
	public int detachHPLoss(Hero hero) {
		return 0;
	}

	/**
	 * 当前是否允许卸下该玩具。若卸下会导致死亡则返回 false。
	 */
	public boolean canDetach(Hero hero) {
		if (hero == null || detachHPLoss(hero) <= 0) return true;
		return hero.HP > detachHPLoss(hero);
	}

	/**
	 * 尝试卸下该玩具。若卸下会导致死亡则拒绝并返回 false；否则先扣除 detachHPLoss 点当前生命值，再返回 true。
	 */
	public boolean tryDetach(Hero hero) {
		if (!canDetach(hero)) {
			GLog.w(Messages.get(this, "cannot_detach", name(), detachHPLoss(hero)));
			return false;
		}
		if (hero != null && detachHPLoss(hero) > 0) {
			hero.HP -= detachHPLoss(hero);
		}
		return true;
	}

	/**
	 * 从护甲取下玩具。放回背包。
	 */
	public void detachFromArmor(Hero hero) {
		if (attachedTo != null) {
			if (!tryDetach(hero)) return;
			Armor armor = attachedTo;
			if (this instanceof BrokenSeal) {
				// 破损纹章卸下时需要返还升级并（如果有天赋）携带附魔
				armor.detachSeal(hero);
			} else {
				int index = armor.getToys().indexOf(this);
				armor.detachToy(index);
			}
			armor.dropExcessToysAfterCapacityChange(hero);
			if (hero != null && !hero.belongings.backpack.contains(this)) {
				collect(hero.belongings.backpack);
				GLog.i(Messages.get(this, "detached", name()));
			}
		}
	}

	/**
	 * 子类实现：应用玩具效果到英雄
	 */
	public abstract void applyEffect(Hero hero);

	/**
	 * 子类实现：移除玩具效果
	 */
	public abstract void removeEffect(Hero hero);

	// attachedTo 引用当前附着到的护甲（运行时）
	public transient Armor attachedTo;

	public int tier() {
		return 1;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		// 注意：attachedTo 是 transient，不序列化
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		attachedTo = null; // 恢复时需要重新绑定
	}

	@Override
	public String info() {
		String info = desc();
		if (attachedTo != null) {
			info += "\n\n" + Messages.get(this, "attached_info");
		}
		return info;
	}

	// 护甲选择器
	protected static WndBag.ItemSelector armorSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(BrokenSeal.class, "prompt"); // 复用破损纹章的提示
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Armor;
		}

		@Override
		public void onSelect(Item item) {
			ItemArmorAttachable toy = (ItemArmorAttachable) curItem;
			if (item instanceof Armor) {
				Armor armor = (Armor) item;
				armor.requestAttachToy(Dungeon.hero, toy);
				Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.UNLOCK);
			}
		}
	};

	// ========== 便捷方法 ==========

	/**
	 * 获取当前英雄护甲上附着的指定类型玩具
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ItemArmorAttachable> T getAttachedToy(Class<T> toyClass) {
		if (Dungeon.hero == null) return null;

		if (Dungeon.hero.belongings.armor != null) {
			T attached = Dungeon.hero.belongings.armor.getToy(toyClass);
			if (attached != null) return attached;
		}

		ChallengeToyEffects challengeEffects = Dungeon.hero.buff(ChallengeToyEffects.class);
		return challengeEffects == null ? null : challengeEffects.getToy(toyClass);
	}

	/**
	 * 检查英雄护甲上是否附着了指定类型的玩具
	 */
	public static boolean hasAttached(Class<? extends ItemArmorAttachable> toyClass) {
		return getAttachedToy(toyClass) != null;
	}

	/**
	 * 获取英雄护甲上所有附着的玩具列表
	 */
	public static ArrayList<ItemArmorAttachable> getAllAttachedToys() {
		ArrayList<ItemArmorAttachable> result = new ArrayList<>();
		if (Dungeon.hero == null) return result;

		if (Dungeon.hero.belongings.armor != null) {
			result.addAll(Dungeon.hero.belongings.armor.getToys());
		}
		ChallengeToyEffects challengeEffects = Dungeon.hero.buff(ChallengeToyEffects.class);
		if (challengeEffects != null) result.addAll(challengeEffects.effects());
		return result;
	}
}
