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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

/**
 * 勾爪 (Grappling Hook)
 * 三阶，力量需求13，初始3~16，成长1~3。
 * 你可以点击视野内的一处可抓握地块并与其直线距离内没有阻挡，然后位移至此处（如同虚空锁链）。
 * 但是此过程需要消耗直线距离+1-武器等级*1/3回合。（最低为1）
 * 正常武器正常生成。
 */
public class GrapplingHook extends MeleeWeapon {

	public static final String AC_GRAPPLE = "GRAPPLE";

	{
		image = ItemSpriteSheet.GRAPPLING_HOOK;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 0.9f;

		tier = 3;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_GRAPPLE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_GRAPPLE)) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell(grappler);
		}
	}

	private final CellSelector.Listener grappler = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null || curUser == null) return;

			// 视野外不可用
			if (!Dungeon.level.heroFOV[target]) {
				GLog.w(Messages.get(GrapplingHook.class, "out_of_view"));
				return;
			}

			// 与直线距离内没有阻挡
			Ballistica line = new Ballistica(curUser.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
			if (line.collisionPos != target || Dungeon.level.distance(curUser.pos, target) == 0) {
				GLog.w(Messages.get(GrapplingHook.class, "blocked"));
				return;
			}

			// 目标必须是可抓握地块（可站立或可躲避），且不是固体
			if (Dungeon.level.solid[target] || !(Dungeon.level.passable[target] || Dungeon.level.avoid[target])) {
				GLog.w(Messages.get(GrapplingHook.class, "not_grabable"));
				return;
			}

			// 目标处不能有别的角色
			if (Actor.findChar(target) != null) {
				GLog.w(Messages.get(GrapplingHook.class, "occupied"));
				return;
			}

			int dist = Dungeon.level.distance(curUser.pos, target);

			// 消耗回合：直线距离+1-武器等级*1/3，最低1
			float cost = Math.max(1f, dist + 1 - buffedLvl() / 3f);

			final Hero hero = curUser;
			hero.busy();
			Sample.INSTANCE.play(Assets.Sounds.CHAINS);
			hero.sprite.parent.add(new Chains(hero.sprite.center(),
					DungeonTilemap.raisedTileCenterToWorld(target),
					Effects.Type.ETHEREAL_CHAIN,
					new Callback() {
						public void call() {
							Actor.add(new Pushing(hero, hero.pos, target, new Callback() {
								public void call() {
									hero.pos = target;
									Dungeon.level.occupyCell(hero);
									hero.spendAndNext(cost);
								}
							}));
						}
					}));
		}

		@Override
		public String prompt() {
			return Messages.get(GrapplingHook.class, "prompt");
		}
	};

	@Override
	public int STRReq(int lvl) {
		return 9 + tier * 2; // 3阶=13
	}

	@Override
	public int min(int lvl) {
		return 3 + lvl;
	}

	@Override
	public int max(int lvl) {
		return 16 + lvl * 3;
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}
}
