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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * 伤害来源链（DamageInfo.causeChain）的文本格式化工具。
 *
 * 用于把「有序因果对象列表」渲染成人类可读的描述，例如：
 *   [玩家, 玩家武器, 烈焰附魔, 怪物身上的火, 草, 门]
 *   → 玩家自己的烈焰附魔武器点燃的火，经由草和门烧死了目标。
 *
 * 底层机制与展示分离：任何系统只要把因果对象按顺序塞进
 * {@link DamageInfo#addCause(Object)}，即可用本工具生成死亡/伤害来源文案。
 */
public class DamageCauseFormatter {

	private DamageCauseFormatter() {}

	/**
	 * 生成单个因果对象的可读名字。
	 * 支持：Char、Item、Buff、普通对象（取类名）、null。
	 */
	public static String nameOf(Object cause) {
		if (cause == null) {
			return Messages.get(DamageCauseFormatter.class, "unknown");
		}
		if (cause instanceof Char) {
			return Messages.titleCase(((Char) cause).name());
		}
		if (cause instanceof Item) {
			return ((Item) cause).title();
		}
		if (cause instanceof Buff) {
			return Messages.titleCase(((Buff) cause).name());
		}
		//普通对象：用类名做 fallback
		return Messages.titleCase(Messages.get(cause.getClass(), "name"));
	}

	/**
	 * 把整条来源链渲染成一句话（各元素用箭头连接）。
	 * 例如 [玩家, 玩家武器, 烈焰附魔, 火, 草, 门]
	 *   → "玩家 → 玩家的武器 → 烈焰附魔 → 火 → 草 → 门"
	 * @param chain 有序因果对象（可为 null/空）
	 */
	public static String describeChain(List<?> chain) {
		List<Object> list = chain == null ? new ArrayList<>() : new ArrayList<>(chain);
		if (list.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		String join = Messages.get(DamageCauseFormatter.class, "join");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append(join);
			sb.append(nameOf(list.get(i)));
		}
		return sb.toString();
	}
}
