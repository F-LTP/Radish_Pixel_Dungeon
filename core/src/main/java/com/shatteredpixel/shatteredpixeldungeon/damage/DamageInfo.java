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
import java.util.Collection;
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
 * 最终伤害 = floor((((基础伤害 + Σ直接加算) × Σ直接乘算) × 暴击倍率 + Σ最终前加算) × Σ最终乘算 + Σ最终加算)
 * 
 * 乘算采用累乘：×1.5 × ×1.2 = ×1.98
 */
public class DamageInfo {
	
	// ========== 伤害值相关 ==========
	
	/** 基础伤害值 */
	private int baseDamage;
	
	// ========== Modifier列表 ==========
	
	/** 直接加算modifier列表 */
	private List<DamageModifier> flatAdditives = new ArrayList<>();
	
	/** 直接乘算modifier列表 */
	private List<DamageModifier> directMultiplicatives = new ArrayList<>();
	
	/** 叠加乘区modifier列表（百分比累加后一次乘算） */
	private List<DamageModifier> stackMultiplicatives = new ArrayList<>();
	
	/** 最终乘算modifier列表 */
	private List<DamageModifier> finalMultiplicatives = new ArrayList<>();

	/** 最终乘算前加算modifier列表 */
	private List<DamageModifier> preFinalAdditives = new ArrayList<>();

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

	/** 伤害来源链：有序的因果对象列表，按「引发→中间传导→最终」顺序排列。
	 *  例如 [玩家, 玩家武器, 烈焰附魔, 怪物身上的火, 草, 门]。
	 *  链会随伤害/火焰/流血等传导机制逐层传递，用于死亡信息与来源追踪。 */
	private List<Object> causeChain = new ArrayList<>();

	/** 混合伤害成分（type == DamageType.MIXED 时生效），可为 null。 */
	private MixedDamage mixed;

	// ========== 构造函数 ==========
	
	/** 最简构造：基础伤害 + 默认类型 */
	public DamageInfo(int baseDamage) {
		this(baseDamage, DamageType.UNKNOWN);
	}
	
	/** 基础构造：基础伤害 + 类型 */
	public DamageInfo(int baseDamage, DamageType type) {
		this.baseDamage = baseDamage;
		this.type = type == null ? DamageType.UNKNOWN : type;
		this.source = this.type;
	}
	
	/** 带攻击者构造 */
	public DamageInfo(int baseDamage, DamageType type, Char attacker) {
		this.baseDamage = baseDamage;
		this.type = type == null ? DamageType.UNKNOWN : type;
		this.attacker = attacker;
		this.source = attacker == null ? this.type : attacker;
	}
	
	/** 全参数构造 */
	public DamageInfo(int baseDamage, DamageType type, Char attacker, Item sourceItem, Object source) {
		this.baseDamage = baseDamage;
		this.type = type == null ? DamageType.UNKNOWN : type;
		this.attacker = attacker;
		this.sourceItem = sourceItem;
		this.source = source == null ? this.type : source;
	}
	
	// ========== 伤害计算 ==========
	
	/**
	 * 获取最终伤害值（应用所有modifier）
	 */
	public int getDamage() {
		return calculateFinalDamage();
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
	}
	
	/**
	 * 核心计算方法
	 * 计算顺序：(((基础 + 直接加算) × 直接乘算) × 暴击倍率 + 最终前加算) × 最终乘算 + 最终加算
	 */
	private int calculateFinalDamage() {
		float result = baseDamage;
		
		// 阶段1：直接加算
		for (DamageModifier m : flatAdditives) {
			result += m.getValue();
		}
		
		// 阶段2：叠加乘区（百分比累加后一次乘算）
		// 例如 +50% 与 +50% 叠加 = 1 + 0.5 + 0.5 = ×2.0
		float stack = 0f;
		for (DamageModifier m : stackMultiplicatives) {
			stack += (m.getValue() - 1f);
		}
		result *= (1f + stack);

		// 阶段3：直接乘算（依次乘算）
		for (DamageModifier m : directMultiplicatives) {
			result *= m.getValue();
		}

		// 阶段3：暴击倍率（独立于modifier列表，便于UI和旧source兼容）
		if (critical) {
			result *= criticalMultiplier;
		}

		// 阶段4：最终乘算前加算
		for (DamageModifier m : preFinalAdditives) {
			result += m.getValue();
		}

		// 阶段5：最终乘算（累乘）
		for (DamageModifier m : finalMultiplicatives) {
			result *= m.getValue();
		}

		// 阶段6：最终加算
		for (DamageModifier m : finalAdditives) {
			result += m.getValue();
		}
		
		// 至少为0，不会出现负伤害
		return Math.max(0, Math.round(result));
	}
	
