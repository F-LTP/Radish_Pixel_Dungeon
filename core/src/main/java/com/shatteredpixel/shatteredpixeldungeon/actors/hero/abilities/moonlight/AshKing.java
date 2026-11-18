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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
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

	//英雄每次行动前刷新索命弯刀形态的「附近有没有敌人」，移速在这之后才会被用到
	@SubscribeEvent(event = HeroActEvent.class, priority = 0)
	public static void onHeroAct(HeroActEvent event) {
		FatalBladeForm form = event.getHero().buff(FatalBladeForm.class);
		if (form != null) form.refreshEnemyNearby();
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

	//三种形态共用：月华在形态期间换用专门的一行贴图，所以形态开始和结束时都要刷新英雄外观
	private static class AshKingBaseBuff extends FlavourBuff {
		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)) {
				refreshSprite();
				return true;
			}
			return false;
		}

		@Override
		public void detach() {
			super.detach();
			refreshSprite();
		}

		//读档时 buff 先于英雄贴图恢复，这时还没有贴图可刷，贴图创建时会自己按当前 buff 取行
		private void refreshSprite() {
			if (target != null && target.sprite instanceof HeroSprite) {
				((HeroSprite) target.sprite).updateArmor();
			}
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
				//只推敌人：关卡里的怪还包括友军（召唤物、被腐化的怪）和中立单位，它们不该被推开、吃碰撞伤害
				if (mob.alignment == Char.Alignment.ENEMY
						&& Dungeon.level.distance(hero.pos, mob.pos) <= 3) {
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
					//走到这里说明怪物已经是这条线在地图内的最后一格，身后就是地图边界，无处可退。
					//不能拿怪物自己的格子当目标：起点和终点相同的弹道会退化成一条指向左上的斜线，把怪物朝不相干的方向扔出去
					continue;
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
	public static class SoulStreamForm extends AshKingBaseBuff {

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
	public static class FatalBladeForm extends AshKingBaseBuff {

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

		//不能重写 act() 来做这件事：限时 buff 的 act() 只在持续时间走完的那一刻执行一次，而且那一次必须结束形态。
		//放在 act() 里的话，持续期间一次也不会刷新，到点之后形态又永远不结束。所以由 AshKing.onHeroAct 每回合调用
		private void refreshEnemyNearby() {
			hasEnemyNearby = false;
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.alignment == Char.Alignment.ENEMY
						&& Dungeon.level.distance(target.pos, mob.pos) <= 4) {
					hasEnemyNearby = true;
					break;
				}
			}
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
