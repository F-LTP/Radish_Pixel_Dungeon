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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroLevelUpEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 嫉妒附魔（武器）。
 * <p>
 * 武器性能比正常高 {@code 30% + 5%*等级}，但会因主人的冷落而积累“嫉妒值”，
 * 使加成按比例衰减，最低保留 30%。击杀非由本武器完成、英雄升级、下楼、
 * 把升级卷轴用在别处、以及背包里长期囤积升级卷轴都会增加嫉妒值；
 * 对本武器使用升级卷轴、或周期性地证明“自己是背包里最有价值的物品”会消退嫉妒。
 * <p>
 * 数值设计上，正常流程中嫉妒的积累会略快于消退。
 */
public class Jealousy extends Weapon.Enchantment {

	private static ItemSprite.Glowing PURPLE = new ItemSprite.Glowing( 0xAA44CC );

	//性能加成
	private static final float BASE_BONUS = 0.30f;
	private static final float BONUS_PER_LEVEL = 0.05f;
	//嫉妒下限：加成最多衰减到其 30%
	private static final float MIN_BONUS_RATIO = 0.30f;

	//嫉妒增长
	private static final float KILL_GAIN = 0.05f;
	private static final float LEVEL_UP_GAIN = 0.10f;
	private static final float DESCEND_GAIN = 0.10f;
	private static final float SCROLL_OTHER_GAIN = 0.20f;
	private static final float NEGLECT_GAIN_PER_SCROLL = 0.005f;
	//嫉妒消退
	private static final float SCROLL_SELF_RECOVER = 0.40f;
	private static final float PROUD_RECOVER = 0.10f;

	//“自己是否背包里最有价值”的随机判定间隔（回合）
	private static final int CHECK_MIN = 15;
	private static final int CHECK_MAX = 30;
	//心情系数：0.5~1.5，越低越挑剔，满意所需的最小差值越大
	private static final float MOOD_MIN = 0.5f;
	private static final float MOOD_MAX = 1.5f;
	//价值方案的基础领先值（金币，value() 单位）。参考物价：
	//药水/卷轴 30/张、t5+5 武器 600（附魔 900）、+3 戒指 300、t5+10 附魔武器 1650。
	private static final float VALUE_GAP = 200f;

	//触发台词的概率（每种情况有 3 条随机台词，但不一定开口）
	private static final float SAY_CHANCE = 0.4f;

