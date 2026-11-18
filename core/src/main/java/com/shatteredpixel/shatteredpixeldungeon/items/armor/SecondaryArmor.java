/*
 * Radish Pixel Dungeon
 *
 * 次要护甲：穿在主要护甲之外的一件附加装备，只提供少量格挡，并附带一个特殊效果。
 * 它不是 Armor 的子类，所以刻印、附魔、强化符石的选择器都选不中它；能用升级卷轴强化。
 * 等级直接用 Item.level，存读档也走 Item 的 "level" 键：早期版本自己另存过一个同名键，
 * 老存档读进来落在同一个字段上，不需要额外兼容。
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.InfusedArmbandBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public abstract class SecondaryArmor extends EquipableItem {

	/** 格挡上下限随等级的加值，下标就是等级（+0 到 +8），超过 +8 按 +8 算 */
	private static final int[] GROWTH = {0, 0, 0, 1, 1, 1, 1, 1, 2};

	public int tier;
	public int blockMin;
	public int blockMax;
	public int strReq;

	protected SecondaryArmor(int tier, int blockMin, int blockMax, int strReq) {
		this.tier = tier;
		this.blockMin = blockMin;
		this.blockMax = blockMax;
		this.strReq = strReq;
	}

	public int growth() {
		return GROWTH[Math.min(Math.max(level, 0), GROWTH.length - 1)];
	}

	public int DRMin() {
		return blockMin + growth();
	}

	public int DRMax() {
		return blockMax + growth();
	}

	public int STRReq() {
		return strReq;
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	@Override
	public boolean isEquipped(Hero hero) {
		return hero.belongings.secondaryArmor() == this;
	}

	@Override
	public boolean doEquip(Hero hero) {
		// 先把自己从背包里取出来：背包满的时候，换下来的那件要靠这个空位才放得回去
		detach(hero.belongings.backpack);

		if (hero.belongings.secondaryArmor == null
				|| hero.belongings.secondaryArmor.doUnequip(hero, true, false)) {
			hero.belongings.secondaryArmor = this;
			return true;
		} else {
			collect(hero.belongings.backpack);
			return false;
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (hero.belongings.secondaryArmor == this) {
			hero.belongings.secondaryArmor = null;
		}
		if (collect) {
			// 丢失背包期间只有「被保留」的物品收得进背包，和基类一样临时标记一下，
			// 让被顶下来的那件先回到背包里，等找回背包后再出现
			boolean wasKept = keptThoughLostInvent;
			keptThoughLostInvent = true;
			if (!collect(hero.belongings.backpack)) {
				// 背包放不下就落在脚边，不能让护甲凭空消失
				Dungeon.level.drop(this, hero.pos).sprite.drop();
			}
			keptThoughLostInvent = wasKept;
		}
		return true;
	}

	/**
	 * 怪物受到任意来源的伤害、进入伤害管线之前，由 Mob.damage 调用一次。
	 * 攻击方一侧的修正钩子目前只有普通攻击会走，法杖、投掷物与持续伤害都不经过，
	 * 所以「不分来源」的次要护甲效果只能在受伤入口上挂修正器。
	 */
	public static void procMobDamage(Mob target, DamageInfo info) {
		Hero hero = Dungeon.hero;
		if (hero == null) return;

		// 灌能臂缠留下的增伤挂在英雄身上，换下臂缠后仍在持续，所以不看当前穿着。
		// 必须先于下面的护甲钩子结算：触发增伤的那一发法杖伤害自己吃不到加成。
		if (info.getAttacker() == hero) {
			InfusedArmbandBuff boost = hero.buff(InfusedArmbandBuff.class);
			if (boost != null) info.addPreFinalAddModifier(boost.bonus(), "infused armband bonus");
		}

		SecondaryArmor armor = hero.belongings.secondaryArmor();
		if (armor != null && hero.STR() >= armor.STRReq()) {
			armor.onMobDamaged(hero, target, info);
		}
	}

	/**
	 * 本件护甲对「怪物受到的一次伤害」的修正或触发，只在已装备且力量达标时调用。
	 * 伤害不一定来自英雄（陷阱、盟友、怪物互伤都会进来），要限定来源的子类自己判断 info.getAttacker()。
	 */
	protected void onMobDamaged(Hero hero, Mob target, DamageInfo info) {
	}

	/**
	 * 效果描述里要显示的当前等级数值，按顺序对应 effect_desc 里的 %d。
	 * 子类覆写；没有数值的子类保持空数组。
	 */
	public Object[] effectArgs() {
		return new Object[0];
	}

	// 次要护甲没有「穿一阵子就认出等级」的途径，等级不做隐藏，未鉴定时也照实显示
	@Override
	public int visiblyUpgraded() {
		return level;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return level;
	}

	@Override
	public String desc() {
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.get(this, "base_desc", tier, DRMin(), DRMax(), STRReq()));
		// 力量不足那段只在真的不满足力量需求时显示
		if (Dungeon.hero != null && Dungeon.hero.STR() < STRReq()) {
			sb.append("\n\n").append(Messages.get(this, "strength_penalty"));
		}
		sb.append("\n\n");
		sb.append(Messages.get(this, "flavor"));
		sb.append("\n\n");
		sb.append(Messages.get(this, "effect_desc", effectArgs()));
		if (level > 0) {
			sb.append("\n\n").append(Messages.get(this, "level_desc", level));
		}
		return sb.toString();
	}
}
