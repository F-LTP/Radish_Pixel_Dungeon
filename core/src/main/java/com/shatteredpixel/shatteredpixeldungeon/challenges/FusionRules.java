package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;

/**
 * 「智能怪物」挑战下的装备融合规则。
 *
 * <p>挑战开启时：</p>
 * <ul>
 *   <li>力量药水、升级卷轴不再生成（随机掉落、初始物品、Boss 与泉水掉落、商店目录都拦掉）</li>
 *   <li>装备融合替代它们：开局 1 次机会，之后每升 2 级 +1 次</li>
 * </ul>
 */
public final class FusionRules {

	private FusionRules() {
	}

	/** 挑战是否启用融合规则。 */
	public static boolean active() {
		return Dungeon.isChallenged(Challenges.REAL_INTELLIGENCE);
	}

	/** 该物品是否被挑战禁用（力量药水 / 升级卷轴）。 */
	public static boolean banned(Item item) {
		if (!active() || item == null) return false;
		return item instanceof PotionOfStrength || item instanceof ScrollOfUpgrade;
	}
}
