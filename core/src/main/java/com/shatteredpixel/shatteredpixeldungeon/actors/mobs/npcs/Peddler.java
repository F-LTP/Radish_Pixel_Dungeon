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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeddlerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDialogue;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPeddlerShop;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;

/**
 * 一楼游商。
 *
 * 参考上游 _upstream/darkest .../actors/mobs/npcs/BarterMan.kt：
 * - interact() 用 WndDialogue 弹对话（问候语 + 动作按钮），不是 WndOptions 三个按钮；
 * - 每个游商按 variant 绑定一种 TradeMode，各干各的活，不共用一个万能交易逻辑；
 * - 被攻击 / 加负面 buff 时 flee()。
 *
 * 与上游的差别：本仓库没有 WndShop 基类，所以货架窗口自己写了一份 WndPeddlerShop；
 * 视觉结构（标题栏 + 5 列商品格 + 黄色价签）保持一致。
 */
public class Peddler extends NPC {

	private static final String VARIANT = "variant";
	private static final String STOCKED = "stocked";
	private static final String GOODS   = "goods";

	private int variant = 0;
	private boolean stocked = false;

	/** 当前货架。铺一次，玩家买走一件就少一件。 */
	private ArrayList<Item> goods = new ArrayList<>();

	public Peddler() {
		this( 0 );
	}

	public Peddler( int variant ) {
		super();
		this.variant = variant;

		properties.add( Property.IMMOVABLE );
	}

	public int variant() {
		return variant;
	}

	public PeddlerCatalog.TradeMode mode() {
		return PeddlerCatalog.mode( variant );
	}

	@Override
	public CharSprite sprite() {
		return new PeddlerSprite( variant );
	}

	@Override
	public String name() {
		return Messages.get( Peddler.class, "name_" + variant );
	}

	@Override
	public String description() {
		return Messages.get( Peddler.class, "desc_" + variant );
	}

	public String greeting() {
		return Messages.get( Peddler.class, "talk_" + variant );
	}

	/** 货架内容。首次访问时铺货。 */
	public ArrayList<Item> goods() {
		stock();
		return goods;
	}

	/** 买走一件后从货架上移除。 */
	public void removeGood( Item item ) {
		if (item == null) return;
		goods.remove( item );
	}

	/**
	 * 首次铺货。对照上游 BarterMan 的 init，货品是固定清单，不是随机抽类别。
	 */
	public void stock() {
		if (stocked) return;
		stocked = true;

		goods = PeddlerCatalog.goods( variant );
	}

	@Override
	protected boolean act() {
		// 每 tick 转身朝向英雄，和上游 BarterMan.act() 一致
		if (sprite != null && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			sprite.turnTo( pos, Dungeon.hero.pos );
		}
		spend( TICK );
		return true;
	}

	@Override
	public void damage( DamageInfo info ) {
		// 测试用 NPC：不会死，挨打也只是站着不动（不再挨打就跑/消失）
	}

	/** 与英雄换位（对话选项「换位」） */
	public void swapWithHero() {
		Hero hero = Dungeon.hero;
		if (hero == null) return;

		// 换位＝把游商放到英雄原来的格子上。那格必须能站、没被别的单位占、也不能是陷阱格，
		// 否则相当于借「换位」把 NPC 塞进墙里、或者白吃一次陷阱。
		int target = hero.pos;
		if (hero.rooted || !Dungeon.level.passable[target]
				|| Dungeon.level.traps.get( target ) != null
				|| com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( target ) != hero) {
			GLog.w( Messages.get( Peddler.class, "swap_blocked" ) );
			return;
		}

		int myPos = pos;
		pos = target;
		hero.pos = myPos;

		if (sprite != null) {
			sprite.place( pos );
		}
		if (hero.sprite != null) {
			hero.sprite.place( hero.pos );
		}
		Dungeon.observe();
		GLog.i( Messages.get( Peddler.class, "swap_done" ) );
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero) {
			return true;
		}

