/*
 * Radish Pixel Dungeon
 * 炼金学徒挑战：规则判定与可调参数的集中点
 */

package com.shatteredpixel.shatteredpixeldungeon.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class AlchemyApprenticeChallenge {

	//每个炼金配方额外消耗的炼金能量
	public static final int EXTRA_ENERGY_COST = 2;

	//原本不消耗能量的配方（种子酿药水、卷轴拆符石、寒霜炸弹、绵绵炸弹）是否也要加价。
	//设计原文是「所有炼金配方」，这里按字面实现；如果只想让本来就耗能的配方变贵，把它改成 false 即可
	public static final boolean CHARGE_FREE_RECIPES = true;

	//拆解卷轴时被换进来的那颗随机符石，是否允许和卷轴原本对应的种类相同。
	//取 false 时挑战每次都有可见的效果；取 true 则有小概率拆出两颗一样的符石
	public static final boolean RANDOM_STONE_CAN_MATCH = false;

	public static boolean isActive(){
		//配方图鉴在标题界面也能打开，那时 Dungeon.challenges 还留着上一局或刚看过的排行榜记录的值，
		//所以要先确认当前确实有一局游戏
		return Dungeon.hero != null && Dungeon.isChallenged(Challenges.ALCHEMY_APPRENTICE);
	}

	//配方自身的基础消耗经过本挑战之后的数值。
	//加价算在配方本身上，所以它先于奇械师的减免和炼金工具箱的代付结算
	public static int recipeCost( int baseCost ){
		if (!isActive()) return baseCost;
		if (baseCost <= 0 && !CHARGE_FREE_RECIPES) return baseCost;
		return baseCost + EXTRA_ENERGY_COST;
	}

	//把这件物品转化为炼金能量时是否不再顺带鉴定它。
	//设计原文只提到药水与卷轴（合剂、秘卷也是它们的子类），其它物品照旧
	public static boolean blocksEnergizeIdentify( Item item ){
		return isActive() && (item instanceof Potion || item instanceof Scroll);
	}

	//把拆解卷轴本该得到的那叠符石里的一颗换成随机种类，其余不动
	public static Item replaceOneStone( Runestone stones ){
		Runestone replacement = randomStone(stones.getClass());
		stones.quantity(stones.quantity() - 1);

		Recipe.MultiOutput result = new Recipe.MultiOutput();
		//两种符石谁先谁后是随机的：否则产出格和背包里的排列顺序会直接说明哪一颗才是卷轴对应的符石，
		//等于变相鉴定了卷轴
		if (Random.Int(2) == 0){
			result.add(stones).add(replacement);
		} else {
			result.add(replacement).add(stones);
		}
		return result;
	}

	//种类权重沿用地牢里随机符石的默认权重（附魔符石与强化符石的权重是 0，不会被随机到）。
	//不走 Generator.random()：它会消耗影响后续物品生成的牌堆；这里只用当前的随机数发生器按权重抽一次
	private static Runestone randomStone( Class<? extends Runestone> original ){
		Class<?>[] classes = Generator.Category.STONE.classes;
		float[] probs = Generator.Category.STONE.defaultProbs.clone();
		if (!RANDOM_STONE_CAN_MATCH){
			for (int i = 0; i < classes.length; i++){
				if (classes[i] == original) probs[i] = 0;
			}
		}

		int index = Random.chances(probs);
		//没有任何可换的种类时保持原样
		if (index == -1) return Reflection.newInstance(original);
		return (Runestone) Reflection.newInstance(classes[index]);
	}

	//炼金指南的符石页和能量页，原文写着拆解、转化会顺带鉴定物品，开启挑战后不再成立。
	//指南界面会把这里返回的说明补在该页正文后面；不需要补充的页返回 null
	public static String guideNote( int pageIdx ){
		if (!isActive()) return null;

		if (pageIdx == Document.ALCHEMY_GUIDE.pageIdx("Stones")){
			return Messages.get(Challenges.class, "alchemy_apprentice_guide_stones");
		} else if (pageIdx == Document.ALCHEMY_GUIDE.pageIdx("Energy_Food")){
			return Messages.get(Challenges.class, "alchemy_apprentice_guide_energy");
		}
		return null;
	}

	//只用于展示的预览物品。拆解的产物是「对应的符石加一颗随机符石」，而一个产出格只能放一种物品，
	//所以借用对应符石的外观和名字，把数量角标写成「1+?」，详情里再用文字说明
	public static class StonePreview extends Runestone {

		private final Runestone stones;

		//参数是不开挑战时预览里本该显示的那叠符石；卷轴未鉴定时它是占位符石，不会泄露种类
		public StonePreview( Runestone stones ){
			this.stones = stones;
			image = stones.image;
		}

		@Override
		protected void activate( int cell ) {
			//仅用于展示，不会被使用
		}

		//这条线上 Item.trueName() 是 final，不能覆写；预览物品只在炼金界面的产出格里出现，
		//那里只判断 trueName() 非空，显示用的都是 name()，所以覆写 name() 就够了
		@Override
		public String name() {
			return stones.name();
		}

		@Override
		public String status() {
			return (stones.quantity() - 1) + "+?";
		}

		@Override
		public String desc() {
			return stones.desc();
		}

		@Override
		public String info() {
			String info = Messages.get(Challenges.class, "alchemy_apprentice_stone_preview", stones.quantity() - 1, stones.name());
			//占位符石没有说明文字
			String stoneInfo = stones.info();
			if (!stoneInfo.isEmpty()){
				info += "\n\n" + stoneInfo;
			}
			return info;
		}
	}

}
