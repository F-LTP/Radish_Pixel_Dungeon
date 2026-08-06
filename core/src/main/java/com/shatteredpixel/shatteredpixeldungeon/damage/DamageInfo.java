/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2026 TheCatist
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.damage;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 伤害信息包装类（伤害计算单元）
 * 
 * 包含一次伤害的所有信息：
 * - 基础伤害值（baseDamage）
 * - 伤害修正项列表（modifiers）
 * - 伤害类型
 * - 是否暴击（属性，而非类型）
 * - 攻击者、来源物品、来源对象
 * 
 * 核心计算公式：
 * 最终伤害 = floor(((基础伤害 + Σ直接加算) × Σ直接乘算) × Σ最终乘算 + Σ最终加算)
 * 
 * 乘算采用累乘：×1.5 × ×1.2 = ×1.98
 */
public class DamageInfo {
	
	// ========== 伤害值相关 ==========
	
	/** 基础伤害值 */
	private int baseDamage;
	
	/** 计算后的最终伤害（缓存） */
	private int cachedFinalDamage;
	
	/** 是否已计算 */
	private boolean calculated = false;
	
	// ========== Modifier列表 ==========
	
	/** 直接加算modifier列表 */
	private List<DamageModifier> flatAdditives = new ArrayList<>();
	
	/** 直接乘算modifier列表 */
	private List<DamageModifier> directMultiplicatives = new ArrayList<>();
	
	/** 最终乘算modifier列表 */
	private List<DamageModifier> finalMultiplicatives = new ArrayList<>();
	
	/** 最终加算modifier列表 */
	private List<DamageModifier> finalAdditives = new ArrayList<>();
	
	// ========== 元信息 ==========
	
	/** 伤害类型 */
	private DamageType type;
	
	/** 是否为暴击 */
	private boolean critical = false;
	
	/** 暴击倍率（默认1.5） */
	private float criticalMultiplier = 1.5f;
	
	/** 攻击者（可能为空） */
	private Char attacker;
	
	/** 来源物品（武器、法杖等，可能为空） */
	private Item sourceItem;
	
	/** 来源对象（Buff、Blob、Trap等，可能为空） */
	private Object source;
	
	// ========== 构造函数 ==========
	
	/** 最简构造：基础伤害 + 默认类型 */
	public DamageInfo(int baseDamage) {
		this(baseDamage, DamageType.UNKNOWN);
	}
	
	/** 基础构造：基础伤害 + 类型 */
	public DamageInfo(int baseDamage, DamageType type) {
		this.baseDamage = baseDamage;
		this.type = type;
	}
	
	/** 带攻击者构造 */
	public DamageInfo(int baseDamage, DamageType type, Char attacker) {
		this.baseDamage = baseDamage;
		this.type = type;
		this.attacker = attacker;
		this.source = attacker;
	}
	
	/** 全参数构造 */
	public DamageInfo(int baseDamage, DamageType type, Char attacker, Item sourceItem, Object source) {
		this.baseDamage = baseDamage;
		this.type = type;
		this.attacker = attacker;
		this.sourceItem = sourceItem;
		this.source = source;
	}
	
	// ========== 伤害计算 ==========
	
	/**
	 * 获取最终伤害值（应用所有modifier）
	 */
	public int getDamage() {
		if (!calculated) {
			cachedFinalDamage = calculateFinalDamage();
			calculated = true;
		}
		return cachedFinalDamage;
	}
	
	/**
	 * 获取基础伤害值（不含modifier）
	 */
	public int getBaseDamage() {
		return baseDamage;
	}
	
	/**
	 * 设置基础伤害值（重置计算）
	 */
	public void setBaseDamage(int baseDamage) {
		this.baseDamage = baseDamage;
		invalidateCache();
	}
	
	/**
	 * 核心计算方法
	 * 计算顺序：((基础 + 直接加算) × 直接乘算) × 最终乘算 + 最终加算
	 */
	private int calculateFinalDamage() {
		float result = baseDamage;
		
		// 阶段1：直接加算
		for (DamageModifier m : flatAdditives) {
			if (m.isActive()) {
				result += m.getValue();
			}
		}
		
		// 阶段2：直接乘算（累乘）
		for (DamageModifier m : directMultiplicatives) {
			if (m.isActive()) {
				result *= m.getValue();
			}
		}
		
		// 阶段3：最终乘算（累乘）
		for (DamageModifier m : finalMultiplicatives) {
			if (m.isActive()) {
				result *= m.getValue();
			}
		}
		
		// 阶段4：最终加算
		for (DamageModifier m : finalAdditives) {
			if (m.isActive()) {
				result += m.getValue();
			}
		}
		
		// 至少为0，不会出现负伤害
		return Math.max(0, Math.round(result));
	}
	
