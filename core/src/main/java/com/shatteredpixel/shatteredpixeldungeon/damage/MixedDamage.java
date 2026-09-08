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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.damage;

import java.util.ArrayList;
import java.util.List;

/**
 * 混合伤害：由多种 DamageType 按百分比构成。
 *
 * 两个平行列表：
 *  - types：参与混合的伤害类型（不含 MIXED / UNKNOWN / TRUE 等整体类型）
 *  - percentages：各类型所占比例，和为 1（否则断言失败）。
 *
 * 减免时按各成分分别判定免疫/抗性后加权求和；
 * 跳字时按占比从高到低在跳字左侧显示多个图标。
 *
 * 用法：
 *   MixedDamage m = new MixedDamage();
 *   m.add(DamageType.FIRE, 0.5f).add(DamageType.PHYSICAL, 0.5f);
 *   info.setMixedDamage(m);  // 并把 info.type 设为 DamageType.MIXED
 */
public class MixedDamage {

	private final List<DamageType> types = new ArrayList<>();
	private final List<Float> percentages = new ArrayList<>();

	/** 追加一个成分，占比 p 需在 [0,1]。 */
	public MixedDamage add(DamageType type, float p) {
		if (type == null) throw new IllegalArgumentException("Mixed damage type cannot be null");
		if (type == DamageType.MIXED || type == DamageType.UNKNOWN || type == DamageType.TRUE) {
			throw new IllegalArgumentException("Cannot mix overall type: " + type);
		}
		if (p < 0f || p > 1f) {
			throw new IllegalArgumentException("Mixed damage percentage must be within [0,1], got " + p);
		}
		types.add(type);
		percentages.add(p);
		return this;
	}

	/** 校验所有占比之和为 1，否则抛异常（构造完成后调用一次即可）。 */
	public MixedDamage validate() {
		float sum = 0f;
		for (float p : percentages) sum += p;
		if (Math.abs(sum - 1f) > 0.0001f) {
			throw new IllegalArgumentException("Mixed damage percentages must sum to 1, got " + sum);
		}
		return this;
	}

	public List<DamageType> types() {
		return new ArrayList<>(types);
	}

	public List<Float> percentages() {
		return new ArrayList<>(percentages);
	}

	public int size() {
		return types.size();
	}

	public DamageType typeAt(int i) {
		return types.get(i);
	}

	public float percentAt(int i) {
		return percentages.get(i);
	}

	public boolean isEmpty() {
		return types.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MixedDamage{");
		for (int i = 0; i < types.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(types.get(i).getId()).append(":").append(Math.round(percentages.get(i) * 100f)).append("%");
		}
		return sb.append("}").toString();
	}
}
