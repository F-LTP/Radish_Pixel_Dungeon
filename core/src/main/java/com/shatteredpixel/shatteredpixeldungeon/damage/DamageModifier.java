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

/**
 * 伤害修正项（Damage Modifier）
 * 
 * 表示单个伤害修正项，包含修正类型、数值和来源描述。
 * 用于追踪伤害计算过程，便于调试和日志记录。
 */
public class DamageModifier {
	
	/**
	 * Modifier类型枚举
	 * 
	 * 计算顺序：((基础 + 直接加算) × 直接乘算) × 最终乘算 + 最终加算
	 */
	public enum Type {
		/** 直接加算 - 在乘算前添加 */
		FLAT_ADDITIVE,
		
		/** 直接乘算 - 对加算后的结果乘算 */
		DIRECT_MULTIPLICATIVE,
		
		/** 最终乘算 - 在最终加算前乘算 */
		FINAL_MULTIPLICATIVE,
		
		/** 最终加算 - 最后添加固定值 */
		FINAL_ADDITIVE
	}
	
	/** Modifier类型 */
	private final Type type;
	
	/** Modifier数值 */
	private final float value;
	
	/** 来源描述（用于调试/日志/UI显示） */
	private final String source;
	
	/** 来源对象（可选，用于更精确追踪） */
	private final Object sourceObject;
	
	/** Modifier是否有效（可用于临时禁用） */
	private boolean active = true;
	
	// ========== 构造函数 ==========
	
	public DamageModifier(Type type, float value, String source) {
		this.type = type;
		this.value = value;
		this.source = source;
		this.sourceObject = null;
	}
	
	public DamageModifier(Type type, float value, String source, Object sourceObject) {
		this.type = type;
		this.value = value;
		this.source = source;
		this.sourceObject = sourceObject;
	}
	
	// ========== 静态工厂方法 ==========
	
	/** 创建直接加算modifier */
	public static DamageModifier flatAdd(float value, String source) {
		return new DamageModifier(Type.FLAT_ADDITIVE, value, source);
	}
	
	/** 创建直接加算modifier（带来源对象） */
	public static DamageModifier flatAdd(float value, String source, Object sourceObject) {
		return new DamageModifier(Type.FLAT_ADDITIVE, value, source, sourceObject);
	}
	
	/** 创建直接乘算modifier */
	public static DamageModifier directMult(float value, String source) {
		return new DamageModifier(Type.DIRECT_MULTIPLICATIVE, value, source);
	}
	
	/** 创建直接乘算modifier（带来源对象） */
	public static DamageModifier directMult(float value, String source, Object sourceObject) {
		return new DamageModifier(Type.DIRECT_MULTIPLICATIVE, value, source, sourceObject);
	}
	
	/** 创建最终乘算modifier */
	public static DamageModifier finalMult(float value, String source) {
		return new DamageModifier(Type.FINAL_MULTIPLICATIVE, value, source);
	}
	
	/** 创建最终乘算modifier（带来源对象） */
	public static DamageModifier finalMult(float value, String source, Object sourceObject) {
		return new DamageModifier(Type.FINAL_MULTIPLICATIVE, value, source, sourceObject);
	}
	
	/** 创建最终加算modifier */
	public static DamageModifier finalAdd(float value, String source) {
		return new DamageModifier(Type.FINAL_ADDITIVE, value, source);
	}
	
	/** 创建最终加算modifier（带来源对象） */
	public static DamageModifier finalAdd(float value, String source, Object sourceObject) {
		return new DamageModifier(Type.FINAL_ADDITIVE, value, source, sourceObject);
	}
	
	// ========== Getters ==========
	
	public Type getType() {
		return type;
	}
	
	public float getValue() {
		return value;
	}
	
	public String getSource() {
		return source;
	}
	
	public Object getSourceObject() {
		return sourceObject;
	}
	
	public boolean isActive() {
		return active;
	}
	
	// ========== Setter ==========
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	// ========== 工具方法 ==========
	
	/** 获取格式化描述 */
	public String getDescription() {
		String prefix = "";
		switch (type) {
			case FLAT_ADDITIVE:
				prefix = "+";
				break;
			case DIRECT_MULTIPLICATIVE:
			case FINAL_MULTIPLICATIVE:
				prefix = "×";
				break;
			case FINAL_ADDITIVE:
				prefix = "+";
				break;
		}
		return prefix + value + " (" + source + ")";
	}
	
	@Override
	public String toString() {
		return "DamageModifier{" + getDescription() + ", active=" + active + "}";
	}
}