	/**
	 * 清除缓存（modifier变化时调用）
	 */
	private void invalidateCache() {
		calculated = false;
	}
	
	/**
	 * 强制重新计算
	 */
	public void recalculate() {
		invalidateCache();
		getDamage();
	}
	
	// ========== Modifier管理（链式调用） ==========
	
	/**
	 * 添加直接加算modifier
	 * @param value 加算值（如 +10）
	 * @param source 来源描述
	 */
	public DamageInfo addFlatModifier(float value, String source) {
		flatAdditives.add(DamageModifier.flatAdd(value, source));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加直接加算modifier（带来源对象）
	 */
	public DamageInfo addFlatModifier(float value, String source, Object sourceObject) {
		flatAdditives.add(DamageModifier.flatAdd(value, source, sourceObject));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加直接乘算modifier
	 * @param value 乘算值（如 1.5 表示 +50%）
	 * @param source 来源描述
	 */
	public DamageInfo addDirectMultModifier(float value, String source) {
		directMultiplicatives.add(DamageModifier.directMult(value, source));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加直接乘算modifier（带来源对象）
	 */
	public DamageInfo addDirectMultModifier(float value, String source, Object sourceObject) {
		directMultiplicatives.add(DamageModifier.directMult(value, source, sourceObject));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加最终乘算modifier
	 * @param value 乘算值（如 1.2 表示 +20%）
	 * @param source 来源描述
	 */
	public DamageInfo addFinalMultModifier(float value, String source) {
		finalMultiplicatives.add(DamageModifier.finalMult(value, source));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加最终乘算modifier（带来源对象）
	 */
	public DamageInfo addFinalMultModifier(float value, String source, Object sourceObject) {
		finalMultiplicatives.add(DamageModifier.finalMult(value, source, sourceObject));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加最终加算modifier
	 * @param value 加算值（如 +50）
	 * @param source 来源描述
	 */
	public DamageInfo addFinalAddModifier(float value, String source) {
		finalAdditives.add(DamageModifier.finalAdd(value, source));
		invalidateCache();
		return this;
	}
	
	/**
	 * 添加最终加算modifier（带来源对象）
	 */
	public DamageInfo addFinalAddModifier(float value, String source, Object sourceObject) {
		finalAdditives.add(DamageModifier.finalAdd(value, source, sourceObject));
		invalidateCache();
		return this;
	}
	
	/**
	 * 直接添加DamageModifier对象
	 */
	public DamageInfo addModifier(DamageModifier modifier) {
		switch (modifier.getType()) {
			case FLAT_ADDITIVE:
				flatAdditives.add(modifier);
				break;
			case DIRECT_MULTIPLICATIVE:
				directMultiplicatives.add(modifier);
				break;
			case FINAL_MULTIPLICATIVE:
				finalMultiplicatives.add(modifier);
				break;
			case FINAL_ADDITIVE:
				finalAdditives.add(modifier);
				break;
		}
		invalidateCache();
		return this;
	}
	
	/**
	 * 清除所有modifier
	 */
	public DamageInfo clearModifiers() {
		flatAdditives.clear();
		directMultiplicatives.clear();
		finalMultiplicatives.clear();
		finalAdditives.clear();
		critical = false;
		invalidateCache();
		return this;
	}
	
	// ========== 暴击处理 ==========
	
	/**
	 * 设置暴击（自动添加暴击乘算modifier）
	 * @param critical 是否暴击
	 */
	public DamageInfo setCritical(boolean critical) {
		if (critical && !this.critical) {
			// 添加暴击乘算modifier
			addDirectMultModifier(criticalMultiplier, "暴击");
		} else if (!critical && this.critical) {
			// 移除暴击modifier（移除"暴击"来源的modifier）
			directMultiplicatives.removeIf(m -> m.getSource().equals("暴击"));
			invalidateCache();
		}
		this.critical = critical;
		return this;
	}
	
	/**
	 * 设置暴击并指定倍率
	 */
	public DamageInfo setCritical(boolean critical, float multiplier) {
		this.criticalMultiplier = multiplier;
		return setCritical(critical);
	}
	
	public boolean isCritical() {
		return critical;
	}
	
	public float getCriticalMultiplier() {
		return criticalMultiplier;
	}
	
	public void setCriticalMultiplier(float multiplier) {
		this.criticalMultiplier = multiplier;
	}
	
	// ========== Getter/Setter ==========
	
	public DamageType getType() {
		return type;
	}
	
	public void setType(DamageType type) {
		this.type = type;
	}
	
	public Char getAttacker() {
		return attacker;
	}
	
	public void setAttacker(Char attacker) {
		this.attacker = attacker;
	}
	
	public Item getSourceItem() {
		return sourceItem;
	}
	
	public void setSourceItem(Item sourceItem) {
		this.sourceItem = sourceItem;
	}
	
	public Object getSource() {
		return source;
	}
	
	public void setSource(Object source) {
		this.source = source;
	}
	
	// ========== 类型便捷方法 ==========
	
	public boolean isPhysical() {
		return type.isPhysical();
	}
	
	public boolean isMagical() {
		return type.isMagical();
	}
	
	public boolean isElemental() {
		return type.isElemental();
	}
	
	public boolean isDoT() {
		return type.isDoT();
	}
	
	public boolean ignoresArmor() {
		return type.ignoresArmor();
	}
	
	public boolean isTrueDamage() {
		return type.isTrueDamage();
	}
	
	/**
	 * 获取浮动文字图标（考虑暴击）
	 */
	public int getFloatingTextIcon() {
		if (critical) {
			if (ignoresArmor()) {
				return FloatingText.CRIT_NO_BLOCK;
			} else {
				return FloatingText.CRIT;
			}
		}
		return type.getFloatingTextIcon();
	}
	
	// ========== Modifier列表访问 ==========
	
	public List<DamageModifier> getFlatAdditives() {
		return new ArrayList<>(flatAdditives);
	}
	
	public List<DamageModifier> getDirectMultiplicatives() {
		return new ArrayList<>(directMultiplicatives);
	}
	
	public List<DamageModifier> getFinalMultiplicatives() {
		return new ArrayList<>(finalMultiplicatives);
	}
	
	public List<DamageModifier> getFinalAdditives() {
		return new ArrayList<>(finalAdditives);
	}
	
	/**
	 * 获取所有modifier
	 */
	public List<DamageModifier> getAllModifiers() {
		List<DamageModifier> all = new ArrayList<>();
		all.addAll(flatAdditives);
		all.addAll(directMultiplicatives);
		all.addAll(finalMultiplicatives);
		all.addAll(finalAdditives);
		return all;
	}
	
	// ========== 工厂方法 ==========
	
	/** 创建物理伤害 */
	public static DamageInfo physical(int baseDamage, Char attacker) {
		return new DamageInfo(baseDamage, DamageType.PHYSICAL, attacker);
	}
	
	/** 创建物理伤害（带武器） */
	public static DamageInfo physical(int baseDamage, Char attacker, Item weapon) {
		DamageInfo info = new DamageInfo(baseDamage, DamageType.PHYSICAL, attacker, weapon, weapon);
		return info;
	}
	
	/** 创建无视护甲的物理伤害 */
	public static DamageInfo physicalNoArmor(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.PHYSICAL_NO_ARMOR, null, null, source);
	}
	
	/** 创建魔法伤害 */
	public static DamageInfo magical(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.MAGICAL, null, null, source);
	}
	
	/** 创建火焰伤害 */
	public static DamageInfo fire(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.FIRE, null, null, source);
	}
	
	/** 创建闪电伤害 */
	public static DamageInfo lightning(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.LIGHTNING, null, null, source);
	}
	
	/** 创建冰霜伤害 */
	public static DamageInfo frost(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.FROST, null, null, source);
	}
	
	/** 创建毒素伤害 */
	public static DamageInfo poison(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.POISON, null, null, source);
	}
	
