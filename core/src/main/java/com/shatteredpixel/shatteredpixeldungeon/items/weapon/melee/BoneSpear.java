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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 骨矛 (Bone Spear)
 * 四阶，力量需求16，初始4~20，成长1~5。
 * 消耗充能释放虚拟骨矛，穿透路径上除了墙壁之外的一切事物，造成(100%+武器等级*10%)法术伤害。
 * 初始充能2，每提升3级增加1。充能需要50回合，也享受等同于法杖的其他充能效果。
 * 只能从残骨堆随机刷新出来或嬗变获得。
 */
public class BoneSpear extends MeleeWeapon {

	public static final String AC_SPEAR = "SPEAR";

	{
		image = ItemSpriteSheet.BONE_SPEAR;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.9f;

		tier = 4;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SPEAR);
		return actions;
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		// 技能读的是英雄身上的 Charger，但以前没有任何地方给英雄挂它，技能永远只回「尚未充能完毕」。
		// 装备时（读档恢复装备时也会走这里）挂上；卸下时不摘，免得靠反复穿脱把充能刷满
		if (ch instanceof Hero) {
			Buff.affect(ch, Charger.class);
		}
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_SPEAR)) {
			curUser = hero;
			curItem = this;

			// 充能只在装备着的时候显示和计算上限，技能同样要求装备着
			if (!isEquipped(hero)) {
				GLog.w(Messages.get(this, "not_equipped"));
				return;
			}

			Charger charger = hero.buff(Charger.class);
			if (charger == null || charger.charges < 1) {
				GLog.w(Messages.get(this, "no_charge"));
				return;
			}
			GameScene.selectCell(attacker);
		}
	}

	@Override
	public String status() {
		if (isEquipped(Dungeon.hero) && Dungeon.hero.buff(Charger.class) != null) {
			return Dungeon.hero.buff(Charger.class).charges + "/" + maxCharges();
		} else {
			return super.status();
		}
	}

	private final CellSelector.Listener attacker = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell == null || curUser == null || curItem == null) return;

			Charger charger = curUser.buff(Charger.class);
			if (charger == null || charger.charges < 1) {
				GLog.w(Messages.get(BoneSpear.class, "no_charge"));
				return;
			}
			charger.charges--;
			Item.updateQuickslot();

			// 与法杖同样的顺序：先占住英雄，等骨矛飞到、伤害结算完再花回合。
			// 以前是先花回合再放动画，怪物已经开始行动，伤害却在之后某一帧才从渲染线程里结算
			curUser.busy();
			curUser.sprite.zap(cell, new Callback() {
				@Override
				public void call() {
					shootSpear(cell);
				}
			});
		}

		@Override
		public String prompt() {
			return Messages.get(BoneSpear.class, "prompt");
		}
	};

	private void shootSpear(int cell) {
		// 虚拟骨矛：沿直线穿透除了墙壁之外的一切
		com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica attack =
				new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(curUser.pos, cell,
						com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.STOP_TARGET
								| com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.STOP_SOLID);

		MagicMissile.boltFromChar(curUser.sprite.parent,
				MagicMissile.SHADOW, curUser.sprite, attack.collisionPos,
				new Callback() {
					@Override
					public void call() {
						// 伤害：100% + 武器等级*10%
						int dmg = (int) Math.round(damageRoll(curUser) * (1f + 0.1f * buffedLvl()));

						// 穿透路径上除了墙壁之外的一切事物
						boolean hitAny = false;
						ArrayList<Char> hit = new ArrayList<>();
						for (int p : attack.path) {
							Char ch = Actor.findChar(p);
							if (ch != null && ch != curUser && !hit.contains(ch)) {
								hit.add(ch);
								ch.damage(DamageInfo.magical(dmg, curItem));
								if (ch.sprite != null) {
									ch.sprite.centerEmitter().burst(BloodParticle.BURST, 3);
								}
								hitAny = true;
							}
							// 墙壁阻挡
							if (Dungeon.level.solid[p]) break;
						}

						if (hitAny) {
							Sample.INSTANCE.play(Assets.Sounds.HIT);
						} else {
							GLog.i(Messages.get(BoneSpear.class, "no_target"));
						}

						curUser.spendAndNext(1f);
					}
				});
	}

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

	public int maxCharges() {
		// 初始2，每提升3级增加1
		return 2 + level() / 3;
	}

	@Override
	public String statsInfo() {
		// 文本是「充能：%d」，不传参数会把 %d 原样显示出来
		return Messages.get(this, "stats_desc", maxCharges());
	}

	@Override
	public String info() {
		String info = super.info();
		if (isEquipped(Dungeon.hero)) {
			info += "\n\n" + Messages.get(this, "charge_info", Dungeon.hero.buff(Charger.class) != null
					? Dungeon.hero.buff(Charger.class).charges : 0, maxCharges());
		}
		return info;
	}

	// 骨矛的充能 Buff，每 50 回合恢复 1 点充能，享受法杖充能效果
	public static class Charger extends Buff {

		public int charges = 2;
		public float partialCharge;

		private static final float CHARGE_TURNS = 50f;

		@Override
		public boolean act() {
			if (charges < maxCharges()) {
				float chargeToGain = 1f / CHARGE_TURNS;

				chargeToGain *= RingOfEnergy.wandChargeMultiplier(target);

				for (Recharging bonus : target.buffs(Recharging.class)) {
					if (bonus != null && bonus.remainder() > 0f) {
						chargeToGain += 0.25f * bonus.remainder();
					}
				}

				partialCharge += chargeToGain;
				if (partialCharge >= 1) {
					charges++;
					partialCharge--;
					updateQuickslot();
				}
			} else {
				partialCharge = 0;
			}
			spend(TICK);
			return true;
		}

		private int maxCharges() {
			if (target instanceof Hero && ((Hero) target).belongings.weapon instanceof BoneSpear) {
				return ((BoneSpear) ((Hero) target).belongings.weapon).maxCharges();
			}
			return 2;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", charges);
		}

		private static final String CHARGES = "charges";
		private static final String PARTIAL = "partialCharge";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CHARGES, charges);
			bundle.put(PARTIAL, partialCharge);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			charges = bundle.getInt(CHARGES);
			partialCharge = bundle.getFloat(PARTIAL);
		}
	}
}