	private float jealousy = 0f;          //0~1
	private int checkTimer = Random.IntRange(CHECK_MIN, CHECK_MAX);
	private int lastDepth = -1;
	private float mood = Random.Float(MOOD_MIN, MOOD_MAX);

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		return Math.max(1, Math.round(damage * performanceMultiplier(weapon.buffedLvl(), jealousy)));
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return PURPLE;
	}

	public float jealousy() {
		return jealousy;
	}

	/** 含嫉妒衰减的性能倍率，始终 ≥ 1。 */
	public static float performanceMultiplier(int level, float jealousy) {
		float maxBonus = BASE_BONUS + BONUS_PER_LEVEL * Math.max(0, level);
		float ratio = MIN_BONUS_RATIO + (1f - MIN_BONUS_RATIO) * (1f - clamp01(jealousy));
		return 1f + maxBonus * ratio;
	}

	// ------------------------------------------------------------------
	// 事件
	// ------------------------------------------------------------------

	@SubscribeEvent(event = HeroLevelUpEvent.class, priority = 0)
	public static void onHeroLevelUp(HeroLevelUpEvent event) {
		Jealousy j = equipped(event.getHero());
		if (j != null) j.addJealousy(event.getHero(), LEVEL_UP_GAIN, "msg_levelup");
	}

	@SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
	public static void onMonsterKilled(CharFinalDamageEvent event) {
		if (Dungeon.hero == null) return;
		Char target = event.getTarget();
		if (target.isAlive() || target.alignment != Char.Alignment.ENEMY) return;
		if (event.getAttacker() != Dungeon.hero) return;

		Jealousy j = equipped(Dungeon.hero);
		if (j == null) return;

		//本武器完成击杀则不加嫉妒
		Item source = event.getSourceItem();
		if (source instanceof Weapon && ((Weapon) source).enchantment == j) return;

		j.addJealousy(Dungeon.hero, KILL_GAIN, "msg_kill");
	}

	@SubscribeEvent(event = HeroActEvent.class, priority = 0)
	public static void onHeroAct(HeroActEvent event) {
		Hero hero = event.getHero();
		Jealousy j = equipped(hero);
		if (j == null) return;

		//下楼
		if (j.lastDepth == -1) {
			j.lastDepth = Dungeon.depth;
		} else if (Dungeon.depth > j.lastDepth) {
			j.lastDepth = Dungeon.depth;
			j.addJealousy(hero, DESCEND_GAIN, "msg_descend");
		} else {
			j.lastDepth = Dungeon.depth;
		}

		//背包里长期囤积的升级卷轴，越多衰减越快
		int scrolls = scrollCount(hero);
		if (scrolls > 0) {
			j.jealousy = clamp01(j.jealousy + NEGLECT_GAIN_PER_SCROLL * scrolls);
		}

		//每隔随机一段时间，重新掷一次心情，并判定自己是不是背包里最有价值的物品
		j.checkTimer--;
		if (j.checkTimer <= 0) {
			j.checkTimer = Random.IntRange(CHECK_MIN, CHECK_MAX);
			j.mood = Random.Float(MOOD_MIN, MOOD_MAX);

			if (j.isMostValuable(hero) && j.jealousy > 0f) {
				j.jealousy = Math.max(0f, j.jealousy - PROUD_RECOVER);
				j.say(hero, "msg_recover_proud");
			} else if (scrolls > 0) {
				j.say(hero, "msg_neglect");
			}
		}
	}

	/** 由 {@link ScrollOfUpgrade} 在升级物品后调用。 */
	public static void onUpgradeScrollUsed(Item upgraded) {
		if (Dungeon.hero == null) return;
		Jealousy j = equipped(Dungeon.hero);
		if (j == null) return;

		if (upgraded instanceof Weapon && ((Weapon) upgraded).enchantment == j) {
			j.jealousy = Math.max(0f, j.jealousy - SCROLL_SELF_RECOVER);
			j.say(Dungeon.hero, "msg_recover_scroll");
		} else {
			j.addJealousy(Dungeon.hero, SCROLL_OTHER_GAIN, "msg_scroll_other");
		}
	}

	// ------------------------------------------------------------------
	// 内部工具
	// ------------------------------------------------------------------

	private void addJealousy(Hero hero, float amount, String msgKey) {
		float before = jealousy;
		jealousy = clamp01(jealousy + amount);
		if (jealousy > before) say(hero, msgKey);
	}

	/**
	 * 随机说一句傲娇台词。每种情况有 3 条候选，且只有 {@link #SAY_CHANCE} 的概率开口。
	 */
	private void say(Hero hero, String msgKey) {
		if (!canShow(hero)) return;
		if (Random.Float() >= SAY_CHANCE) return;
		int variant = Random.Int(3) + 1;
		GLog.i(Messages.get(this, msgKey + "_" + variant));
	}

	/** 当前装备的、带嫉妒附魔的武器附魔实例，没有则返回 null。 */
	public static Jealousy equipped(Hero hero) {
		if (hero == null || hero.belongings == null) return null;
		KindOfWeapon w = hero.belongings.weapon();
		if (w instanceof Weapon && ((Weapon) w).enchantment instanceof Jealousy) {
			return (Jealousy) ((Weapon) w).enchantment;
		}
		return null;
	}

	private static Weapon weaponOf(Hero hero) {
		if (hero == null || hero.belongings == null) return null;
		KindOfWeapon w = hero.belongings.weapon();
		return w instanceof Weapon ? (Weapon) w : null;
	}

	private static int scrollCount(Hero hero) {
		if (hero == null || hero.belongings == null) return 0;
		Item s = hero.belongings.getItem(ScrollOfUpgrade.class);
		return s == null ? 0 : s.quantity();
	}

	private static boolean canShow(Hero hero) {
		Weapon w = weaponOf(hero);
		return w != null && w.isIdentified();
	}

	/**
	 * 自己是不是背包里“最有价值”的物品。两套方案满足其一即可：
	 * <ol>
	 *     <li>等级鹤立鸡群，且剩余的升级卷轴足够少；</li>
	 *     <li>身价明显高于背包里的其他物品。</li>
	 * </ol>
	 * 心情系数（0.5~1.5）越低越挑剔，满意所需的最小差值越大；
	 * 同时决定“卷轴足够少”的容忍张数（0~2 张）。
	 */
	private boolean isMostValuable(Hero hero) {
		Weapon self = weaponOf(hero);
		if (self == null) return false;

		int myLevel = self.level();
		int myValue = self.value();
		int maxOtherLevel = 0;
		int maxOtherValue = 0;
		for (Item item : hero.belongings) {
			if (item == self) continue;
			maxOtherLevel = Math.max(maxOtherLevel, item.level());
			maxOtherValue = Math.max(maxOtherValue, item.value());
		}

		//心情越好（mood 越高），要求越低（demand 越小）
		float demand = (MOOD_MIN + MOOD_MAX) - mood;   //0.5~1.5

		//方案一：等级达到其他物品最高等级的 (1 + demand) 倍，且剩余卷轴不超过容忍张数
		int allowedScrolls = Math.round((mood - MOOD_MIN) * 2f);   //0~2
		boolean levelProud = myLevel >= maxOtherLevel * (1f + demand);
		boolean scrollsFew = scrollCount(hero) <= allowedScrolls;

		//方案二：身价比其他物品最高身价还高出 VALUE_GAP * demand
		boolean valueProud = myValue - maxOtherValue >= VALUE_GAP * demand;

		return (levelProud && scrollsFew) || valueProud;
	}

	private static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}

	// ------------------------------------------------------------------
	// 存档
	// ------------------------------------------------------------------

	private static final String JEALOUSY = "jealousy";
	private static final String CHECK_TIMER = "check_timer";
	private static final String LAST_DEPTH = "last_depth";
	private static final String MOOD = "mood";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(JEALOUSY, jealousy);
		bundle.put(CHECK_TIMER, checkTimer);
		bundle.put(LAST_DEPTH, lastDepth);
		bundle.put(MOOD, mood);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		if (bundle.contains(JEALOUSY)) jealousy = bundle.getFloat(JEALOUSY);
		if (bundle.contains(CHECK_TIMER)) checkTimer = bundle.getInt(CHECK_TIMER);
		if (bundle.contains(LAST_DEPTH)) lastDepth = bundle.getInt(LAST_DEPTH);
		if (bundle.contains(MOOD)) mood = bundle.getFloat(MOOD);
	}
}