	// ========== Modifier管理（链式调用） ==========
	
	/**
	 * 添加直接加算modifier
	 * @param value 加算值（如 +10）
	 * @param source 来源描述
	 */
	public DamageInfo addFlatModifier(float value, String source) {
		flatAdditives.add(DamageModifier.flatAdd(value, source));
		return this;
	}
	
	/**
	 * 添加直接加算modifier（带来源对象）
	 */
	public DamageInfo addFlatModifier(float value, String source, Object sourceObject) {
		flatAdditives.add(DamageModifier.flatAdd(value, source, sourceObject));
		return this;
	}
	
	/**
	 * 添加直接乘算modifier
	 * @param value 乘算值（如 1.5 表示 +50%）
	 * @param source 来源描述
	 */
	public DamageInfo addDirectMultModifier(float value, String source) {
		directMultiplicatives.add(DamageModifier.directMult(value, source));
		return this;
	}
	
	/**
	 * 添加直接乘算modifier（带来源对象）
	 */
	public DamageInfo addDirectMultModifier(float value, String source, Object sourceObject) {
		directMultiplicatives.add(DamageModifier.directMult(value, source, sourceObject));
		return this;
	}

	/**
	 * 添加叠加乘区modifier
	 * @param value 乘算值（如 1.5 表示 +50%），同一乘区内多个值按百分比累加
	 * @param source 来源描述
	 */
	public DamageInfo addStackMultModifier(float value, String source) {
		stackMultiplicatives.add(DamageModifier.stackMult(value, source));
		return this;
	}

	/**
	 * 添加叠加乘区modifier（带来源对象）
	 */
	public DamageInfo addStackMultModifier(float value, String source, Object sourceObject) {
		stackMultiplicatives.add(DamageModifier.stackMult(value, source, sourceObject));
		return this;
	}
	
	/**
	 * 添加最终乘算modifier
	 * @param value 乘算值（如 1.2 表示 +20%）
	 * @param source 来源描述
	 */
	public DamageInfo addFinalMultModifier(float value, String source) {
		finalMultiplicatives.add(DamageModifier.finalMult(value, source));
		return this;
	}
	
	/**
	 * 添加最终乘算modifier（带来源对象）
	 */
	public DamageInfo addFinalMultModifier(float value, String source, Object sourceObject) {
		finalMultiplicatives.add(DamageModifier.finalMult(value, source, sourceObject));
		return this;
	}
	
	/**
	 * 添加最终加算modifier
	 * @param value 加算值（如 +50）
	 * @param source 来源描述
	 */
	public DamageInfo addPreFinalAddModifier(float value, String source) {
		preFinalAdditives.add(DamageModifier.preFinalAdd(value, source));
		return this;
	}

	/**
	 * 添加最终乘算前加算modifier（带来源对象）
	 */
	public DamageInfo addPreFinalAddModifier(float value, String source, Object sourceObject) {
		preFinalAdditives.add(DamageModifier.preFinalAdd(value, source, sourceObject));
		return this;
	}

	public DamageInfo addFinalAddModifier(float value, String source) {
		finalAdditives.add(DamageModifier.finalAdd(value, source));
		return this;
	}
	