		final Peddler self = this;

		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				openDialogue( self );
			}
		} );

		return true;
	}

	/** 按 TradeMode 组装对话窗口。这是"有的干这个有的干那个"的落地点。 */
	private void openDialogue( final Peddler self ) {

		final Hero hero = Dungeon.hero;
		if (hero == null) return;

		PeddlerCatalog.TradeMode mode = PeddlerCatalog.mode( variant );

		ArrayList<String> labels = new ArrayList<>();
		final ArrayList<Callback> cbs = new ArrayList<>();

		switch (mode) {

			case BUY:
				labels.add( Messages.get( Peddler.class, "opt_buy" ) );
				cbs.add( new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndPeddlerShop( self ) );
					}
				} );
				labels.add( Messages.get( Peddler.class, "opt_talk" ) );
				cbs.add( new Callback() {
					@Override
					public void call() {
						GLog.i( Messages.get( Peddler.class, "talk_more_" + variant ) );
					}
				} );
				break;

			case SERVICE:
				labels.add( Messages.get( Peddler.class, "opt_service",
						PeddlerCatalog.profile( variant ).servicePrice ) );
				cbs.add( new Callback() {
					@Override
					public void call() {
						buyService( variant );
					}
				} );
				labels.add( Messages.get( Peddler.class, "opt_talk" ) );
				cbs.add( new Callback() {
					@Override
					public void call() {
						GLog.i( Messages.get( Peddler.class, "talk_more_" + variant ) );
					}
				} );
				break;

			case TALK:
			default:
				labels.add( Messages.get( Peddler.class, "opt_talk" ) );
				cbs.add( new Callback() {
					@Override
					public void call() {
						GLog.i( Messages.get( Peddler.class, "talk_more_" + variant ) );
					}
				} );
				// 书页虽然只聊天，但会顺手给一张卷轴，所以也给个"讨点东西"
				if (!PeddlerCatalog.goods( variant ).isEmpty()) {
					labels.add( Messages.get( Peddler.class, "opt_buy" ) );
					cbs.add( new Callback() {
						@Override
						public void call() {
							GameScene.show( new WndPeddlerShop( self ) );
						}
					} );
				}
				break;
		}

		// 换位：测试时常用，把玩家和游商的位置对调，方便让开通道或凑近货架
		labels.add( Messages.get( Peddler.class, "opt_swap" ) );
		cbs.add( new Callback() {
			@Override
			public void call() {
				self.swapWithHero();
			}
		} );

		labels.add( Messages.get( Peddler.class, "opt_leave" ) );
		cbs.add( null );

		GameScene.show( new WndDialogue(
				new PeddlerSprite( variant ),
				Messages.titleCase( name() ),
				greeting(),
				labels.toArray( new String[0] ),
				cbs.toArray( new Callback[0] ) ) );
	}

	@Override
	public boolean reset() {
		return true;
	}

	/**
	 * 收费服务：先验金币，再真的做事，做成了才扣钱。
	 * 三处反馈都要给（没钱 / 没什么可治 / 收钱治好了），否则点了选项毫无动静。
	 */
	private static void buyService( int variant ) {
		final Hero hero = Dungeon.hero;
		if (hero == null) return;

		final int price = PeddlerCatalog.profile( variant ).servicePrice;

		if (Dungeon.gold < price) {
			GLog.w( Messages.get( Peddler.class, "service_poor", price ) );
			return;
		}

		if (!PeddlerCatalog.applyService( variant, hero )) {
			GLog.i( Messages.get( Peddler.class, "service_idle" ) );
			return;
		}

		Dungeon.gold -= price;
		com.shatteredpixel.shatteredpixeldungeon.items.armor.CountessDress.onGoldChanged();
		GLog.p( Messages.get( Peddler.class, "service_done", price ) );
	}
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VARIANT, variant );
		bundle.put( STOCKED, stocked );
		// 货架内容必须一起存：里面既有「还剩什么」，也隐含「已经卖掉了什么」。
		// 只存 stocked 不存 goods，读档后就成了「标记为已铺货 + 空货架」，stock() 永远不会再补货。
		bundle.put( GOODS, goods );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		variant = bundle.getInt( VARIANT );
		stocked = bundle.getBoolean( STOCKED );

		goods = new ArrayList<>();
		if (bundle.contains( GOODS )) {
			// 旧档没有 GOODS 键：这类档要当作「还没铺过货」，把 stocked 放回去让 stock() 重建，
			// 否则会复现「货架永远空」的老 bug。
			for (Bundlable b : bundle.getCollection( GOODS )) {
				if (b instanceof Item) {
					goods.add( (Item)b );
				}
			}
		} else {
			stocked = false;
		}
	}
}
