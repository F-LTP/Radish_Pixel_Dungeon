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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneClaw;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneSpear;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 残骨堆 (Bone Pile)
 * 使用后有概率翻出骨爪、骨矛、金币、随机符石或律法残页。
 * 杀死骨头33%掉落1个，聚合骷髅100%掉落2～4个。
 */
public class BonePile extends Item {

	public static final String AC_USE = "USE";

	{
		defaultAction = AC_USE;

		image = ItemSpriteSheet.BONE_PILE;
		stackable = true;

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
			use(hero);
		}
	}

	private void use(Hero hero) {
		detach(hero.belongings.backpack);
		hero.spendAndNext(1f);
		Sample.INSTANCE.play(Assets.Sounds.DEGRADE);

		float roll = Random.Float();

		// 12% 骨爪
		if (roll < 0.12f) {
			Item bone = new BoneClaw();
			bone.upgrade(Random.Int(0, 2));
			Dungeon.level.drop(bone, hero.pos).sprite.drop();
			GLog.p(Messages.get(this, "get_claw"));
		}
		// 6% 骨矛
		else if (roll < 0.18f) {
			Item spear = new BoneSpear();
			spear.upgrade(Random.Int(0, 1));
			Dungeon.level.drop(spear, hero.pos).sprite.drop();
			GLog.p(Messages.get(this, "get_spear"));
		}
		// 18% 金币
		else if (roll < 0.36f) {
			int gold = Random.Int(100, 200);
			Dungeon.level.drop(new Gold(gold), hero.pos).sprite.drop();
			GLog.p(Messages.get(this, "get_gold", gold));
		}
		// 12% 随机符石
		else if (roll < 0.48f) {
			Item stone = Generator.randomUsingDefaults(Generator.Category.STONE);
			Dungeon.level.drop(stone, hero.pos).sprite.drop();
			GLog.p(Messages.get(this, "get_stone", stone.name()));
		}
		// 6% 律法残页
		else if (roll < 0.54f) {
			Dungeon.level.drop(new LawFragment(), hero.pos).sprite.drop();
			GLog.p(Messages.get(this, "get_law"));
		}
		// 其余概率：什么都没翻出来
		else {
			GLog.i(Messages.get(this, "nothing"));
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
		return 15 * quantity;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
}