	/**
	 * 添加最终加算modifier（带来源对象）
	 */
	public DamageInfo addFinalAddModifier(float value, String source, Object sourceObject) {
		finalAdditives.add(DamageModifier.finalAdd(value, source, sourceObject));
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
			case STACK_MULTIPLICATIVE:
				stackMultiplicatives.add(modifier);
				break;
			case FINAL_MULTIPLICATIVE:
				finalMultiplicatives.add(modifier);
				break;
			case PRE_FINAL_ADDITIVE:
				preFinalAdditives.add(modifier);
				break;
			case FINAL_ADDITIVE:
				finalAdditives.add(modifier);
				break;
		}
		return this;
	}
	
	/**
	 * 清除所有modifier
	 */
	public DamageInfo clearModifiers() {
		flatAdditives.clear();
		directMultiplicatives.clear();
		stackMultiplicatives.clear();
		finalMultiplicatives.clear();
		preFinalAdditives.clear();
		finalAdditives.clear();
		critical = false;
		return this;
	}
	
	// ========== 暴击处理 ==========
	
	/**
	 * 设置暴击（自动添加暴击乘算modifier）
	 * @param critical 是否暴击
	 */
	public DamageInfo setCritical(boolean critical) {
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
		this.type = type == null ? DamageType.UNKNOWN : type;
		if (source == null) source = this.type;
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
		this.source = source == null ? type : source;
	}

	// ========== 伤害来源链 ==========

	/** 追加一个因果对象到来源链末尾（如「烈焰附魔」「草」「门」）。 */
	public DamageInfo addCause(Object cause) {
		if (cause != null) {
			causeChain.add(cause);
		}
		return this;
	}

	/** 批量追加因果对象到来源链末尾，保持传入顺序。 */
	public DamageInfo addCauses(Collection<?> causes) {
		if (causes != null) {
			for (Object c : causes) {
				if (c != null) causeChain.add(c);
			}
		}
		return this;
	}

	/** 读取来源链副本（不可直接修改内部）。 */
	public List<Object> getCauseChain() {
		return new ArrayList<>(causeChain);
	}

	/** 设置整个来源链（覆盖）。 */
	public DamageInfo setCauseChain(Collection<?> chain) {
		causeChain.clear();
		if (chain != null) {
			for (Object c : chain) {
				if (c != null) causeChain.add(c);
			}
		}
		return this;
	}

	/** 是否有来源链。 */
	public boolean hasCauseChain() {
		return !causeChain.isEmpty();
	}

	// ========== 混合伤害 ==========

	public boolean isMixed() {
		return type == DamageType.MIXED;
	}

	public MixedDamage getMixed() {
		return mixed;
	}

	/** 设置混合伤害成分（并自动把 type 置为 MIXED）。 */
	public DamageInfo setMixedDamage(MixedDamage mixed) {
		if (mixed == null) throw new IllegalArgumentException("MixedDamage cannot be null");
		mixed.validate();
		this.mixed = mixed;
		this.type = DamageType.MIXED;
		return this;
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

	public boolean ignoresShields() {
		return type.ignoresShields();
	}

	public boolean isTrueDamage() {
		return type.isTrueDamage();
	}
	
	/**
	 * 获取浮动文字图标（考虑暴击）。仅用于单一伤害类型。
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

	/**
	 * 获取浮动文字图标列表（考虑暴击）。
	 * 混合伤害时按成分占比从高到低返回各成分图标；否则为单个元素列表。
	 */
	public int[] getFloatingTextIcons() {
		if (isMixed() && mixed != null) {
			// 按占比从高到低排序
			int n = mixed.size();
			Integer[] idx = new Integer[n];
			for (int i = 0; i < n; i++) idx[i] = i;
			java.util.Arrays.sort(idx, (a, b) ->
					Float.compare(mixed.percentAt(b), mixed.percentAt(a)));
			int[] icons = new int[n];
			for (int i = 0; i < n; i++) {
				icons[i] = critical
						? (mixed.typeAt(idx[i]).ignoresArmor() ? FloatingText.CRIT_NO_BLOCK : FloatingText.CRIT)
						: mixed.typeAt(idx[i]).getFloatingTextIcon();
			}
			return icons;
		}
		return new int[]{ getFloatingTextIcon() };
	}
	
	// ========== Modifier列表访问 ==========
	
	public List<DamageModifier> getFlatAdditives() {
		return new ArrayList<>(flatAdditives);
	}
	
	public List<DamageModifier> getDirectMultiplicatives() {
		return new ArrayList<>(directMultiplicatives);
	}

	public List<DamageModifier> getStackMultiplicatives() {
		return new ArrayList<>(stackMultiplicatives);
	}
	
	public List<DamageModifier> getFinalMultiplicatives() {
		return new ArrayList<>(finalMultiplicatives);
	}

	public List<DamageModifier> getPreFinalAdditives() {
		return new ArrayList<>(preFinalAdditives);
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
		all.addAll(stackMultiplicatives);
		all.addAll(preFinalAdditives);
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
	
	/**
	 * 通用构造：基础伤害 + 显式类型 + 攻击者 + 来源对象。
	 * 迁移 `damage(int, Object)` 调用点的标准入口。
	 */
	public static DamageInfo of(int baseDamage, DamageType type, Char attacker, Object source) {
		return new DamageInfo(baseDamage, type, attacker, null, source);
	}
	
	/**
	 * 按类型封装来源：将原本以任意 Object 表示的伤害来源归类到指定 DamageType，
	 * 便于用伤害类型统一推导死亡原因/文案。来源为 Char 时同时视为攻击者。
	 */
	public static DamageInfo of(int baseDamage, DamageType type, Object source) {
		Char attacker = source instanceof Char ? (Char) source : null;
		Item sourceItem = source instanceof Item ? (Item) source : null;
		return new DamageInfo(baseDamage, type, attacker, sourceItem, source);
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
				sb.append("    ").append(m.getDescription()).append("\n");
			}
		}
		
		if (!directMultiplicatives.isEmpty()) {
			sb.append("  × 直接乘算:\n");
			for (DamageModifier m : directMultiplicatives) {
				sb.append("    ").append(m.getDescription()).append("\n");
			}
		}

		if (!stackMultiplicatives.isEmpty()) {
			float stack = 0f;
			for (DamageModifier m : stackMultiplicatives) stack += (m.getValue() - 1f);
			sb.append("  × 叠加乘区: ×").append(1f + stack).append("\n");
			for (DamageModifier m : stackMultiplicatives) {
				sb.append("    ").append(m.getDescription()).append("\n");
			}
		}

		if (critical) {
			sb.append("  × 暴击: ×").append(criticalMultiplier).append("\n");
		}

		if (!preFinalAdditives.isEmpty()) {
			sb.append("  + 最终乘算前加算:\n");
			for (DamageModifier m : preFinalAdditives) {
				sb.append("    ").append(m.getDescription()).append("\n");
			}
		}

		if (!finalMultiplicatives.isEmpty()) {
			sb.append("  × 最终乘算:\n");
			for (DamageModifier m : finalMultiplicatives) {
				sb.append("    ").append(m.getDescription()).append("\n");
			}
		}
		
		if (!finalAdditives.isEmpty()) {
			sb.append("  + 最终加算:\n");
			for (DamageModifier m : finalAdditives) {
				sb.append("    ").append(m.getDescription()).append("\n");
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
		copy.setCauseChain(this.causeChain);
		if (this.mixed != null) {
			MixedDamage mc = new MixedDamage();
			for (int i = 0; i < this.mixed.size(); i++) {
				mc.add(this.mixed.typeAt(i), this.mixed.percentAt(i));
			}
			copy.mixed = mc;
		}

		// 复制modifier列表
		for (DamageModifier m : flatAdditives) {
			copy.flatAdditives.add(m);
		}
		for (DamageModifier m : directMultiplicatives) {
			copy.directMultiplicatives.add(m);
		}
		for (DamageModifier m : stackMultiplicatives) {
			copy.stackMultiplicatives.add(m);
		}
		for (DamageModifier m : finalMultiplicatives) {
			copy.finalMultiplicatives.add(m);
		}
		for (DamageModifier m : preFinalAdditives) {
			copy.preFinalAdditives.add(m);
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
			+ stackMultiplicatives.size() + finalMultiplicatives.size() + preFinalAdditives.size() + finalAdditives.size();
		if (modCount > 0) {
			sb.append(", modifiers=").append(modCount);
		}
		sb.append("}");
		return sb.toString();
	}
}
