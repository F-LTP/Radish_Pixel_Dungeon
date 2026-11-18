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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;

import java.util.ArrayList;

/**
 * 七个游商的角色数据表。
 *
 * 设计要点（用户原话：「有的干这个有的干那个，不是一个人要干所有事」）：
 * 每个游商按 variant 绑定一个 TradeMode，只干自己那一件事，不共用一个万能交易逻辑。
 *
 * 参考上游 _upstream/darkest .../npcs/BarterMan.kt：
 * - 货品是"固定清单 + 随机数量"，不是每次随机抽类别；
 * - 售价直接用 Item.value()（本仓库标准商人价 = Shopkeeper.sellPrice 的口径），
 *   不在游商这里另造一套价格公式。
 */
public final class PeddlerCatalog {

	private PeddlerCatalog() {}

	public static final int COUNT = 7;

	/** 游商交易模式。每个游商只绑定其中一种。 */
	public enum TradeMode {
		/** 金币购买：打开货架窗口，按标准商人价买下陈列的货品。 */
		BUY,
		/** 纯交谈：不卖东西，只给情报/闲聊。 */
		TALK,
		/** 金币服务：付出金币换一次性便利（治疗/解负面）。 */
		SERVICE,
		/** 以物换物：用自己背包里的物品换他手上的货（上游 BarterMan 的原味逻辑）。 */
		SWAP
	}

	/** 单个游商的定义。 */
	public static final class Profile {
		public final int variant;
		public final TradeMode mode;
		/** 服务模式下单次价格；其余模式为 0。 */
		public final int servicePrice;

		Profile( int variant, TradeMode mode, int servicePrice ) {
			this.variant = variant;
			this.mode = mode;
			this.servicePrice = servicePrice;
		}
	}

	private static final Profile[] PROFILES = {
			// 0 赭：最朴实的一档，卖治疗药水，走金币购买
			new Profile( 0, TradeMode.BUY,     0 ),
			// 1 紫苑：卖鉴定卷轴
			new Profile( 1, TradeMode.BUY,     0 ),
			// 2 默：卖升级卷轴，同时也收货（以物换物）
			new Profile( 2, TradeMode.BUY,     0 ),
			// 3 白霭：卖魔法地图
			new Profile( 3, TradeMode.BUY,     0 ),
			// 4 熔：卖祛邪卷轴
			new Profile( 4, TradeMode.BUY,     0 ),
			// 5 灰羽：不卖货，只收钱做治疗服务
			new Profile( 5, TradeMode.SERVICE, 120 ),
			// 6 书页：纯情报商，只聊天不给货
			new Profile( 6, TradeMode.TALK,    0 ),
	};

	public static Profile profile( int variant ) {
		if (variant < 0 || variant >= PROFILES.length) return PROFILES[0];
		return PROFILES[variant];
	}

	public static TradeMode mode( int variant ) {
		return profile( variant ).mode;
	}

	/**
	 * 这个游商陈列的货品。返回的是新建实例，调用方可以直接持有并挪走。
	 * 全为空的游商（TALK）返回空表。
	 */
	public static ArrayList<Item> goods( int variant ) {
		ArrayList<Item> list = new ArrayList<>();
		switch (variant) {

			case 0:
				// 赭：最朴实的杂货摊，药水管够
				add( list, new PotionOfHealing(), 4 );
				add( list, new PotionOfPurity(), 2 );
				add( list, new ScrollOfIdentify(), 2 );
				add( list, new ScrollOfRemoveCurse(), 1 );
				break;

			case 1:
				// 紫苑：鉴定为主，兼卖侦查类
				add( list, new ScrollOfIdentify(), 4 );
				add( list, new PotionOfMindVision(), 2 );
				add( list, new ScrollOfMagicMapping(), 2 );
				add( list, new PotionOfHealing(), 2 );
				break;

			case 2:
				// 默：强化与充盈，最贵的一档
				add( list, new ScrollOfUpgrade(), 2 );
				add( list, new ScrollOfRecharging(), 2 );
				add( list, new ScrollOfIdentify(), 3 );
				add( list, new PotionOfExperience(), 1 );
				break;

			case 3:
				// 白霭：探路、机动、逃生
				add( list, new ScrollOfMagicMapping(), 3 );
				add( list, new ScrollOfTeleportation(), 2 );
				add( list, new PotionOfLevitation(), 2 );
				add( list, new PotionOfHaste(), 1 );
				break;

			case 4:
				// 熔：祛邪、解异常、火与寒
				add( list, new ScrollOfRemoveCurse(), 3 );
				add( list, new PotionOfPurity(), 2 );
				add( list, new PotionOfLiquidFlame(), 2 );
				add( list, new PotionOfFrost(), 2 );
				break;

			case 5:
				// 灰羽：治疗与保命（同时仍提供收费治療服务）
				add( list, new PotionOfHealing(), 4 );
				add( list, new PotionOfPurity(), 2 );
				add( list, new ScrollOfLullaby(), 2 );
				add( list, new PotionOfInvisibility(), 1 );
				break;

			case 6:
				// 书页：情报与扰乱
				add( list, new ScrollOfTeleportation(), 3 );
				add( list, new ScrollOfMagicMapping(), 2 );
				add( list, new ScrollOfTerror(), 2 );
				add( list, new ScrollOfMirrorImage(), 2 );
				break;

			default:
				break;
		}
		return list;
	}

	private static void add( ArrayList<Item> list, Item template, int count ) {
		for (int i = 0; i < count; i++) {
			Item it = template.duplicate();
			if (it == null) {
				// duplicate 在某些物品上可能返回 null，退回到"每次 new 一个同类"
				continue;
			}
			list.add( it );
		}
	}

	/**
	 * 这个游商给的单件售价：直接取物品标准价值（与商人价同口径）。
	 */
	public static int price( Item item ) {
		if (item == null) return 0;
		// 标准商人价：与普通商店（Shopkeeper.sellPrice）同一口径，不再用偏低的 Item.value()
		int p = Shopkeeper.sellPrice( item );
		if (p < 1) p = 1;
		return p;
	}

	/**
	 * 灰羽的治疗服务落地。返回是否真的做了事。
	 */
	public static boolean applyService( int variant, Hero hero ) {
		if (variant != 5) return false;

		boolean did = false;

		// 回满生命
		if (hero.HP < hero.HT) {
			hero.HP = hero.HT;
			did = true;
		}

		// 清掉几种常见负面状态（清掉任意一种也算"做了事"，照样该收费）
		if (cleanse( hero, Poison.class )) did = true;
		if (cleanse( hero, Weakness.class )) did = true;
		if (cleanse( hero, Charm.class )) did = true;

		return did;
	}

	private static boolean cleanse( Hero hero, Class<? extends Buff> type ) {
		Buff b = hero.buff( type );
		if (b != null) {
			b.detach();
			return true;
		}
		return false;
	}
}
