/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/**
 * 子职业基类（Base）- 每个子职业由一个定义类继承本类。
 *
 * <p>只承载子职业的<b>身份与行为</b>：名称/图标、T3/T4 天赋、战斗钩子。
 * 各子职业单例常量与查找统一由 {@link HeroSubClasses}（Manager）持有。</p>
 */
public abstract class HeroSubClass {

	private final String name;
	private final String icon;

	protected HeroSubClass(String name, String icon){
		this.name = name;
		this.icon = icon;
	}

	//各子职业提供的 T3/T4 天赋
	public abstract Talent[] subclassT3();
	public abstract Talent[] subclassT4();

	/**
	 * 攻击命中后的子职业钩子。
	 */
	public void onAttackProc(Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy) { }

	/**
	 * 受到伤害时的子职业钩子（护甲 proc 之后调用）。
	 */
	public void onDefenseProc(Hero hero, Char enemy, int damage) { }

	/**
	 * 移动一步时的子职业钩子。
	 */
	public void onMove(Hero hero) { }

	/**
	 * 计算攻击回合消耗倍率。默认 1.0。
	 */
	public float attackDelayMultiplier(Hero hero, Char enemy, boolean surpriseAttack) { return 1f; }

	public String name() { return name; }

	public String title() {
		return Messages.get(HeroSubClass.class, name());
	}

	public String super_desc() {
		return Messages.get(HeroSubClass.class, "super_desc",title());
	}

	public String shortDesc() {
		return Messages.get(HeroSubClass.class, name()+"_short_desc");
	}

	public String desc() {
		return Messages.get(HeroSubClass.class, name() + "_desc");
	}

	public String icon(){
		return icon;
	}

}
