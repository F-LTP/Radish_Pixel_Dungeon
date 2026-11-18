package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 红包属性测试入口：只在红包挑战里显示。
 * 直接给对应属性，方便逐项验证。
 */
public class WndHongbaoDebug extends WndOptions {

	public WndHongbaoDebug() {
		super("红包属性测试", "直接获得指定属性，用于测试：",
				"戒指栏位 +1",
				"法杖不消耗充能概率 +10%",
				"连击次数 +1",
				"攻击距离 +1",
				"投掷物返回概率 +10%",
				"附魔触发与效果 +10%",
				"全部属性各提升一次");
	}

	@Override
	protected void onSelect(int index) {
		if (Dungeon.hero == null) return;
		EnvelopeBoost boost = Buff.affect(Dungeon.hero, EnvelopeBoost.class);
		switch (index) {
			case 0:
				Dungeon.hero.belongings.growExtraRingSlots();
				InventoryPane.refresh();
				GLog.p("测试：戒指栏位 +1");
				break;
			case 1:
				boost.addWandFreeChance(0.10f);
				GLog.p("测试：法杖不消耗充能概率 " + Math.round(boost.wandFreeChance() * 100) + "%");
				break;
			case 2:
				boost.addComboHits(1);
				GLog.p("测试：连击次数 +" + boost.comboHits());
				break;
			case 3:
				boost.addReachBonus(1);
				GLog.p("测试：攻击距离 +" + boost.reachBonus());
				break;
			case 4:
				boost.addMissileReturnChance(0.10f);
				GLog.p("测试：投掷物返回概率 " + Math.round(boost.missileReturnChance() * 100) + "%");
				break;
			case 5:
				boost.addEnchantPower(0.10f);
				GLog.p("测试：附魔触发与效果 +" + Math.round(boost.enchantPower() * 100) + "%");
				break;
			case 6:
				Dungeon.hero.belongings.growExtraRingSlots();
				boost.addWandFreeChance(0.10f);
				boost.addComboHits(1);
				boost.addReachBonus(1);
				boost.addMissileReturnChance(0.10f);
				boost.addEnchantPower(0.10f);
				InventoryPane.refresh();
				GLog.p("测试：全部红包属性各提升一次");
				break;
		}
	}
}
