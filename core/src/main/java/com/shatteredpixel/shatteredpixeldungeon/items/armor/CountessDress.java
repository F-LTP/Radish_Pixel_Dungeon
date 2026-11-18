/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** 伯爵礼服：二阶护甲，获得或花费金币后回复少量生命（赎回商品的花费不算）。 */
public class CountessDress extends Armor {

	{ image = ItemSpriteSheet.ARMOR_COUNTESS; }

	public CountessDress() { super( 2 ); }

	@Override public int DRMax(int lvl){ return 4 + 2 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 1 + lvl; }

	private static int healAmount( int lvl ){ return Math.round( 1 + 0.5f * lvl ); }

	// 未鉴定时只给 +0 的数值，否则能从描述里的数字反推出等级
	@Override
	public String statsInfo() {
		if (isIdentified()) {
			return Messages.get( this, "stats_desc", healAmount( buffedLvl() ) );
		} else {
			return Messages.get( this, "typical_stats_desc", healAmount( 0 ) );
		}
	}

	/** 金币变化回调：由改动金币的代码在改完之后调用。赎回已卖出物品的扣款按设计不算，不要在那里调用。 */
	public static void onGoldChanged(){
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		// 取 armor() 而不是 armor 字段：遗落行囊期间身上的护甲不生效
		Armor armor = hero.belongings.armor();
		if (!(armor instanceof CountessDress)) return;
		// 走统一治疗入口：直接改 HP 不会发出 HeroHealEvent，依赖它的机制会漏掉这次回血
		hero.heal( healAmount( armor.buffedLvl() ) );
	}
}
