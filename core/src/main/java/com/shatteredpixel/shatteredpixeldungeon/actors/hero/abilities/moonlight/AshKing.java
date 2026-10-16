/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: You can redistribute it and/or modify
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 薪王化身护甲技能
 * 月华将神器/法杖/投武转化为三种战斗形态
 * 充能消耗：50
 */
public class AshKing extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		// 检查是否已有形态 Buff（互斥）
		if (hero.buff(HolyLanceForm.class) != null ||
			hero.buff(SoulStreamForm.class) != null ||
			hero.buff(FatalBladeForm.class) != null) {
			GLog.w(Messages.get(this, "already_active"));
			return;
		}

		// 激活后等待下一次使用神器/法杖/投武时触发化身
		Buff.affect(hero, IncarnationReady.class);
		((HeroSprite)hero.sprite).updateArmor();

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();

		GLog.i(Messages.get(this, "ready"));
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
	}

	@Override
	public String icon() {
		return HeroIcon.ASH_KING;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.HOLY_LANCE, Talent.SOUL_STREAM, Talent.FATAL_BLADE, Talent.HEROIC_ENERGY};
	}

	/**
	 * 化身就绪状态 Buff
	 * 下一次使用神器/法杖/投武时触发对应形态
	 */
	public static class IncarnationReady extends Buff {

		@Override
		public String icon() {
			return BuffIndicator.BLESS;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.8f, 0.4f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc");
		}

		@Override
		public boolean act() {
			spend(TICK);
			return true;
		}

		public boolean tryIncarnate(Hero hero, String itemType) {
			if (itemType.equals("Artifact")) {
				int duration = 10 + hero.pointsInTalent(Talent.HOLY_LANCE) * 5;
				Buff.affect(hero, HolyLanceForm.class, duration);
				HolyLanceForm.knockbackEnemies(hero);
				Buff.affect(hero, Light.class, Light.DURATION);
				hero.sprite.update();
				detach();
				GLog.p(Messages.get(HolyLanceForm.class, "activated", duration));
				GLog.p(Messages.get(this, "holy_lance"));
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				return true;
			} else if (itemType.equals("Wand")) {
				int duration = 10 + hero.pointsInTalent(Talent.SOUL_STREAM) * 5;
				SoulStreamForm buff = Buff.affect(hero, SoulStreamForm.class, duration);
				buff.freeCasts = 5;
				detach();
				GLog.p(Messages.get(SoulStreamForm.class, "activated", duration, 5));
				GLog.p(Messages.get(this, "soul_stream"));
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				return true;
			} else if (itemType.equals("MissileWeapon")) {
				int duration = 10 + hero.pointsInTalent(Talent.FATAL_BLADE) * 5;
				Buff.affect(hero, FatalBladeForm.class, duration);
				detach();
				GLog.p(Messages.get(FatalBladeForm.class, "activated", duration));
				GLog.p(Messages.get(this, "fatal_blade"));
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				return true;
			}
			return false;
		}
	}

	private static class AshKingBaseBuff extends FlavourBuff {
		@Override
		public void detach() {
			super.detach();
			((HeroSprite) Dungeon.hero.sprite).updateArmor();
		}
	}

	/**
	 * 神佑长枪形态
	 * 使用神器激活
	 * 效果：照明、攻击距离+1、击退周围敌人
	 */
	public static class HolyLanceForm extends AshKingBaseBuff {

		{
			type = buffType.POSITIVE;
		}

		public static final float DURATION = 25f;

		@Override
		public String icon() {
			return BuffIndicator.BLESS;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.9f, 0.5f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(visualcooldown()));
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)visualcooldown());
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		public static void knockbackEnemies(Hero hero) {
			ArrayList<Mob> enemies = new ArrayList<>();
			for (Mob mob : Dungeon.level.mobs) {
				if (Dungeon.level.distance(hero.pos, mob.pos) <= 3) {
					enemies.add(mob);
				}
			}

			for (Mob mob : enemies) {
				// 计算推开方向：从hero指向mob的方向
				Ballistica heroToMob = new Ballistica(hero.pos, mob.pos, Ballistica.PROJECTILE);
				// 获取mob在轨迹上的位置（轨迹终点或mob位置）
				int mobIndex = heroToMob.path.indexOf(mob.pos);
				// 轨迹终点：沿着推开方向继续延伸
				int targetPos;
				if (mobIndex >= 0 && mobIndex < heroToMob.path.size() - 1) {
					// mob在轨迹上，取轨迹终点作为推开目标
					targetPos = heroToMob.path.get(heroToMob.path.size() - 1);
				} else {
					// mob不在轨迹上（可能在侧面），直接用mob位置
					targetPos = mob.pos;
				}
				// 创建从mob到推开目标的轨迹（使用MAGIC_BOLT不受墙壁阻挡）
				Ballistica knockbackPath = new Ballistica(mob.pos, targetPos, Ballistica.MAGIC_BOLT);
				int knockbackDist = 5;
				WandOfBlastWave.throwChar(mob, knockbackPath, knockbackDist, true, true, null);
			}

			if (enemies.size() > 0) {
				Sample.INSTANCE.play(Assets.Sounds.BLAST);
			}
		}
	}

	/**
	 * 灵魂激流形态
	 * 使用法杖激活
	 * 效果：5次免费法杖攻击，攻击穿透敌人
	 */
	public static class SoulStreamForm extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public static final float DURATION = 25f;

		public int freeCasts = 5;

		@Override
		public String icon() {
			return BuffIndicator.RECHARGING;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.5f, 0.8f, 1f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(visualcooldown()), freeCasts);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(freeCasts);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		public boolean consumeFreeCast() {
			if (freeCasts > 0) {
				freeCasts--;
//				GLog.i(Messages.get(this, "cast_used", freeCasts));
				return true;
			}
			return false;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("freeCasts", freeCasts);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			freeCasts = bundle.getInt("freeCasts");
		}
	}

	/**
	 * 致命刀刃形态（索提）
	 * 使用投武激活
	 * 效果：投武产生毒雾，范围内有敌人时移速提升
	 */
	public static class FatalBladeForm extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public static final float DURATION = 25f;

		public boolean hasEnemyNearby = false;

		@Override
		public String icon() {
			return BuffIndicator.WEAPON;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.6f, 0.2f, 0.8f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(visualcooldown()));
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)visualcooldown());
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public boolean act() {
			hasEnemyNearby = false;
			for (Mob mob : Dungeon.level.mobs) {
				if (Dungeon.level.distance(target.pos, mob.pos) <= 4) {
					hasEnemyNearby = true;
					break;
				}
			}
			spend(TICK);
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("hasEnemyNearby", hasEnemyNearby);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			hasEnemyNearby = bundle.getBoolean("hasEnemyNearby");
		}
	}
}
