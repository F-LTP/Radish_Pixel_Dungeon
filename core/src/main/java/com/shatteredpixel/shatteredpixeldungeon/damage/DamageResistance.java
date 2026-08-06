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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 伤害抗性管理类
 * 
 * 用于计算和存储角色对各种伤害类型的抗性。
 * 抗性值范围：0.0 = 无抗性，0.5 = 50%减免，1.0 = 完全免疫
 * 
 * 注意：此类仅存储抗性数据，不负责从Char读取抗性来源。
 * 抗性来源的整合应在Char.damage()方法中处理。
 */
public class DamageResistance {
	
	/** 各类型抗性值 */
	private Map<DamageType, Float> resistances = new HashMap<>();
	
	/** 完全免疫的类型 */
	private Set<DamageType> immunities = new HashSet<>();
	
	// ========== 基本操作 ==========
	
	/** 设置抗性值 */
	public void setResistance(DamageType type, float value) {
		value = Math.max(0f, Math.min(1f, value)); // 限制在 0-1 范围
		resistances.put(type, value);
	}
	
	/** 设置免疫 */
	public void setImmunity(DamageType type, boolean immune) {
		if (immune) {
			immunities.add(type);
		} else {
			immunities.remove(type);
		}
	}
	
	/** 获取抗性值 */
	public float getResistance(DamageType type) {
		if (isImmune(type)) {
			return 1.0f; // 免疫视为100%抗性
		}
		return resistances.getOrDefault(type, 0f);
	}
	
	/** 是否免疫 */
	public boolean isImmune(DamageType type) {
		return immunities.contains(type);
	}
	
	// ========== 伤害计算 ==========
	
	/**
	 * 计算最终伤害（应用抗性减免）
	 * 
	 * @param baseDamage 基础伤害值
	 * @param info 伤害信息
	 * @return 减免后的伤害值
	 */
	public int calculateDamage(int baseDamage, DamageInfo info) {
		// 真实伤害无视抗性
		if (info.isTrueDamage()) {
			return baseDamage;
		}
		
		// 完全免疫
		if (isImmune(info.getType())) {
			return 0;
		}
		
		// 应用抗性
		float resistance = getResistance(info.getType());
		if (resistance <= 0f) {
			return baseDamage;
		}
		
		return Math.round(baseDamage * (1f - resistance));
	}
	
	/**
	 * 计算伤害减免百分比
	 */
	public float getReductionPercent(DamageInfo info) {
		if (info.isTrueDamage() || isImmune(info.getType())) {
			return 0f;
		}
		return getResistance(info.getType()) * 100f;
	}
	
	// ========== 工具方法 ==========
	
	/** 清除所有抗性 */
	public void clear() {
		resistances.clear();
		immunities.clear();
	}
	
	/** 合并另一个抗性对象（抗性取最大值） */
	public void merge(DamageResistance other) {
		for (Map.Entry<DamageType, Float> entry : other.resistances.entrySet()) {
			float current = resistances.getOrDefault(entry.getKey(), 0f);
			// 抗性叠加：取最大值而非累加
			if (entry.getValue() > current) {
				resistances.put(entry.getKey(), entry.getValue());
			}
		}
		immunities.addAll(other.immunities);
	}
	
	/** 添加抗性值（累加后限制在0-1范围） */
	public void addResistance(DamageType type, float value) {
		float current = resistances.getOrDefault(type, 0f);
		float newValue = Math.max(0f, Math.min(1f, current + value));
		resistances.put(type, newValue);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("DamageResistance{");
		if (!resistances.isEmpty()) {
			sb.append("resistances=");
			for (Map.Entry<DamageType, Float> e : resistances.entrySet()) {
				sb.append(e.getKey().getId()).append(":").append(e.getValue() * 100).append("%, ");
			}
		}
		if (!immunities.isEmpty()) {
			sb.append("immunities=");
			for (DamageType t : immunities) {
				sb.append(t.getId()).append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}
}