	/** 创建腐蚀伤害 */
	public static DamageInfo corrosive(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.CORROSIVE, null, null, source);
	}
	
	/** 创建流血伤害 */
	public static DamageInfo bleeding(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.BLEEDING, null, null, source);
	}
	
	/** 创建粘液伤害 */
	public static DamageInfo ooze(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.OOZE, null, null, source);
	}
	
	/** 创建燃烧状态伤害 */
	public static DamageInfo burningStatus(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.BURNING_STATUS, null, null, source);
	}
	
	/** 创建真实伤害 */
	public static DamageInfo trueDamage(int baseDamage) {
		return new DamageInfo(baseDamage, DamageType.TRUE);
	}
	
	/** 创建真实伤害（带来源） */
	public static DamageInfo trueDamage(int baseDamage, Object source) {
		return new DamageInfo(baseDamage, DamageType.TRUE, null, null, source);
	}
	
	/** 创建饥饿伤害 */
	public static DamageInfo hunger(int baseDamage) {
		return new DamageInfo(baseDamage, DamageType.HUNGER);
	}
	
	/** 创建坠落伤害 */
	public static DamageInfo fall(int baseDamage) {
		return new DamageInfo(baseDamage, DamageType.FALL);
	}
	
	/** 创建深渊伤害 */
	public static DamageInfo chasm(int baseDamage) {
		return new DamageInfo(baseDamage, DamageType.CHASM);
	}
	
	/** 从来源自动判断类型创建 */
	public static DamageInfo fromSource(int baseDamage, Object source) {
		DamageType type = DamageType.fromSource(source);
		return new DamageInfo(baseDamage, type, null, null, source);
	}
	
	// ========== 调试工具 ==========
	
	/**
	 * 获取伤害计算过程描述
	 */
	public String getCalculationTrace() {
		StringBuilder sb = new StringBuilder();
		sb.append("伤害计算过程:\n");
		sb.append("  基础伤害: ").append(baseDamage).append("\n");
		
		if (!flatAdditives.isEmpty()) {
			sb.append("  + 直接加算:\n");
			for (DamageModifier m : flatAdditives) {
				if (m.isActive()) {
					sb.append("    ").append(m.getDescription()).append("\n");
				}
			}
		}
		
		if (!directMultiplicatives.isEmpty()) {
			sb.append("  × 直接乘算:\n");
			for (DamageModifier m : directMultiplicatives) {
				if (m.isActive()) {
					sb.append("    ").append(m.getDescription()).append("\n");
				}
			}
		}
		
		if (!finalMultiplicatives.isEmpty()) {
			sb.append("  × 最终乘算:\n");
			for (DamageModifier m : finalMultiplicatives) {
				if (m.isActive()) {
					sb.append("    ").append(m.getDescription()).append("\n");
				}
			}
		}
		
		if (!finalAdditives.isEmpty()) {
			sb.append("  + 最终加算:\n");
			for (DamageModifier m : finalAdditives) {
				if (m.isActive()) {
					sb.append("    ").append(m.getDescription()).append("\n");
				}
			}
		}
		
		sb.append("  = 最终伤害: ").append(getDamage());
		return sb.toString();
	}
	
	// ========== 复制方法 ==========
	
	/**
	 * 创建副本
	 */
	public DamageInfo copy() {
		DamageInfo copy = new DamageInfo(baseDamage, type, attacker, sourceItem, source);
		copy.critical = this.critical;
		copy.criticalMultiplier = this.criticalMultiplier;
		copy.cachedFinalDamage = this.cachedFinalDamage;
		copy.calculated = this.calculated;
		
		// 复制modifier列表
		for (DamageModifier m : flatAdditives) {
			copy.flatAdditives.add(m);
		}
		for (DamageModifier m : directMultiplicatives) {
			copy.directMultiplicatives.add(m);
		}
		for (DamageModifier m : finalMultiplicatives) {
			copy.finalMultiplicatives.add(m);
		}
		for (DamageModifier m : finalAdditives) {
			copy.finalAdditives.add(m);
		}
		
		return copy;
	}
	
	/**
	 * 创建副本并设置新基础伤害
	 */
	public DamageInfo withBaseDamage(int newBaseDamage) {
		DamageInfo copy = copy();
		copy.setBaseDamage(newBaseDamage);
		return copy;
	}
	
	/**
	 * 创建副本并设置暴击
	 */
	public DamageInfo withCritical(boolean isCritical) {
		DamageInfo copy = copy();
		copy.setCritical(isCritical);
		return copy;
	}
	
	// ========== toString ==========
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DamageInfo{base=").append(baseDamage);
		sb.append(", final=").append(getDamage());
		sb.append(", type=").append(type.getId());
		if (critical) {
			sb.append(", critical=true");
		}
		if (attacker != null) {
			sb.append(", attacker=").append(attacker.getClass().getSimpleName());
		}
		if (sourceItem != null) {
			sb.append(", item=").append(sourceItem.getClass().getSimpleName());
		}
		if (source != null) {
			sb.append(", source=").append(source.getClass().getSimpleName());
		}
		int modCount = flatAdditives.size() + directMultiplicatives.size() 
			+ finalMultiplicatives.size() + finalAdditives.size();
		if (modCount > 0) {
			sb.append(", modifiers=").append(modCount);
		}
		sb.append("}");
		return sb.toString();
	}
}