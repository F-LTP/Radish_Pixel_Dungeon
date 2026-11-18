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
 * 承伤修正钩子：实现者（通常是 Buff）对承伤值施加“原始”或“最终”两类修正。
 * 原始修正在抗性结算之前生效，最终修正在抗性之后、护盾之前生效。
 * 每类分为加算（固定值、百分比）与乘算：固定加算直接相加，百分比加算按 1 + Σ 叠加后一次乘算，
 * 乘算逐项累乘；dmg 为该相位开始时的承伤值。聚合与取整规则见 Char 的结算方法。
 * 与 DamageModifier（构建期用于 DamageInfo 的数值项）不同，本接口作用于管线中已算出的承伤值。
 * 修正若需伴随副作用（如黏稠刻印把延迟量挂成 DoT），由实现者的 Buff.onDamageTaken 完成。
 */
public interface IDamageModifier {

	// —— 原始伤害（抗性之前）——

	default float rawAddFlat(int dmg, DamageInfo info) { return 0f; }

	default float rawAddPercent(int dmg, DamageInfo info) { return 0f; }

	default float rawMultiply(int dmg, DamageInfo info) { return 1f; }

	// —— 最终伤害（抗性之后）——

	default float finalAddFlat(int dmg, DamageInfo info) { return 0f; }

	default float finalAddPercent(int dmg, DamageInfo info) { return 0f; }

	default float finalMultiply(int dmg, DamageInfo info) { return 1f; }
}
